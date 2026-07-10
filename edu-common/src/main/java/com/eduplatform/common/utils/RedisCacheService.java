package com.eduplatform.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 显式类型的 Redis 缓存服务。
 * MySQL 始终是事实来源，Redis 读写失败只按缓存未命中处理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final String NULL_VALUE = "__NULL__";
    private static final Duration NULL_TTL = Duration.ofMinutes(2);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final int MAX_LOCK_RETRIES = 3;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) end return 0",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T getOrLoad(
            String key, Class<T> valueType, Supplier<T> loader, Duration ttl) {
        CacheRead<T> cached = read(key, valueType);
        if (cached.found()) return cached.value();
        T value = loader.get();
        write(key, value, ttl);
        return value;
    }

    public <T> T getOrLoad(
            String key, Class<T> valueType, Supplier<T> loader, long baseTtlSeconds, int jitterSeconds) {
        return getOrLoad(key, valueType, loader, jitteredTtl(baseTtlSeconds, jitterSeconds));
    }

    public <T> T getOrLoadWithLock(
            String key, Class<T> valueType, Supplier<T> loader, Duration ttl) {
        CacheRead<T> cached = read(key, valueType);
        if (cached.found()) return cached.value();

        String lockKey = "lock:" + key;
        for (int attempt = 0; attempt < MAX_LOCK_RETRIES; attempt++) {
            String token = UUID.randomUUID().toString();
            LockResult lockResult = tryLock(lockKey, token);
            if (lockResult == LockResult.UNAVAILABLE) {
                return loader.get();
            }
            if (lockResult == LockResult.ACQUIRED) {
                try {
                    CacheRead<T> rechecked = read(key, valueType);
                    if (rechecked.found()) return rechecked.value();
                    T value = loader.get();
                    write(key, value, ttl);
                    return value;
                } finally {
                    unlock(lockKey, token);
                }
            }

            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(30, 71));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[Cache] 等待锁时被中断 key={}", key);
                return loader.get();
            }

            CacheRead<T> retry = read(key, valueType);
            if (retry.found()) return retry.value();
        }

        log.warn("[Cache] 锁竞争重试耗尽，回退数据源 key={}", key);
        return loader.get();
    }

    public <T> T getOrLoadWithLock(
            String key, Class<T> valueType, Supplier<T> loader, long baseTtlSeconds, int jitterSeconds) {
        return getOrLoadWithLock(
                key, valueType, loader, jitteredTtl(baseTtlSeconds, jitterSeconds));
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("[Cache] EVICT key={}", key);
        } catch (DataAccessException e) {
            log.warn("[Cache] 删除失败 key={}: {}", key, e.getMessage());
        }
    }

    public void evictPattern(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();
        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                }
                return null;
            });
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("[Cache] EVICT pattern={} count={}", pattern, keys.size());
            }
        } catch (DataAccessException e) {
            log.warn("[Cache] 批量删除失败 pattern={}: {}", pattern, e.getMessage());
        }
    }

    private <T> CacheRead<T> read(String key, Class<T> valueType) {
        final String cached;
        try {
            cached = redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            log.warn("[Cache] 读取失败，按未命中处理 key={}: {}", key, e.getMessage());
            return CacheRead.miss();
        }

        if (cached == null) return CacheRead.miss();
        if (NULL_VALUE.equals(cached)) return CacheRead.hit(null);
        try {
            return CacheRead.hit(objectMapper.readValue(cached, valueType));
        } catch (JsonProcessingException e) {
            log.warn("[Cache] JSON 解析失败，清除损坏缓存 key={}: {}", key, e.getMessage());
            evict(key);
            return CacheRead.miss();
        }
    }

    private void write(String key, Object value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("缓存 TTL 必须为正数");
        }
        try {
            if (value == null) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, NULL_TTL);
            } else {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
            }
        } catch (JsonProcessingException e) {
            log.warn("[Cache] JSON 序列化失败 key={}: {}", key, e.getMessage());
        } catch (DataAccessException e) {
            log.warn("[Cache] 写入失败 key={}: {}", key, e.getMessage());
        }
    }

    private LockResult tryLock(String lockKey, String token) {
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, LOCK_TTL);
            return Boolean.TRUE.equals(acquired) ? LockResult.ACQUIRED : LockResult.CONTENDED;
        } catch (DataAccessException e) {
            log.warn("[Cache] 获取锁失败 lockKey={}: {}", lockKey, e.getMessage());
            return LockResult.UNAVAILABLE;
        }
    }

    private void unlock(String lockKey, String token) {
        try {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        } catch (DataAccessException e) {
            log.warn("[Cache] 释放锁失败 lockKey={}: {}", lockKey, e.getMessage());
        }
    }

    private Duration jitteredTtl(long baseTtlSeconds, int jitterSeconds) {
        if (baseTtlSeconds <= 0 || jitterSeconds < 0) {
            throw new IllegalArgumentException("缓存 TTL 参数非法");
        }
        long jitter = jitterSeconds == 0
                ? 0
                : ThreadLocalRandom.current().nextLong(jitterSeconds + 1L);
        return Duration.ofSeconds(baseTtlSeconds + jitter);
    }

    private enum LockResult {
        ACQUIRED, CONTENDED, UNAVAILABLE
    }

    private record CacheRead<T>(boolean found, T value) {
        private static <T> CacheRead<T> hit(T value) {
            return new CacheRead<>(true, value);
        }

        private static <T> CacheRead<T> miss() {
            return new CacheRead<>(false, null);
        }
    }
}

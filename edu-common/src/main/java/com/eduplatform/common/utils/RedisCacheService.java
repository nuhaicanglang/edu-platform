package com.eduplatform.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 缓存工具服务
 * 解决缓存三大问题：
 * - 缓存穿透：缓存空值（NULL_VALUE），避免查不存在的数据每次打到 DB
 * - 缓存击穿：SETNX 互斥锁，热点 key 过期时只允许一个线程重建缓存
 * - 缓存雪崩：基础 TTL + 随机偏移，避免大量 key 同时过期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /** 空值占位符（防穿透） */
    private static final String NULL_VALUE = "__NULL__";
    /** 空值 TTL：2 分钟（短于正常 TTL，减少误缓存影响） */
    private static final long NULL_TTL_SECONDS = 120;
    /** 分布式锁默认超时 10 秒 */
    private static final long LOCK_TTL_SECONDS = 10;

    private final Random random = new Random();

    // ===================================================================
    // 防穿透 + 防雪崩：getOrLoad
    // ===================================================================

    /**
     * 从缓存取，缓存未命中则调用 loader 加载并写入缓存。
     * - 防穿透：loader 返回 null 时写入空值占位符
     * - 防雪崩：TTL = baseTtl + random(0, jitterSeconds)
     *
     * @param key          缓存 key
     * @param loader       加载数据的函数
     * @param baseTtl      基础 TTL（秒）
     * @param jitter       随机抖动范围（秒）
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> loader, long baseTtl, int jitter) {
        Object cached;
        try { cached = redisTemplate.opsForValue().get(key); }
        catch (Exception e) { log.warn("[Cache] get error key={}, evict and reload: {}", key, e.getMessage()); redisTemplate.delete(key); cached = null; }

        // 命中空值占位符（防穿透）
        if (NULL_VALUE.equals(cached)) {
            log.debug("[Cache] NULL hit key={}", key);
            return null;
        }

        // 正常命中
        if (cached != null) {
            try {
                log.debug("[Cache] HIT key={}", key);
                return (T) cached;
            } catch (Exception e) {
                // 反序列化类型不匹配或 Jackson 异常，清除后回退 DB
                log.warn("[Cache] deserialize error key={}, evict and reload: {}", key, e.getMessage());
                redisTemplate.delete(key);
            }
        }

        // 未命中，加载数据
        log.debug("[Cache] MISS key={}", key);
        T data = loader.get();

        long ttl = baseTtl + random.nextInt(Math.max(1, jitter));
        if (data == null) {
            // 防穿透：缓存空值
            redisTemplate.opsForValue().set(key, NULL_VALUE, NULL_TTL_SECONDS, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
        }
        return data;
    }

    // ===================================================================
    // 防穿透 + 防雪崩 + 防击穿：getOrLoadWithLock
    // ===================================================================

    /**
     * 带互斥锁的缓存获取，防止热点 key 失效时缓存击穿。
     * 获取锁失败时短暂等待后重试（最多 3 次），避免死循环。
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoadWithLock(String key, Supplier<T> loader, long baseTtl, int jitter) {
        // 先尝试直接读缓存
        Object cached;
        try { cached = redisTemplate.opsForValue().get(key); }
        catch (Exception e) { log.warn("[Cache] get error key={}: {}", key, e.getMessage()); cached = null; }
        if (NULL_VALUE.equals(cached)) return null;
        if (cached != null) {
            try { return (T) cached; } catch (Exception e) {
                log.warn("[Cache] cast error key={}, evict and reload", key);
                redisTemplate.delete(key);
            }
        }

        String lockKey = "lock:" + key;
        int retries = 3;

        while (retries-- > 0) {
            // 尝试获取分布式锁（SETNX）
            Boolean locked = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双重检查：拿到锁后再查一次缓存（可能其他线程已重建）
                    Object recheck = redisTemplate.opsForValue().get(key);
                    if (NULL_VALUE.equals(recheck)) return null;
                    if (recheck != null) {
                        try { return (T) recheck; } catch (ClassCastException e) {
                            log.warn("[Cache] recheck cast error key={}", key);
                        }
                    }

                    // 真正加载
                    T data = loader.get();
                    long ttl = baseTtl + random.nextInt(Math.max(1, jitter));
                    if (data == null) {
                        redisTemplate.opsForValue().set(key, NULL_VALUE, NULL_TTL_SECONDS, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
                    }
                    return data;
                } finally {
                    stringRedisTemplate.delete(lockKey);
                }
            } else {
                // 未拿到锁，等待 50ms 重试
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                Object retry = redisTemplate.opsForValue().get(key);
                if (NULL_VALUE.equals(retry)) return null;
                if (retry != null) {
                    try {
                        return (T) retry;
                    } catch (ClassCastException e) {
                        log.warn("[Cache] retry cast error key={}", key);
                        redisTemplate.delete(key);
                    }
                }
            }
        }

        // 兜底：直接查 DB（锁竞争失败也不让请求失败）
        log.warn("[Cache] lock retry exhausted, fallback to loader key={}", key);
        return loader.get();
    }

    // ===================================================================
    // 缓存失效
    // ===================================================================

    /** 删除单个 key */
    public void evict(String key) {
        redisTemplate.delete(key);
        log.debug("[Cache] EVICT key={}", key);
    }

    /**
     * 批量删除匹配 pattern 的 key。
     * <p>
     * 使用 SCAN 迭代而非 KEYS 命令，避免在生产大数据量下阻塞 Redis。
     * 每批处理 500 个 key，游标式扫描完成后统一批量删除。
     * </p>
     */
    public void evictPattern(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();
        redisTemplate.execute((org.springframework.data.redis.connection.RedisConnection conn) -> {
            try (Cursor<byte[]> cursor = conn.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                log.warn("[Cache] SCAN error pattern={}: {}", pattern, e.getMessage());
            }
            return null;
        });
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("[Cache] EVICT pattern={} count={}", pattern, keys.size());
        }
    }
}

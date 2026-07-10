package com.eduplatform.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock StringRedisTemplate template;
    @Mock ValueOperations<String, String> values;

    private ObjectMapper objectMapper;
    private RedisCacheService cache;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cache = new RedisCacheService(template, objectMapper);
        when(template.opsForValue()).thenReturn(values);
    }

    @Test
    void readsCachedValueUsingExplicitType() throws Exception {
        CourseSnapshot expected = new CourseSnapshot(1L, "数据结构");
        when(values.get("course:1")).thenReturn(objectMapper.writeValueAsString(expected));

        CourseSnapshot result = cache.getOrLoad(
                "course:1", CourseSnapshot.class,
                () -> fail("缓存命中时不应访问数据库"), Duration.ofMinutes(5));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void releasesLockWithOwnershipScriptInsteadOfDeletingKey() {
        when(values.get("course:1")).thenReturn(null);
        when(values.setIfAbsent(eq("lock:course:1"), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(template.execute(any(RedisScript.class), anyList(), any())).thenReturn(0L);

        cache.getOrLoadWithLock(
                "course:1", CourseSnapshot.class,
                () -> new CourseSnapshot(1L, "数据结构"), Duration.ofMinutes(5));

        verify(template).execute(any(RedisScript.class), eq(List.of("lock:course:1")), any());
        verify(template, never()).delete("lock:course:1");
    }

    @Test
    void loadsDatabaseWhenRedisReadFails() {
        when(values.get("course:1"))
                .thenThrow(new RedisConnectionFailureException("down"));

        CourseSnapshot result = cache.getOrLoad(
                "course:1", CourseSnapshot.class,
                () -> new CourseSnapshot(1L, "数据结构"), Duration.ofMinutes(5));

        assertThat(result.id()).isEqualTo(1L);
    }

    private record CourseSnapshot(Long id, String name) {}
}

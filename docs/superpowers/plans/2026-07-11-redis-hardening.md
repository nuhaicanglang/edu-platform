# Redis Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Retain useful caching and conversation history while removing unsafe polymorphic deserialization, incorrect lock release, and fragile Redis failure handling.

**Architecture:** Cache values are explicit-type JSON stored through `StringRedisTemplate`; locks carry UUID ownership tokens and use compare-and-delete Lua. Call sites keep MySQL as the source of truth and conversation keys include both user and course.

**Tech Stack:** Spring Data Redis, Jackson, JUnit 5, Mockito, Testcontainers Redis

---

### Task 1: Define a typed cache contract

**Files:**
- Modify: `edu-common/src/main/java/com/eduplatform/common/config/RedisConfig.java`
- Modify: `edu-common/src/main/java/com/eduplatform/common/utils/RedisCacheService.java`
- Test: `edu-common/src/test/java/com/eduplatform/common/utils/RedisCacheServiceTest.java`

- [ ] **Step 1: Write failing typed serialization tests**

```java
@Test
void readsCachedValueUsingExplicitType() throws Exception {
    CourseSnapshot expected = new CourseSnapshot(1L, "数据结构");
    when(values.get("course:1")).thenReturn(objectMapper.writeValueAsString(expected));
    assertEquals(expected, cache.getOrLoad("course:1", CourseSnapshot.class,
            () -> fail("loader must not run"), Duration.ofMinutes(5)));
}

private record CourseSnapshot(Long id, String name) {}
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-common -Dtest=RedisCacheServiceTest test`

Expected: FAIL because the explicit-type overload does not exist.

- [ ] **Step 3: Implement the typed API**

Store JSON strings through `StringRedisTemplate`, deserialize only into the caller-provided `Class<T>`, and keep a reserved null sentinel. Remove `activateDefaultTyping` and `LaissezFaireSubTypeValidator` from `RedisConfig`.

- [ ] **Step 4: Run GREEN**

Run the same test. Expected: PASS.

### Task 2: Release only owned locks

**Files:**
- Modify: `edu-common/src/main/java/com/eduplatform/common/utils/RedisCacheService.java`
- Test: `edu-common/src/test/java/com/eduplatform/common/utils/RedisCacheServiceTest.java`

- [ ] **Step 1: Write the failing ownership test**

```java
@Test
void doesNotDeleteLockOwnedByAnotherWorker() {
    when(values.setIfAbsent(eq("lock:course:1"), anyString(), any(Duration.class))).thenReturn(true);
    when(template.execute(any(DefaultRedisScript.class), anyList(), any())).thenReturn(0L);
    cache.getOrLoadWithLock("course:1", CourseSnapshot.class,
            () -> new CourseSnapshot(1L, "数据结构"), Duration.ofMinutes(5));
    verify(template, never()).delete("lock:course:1");
}
```

- [ ] **Step 2: Run RED**

Expected: FAIL because current code unconditionally deletes the lock key.

- [ ] **Step 3: Implement compare-and-delete**

Use a UUID value and this Lua script:

```lua
if redis.call('get', KEYS[1]) == ARGV[1] then
  return redis.call('del', KEYS[1])
end
return 0
```

Use bounded retries, `ThreadLocalRandom` jitter, and preserve interruption.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-common -Dtest=RedisCacheServiceTest test
git add edu-common/src
git commit -m "fix: make redis cache serialization and locks safe"
```

### Task 3: Make Redis failure a cache miss, not an application failure

**Files:**
- Modify: `edu-common/src/main/java/com/eduplatform/common/utils/RedisCacheService.java`
- Test: `edu-common/src/test/java/com/eduplatform/common/utils/RedisCacheServiceTest.java`

- [ ] **Step 1: Write failing outage tests**

```java
@Test
void loadsDatabaseWhenRedisReadFails() {
    when(values.get("course:1")).thenThrow(new RedisConnectionFailureException("down"));
    CourseSnapshot value = cache.getOrLoad("course:1", CourseSnapshot.class,
            () -> new CourseSnapshot(1L, "数据结构"), Duration.ofMinutes(5));
    assertEquals(1L, value.id());
}
```

- [ ] **Step 2: Run RED**

Expected: FAIL if the catch path attempts another Redis operation or propagates the connection error.

- [ ] **Step 3: Implement guarded read/write helpers**

All Redis reads, writes, deletes, and scripts catch `DataAccessException`, log key and operation without cached content, and continue with the loader. Never call another Redis operation from a Redis exception handler.

- [ ] **Step 4: Run GREEN**

Run: `mvn -pl edu-common test`. Expected: PASS.

### Task 4: Update cache call sites and isolate conversations

**Files:**
- Modify: `edu-system/src/main/java/com/eduplatform/system/service/CourseService.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/service/ClassGroupService.java`
- Modify: `edu-agent/src/main/java/com/eduplatform/agent/service/IntelligentQAService.java`
- Test: `edu-agent/src/test/java/com/eduplatform/agent/service/IntelligentQAServiceTest.java`

- [ ] **Step 1: Write the course-isolated history test**

```java
@Test
void conversationHistoryKeyContainsUserAndCourse() {
    service.ask("问题", 12L, 7L);
    verify(values).get("qa:history:7:course:12");
}
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-agent -am -Dtest=IntelligentQAServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because the existing API accepts raw course context and keys only by user.

- [ ] **Step 3: Update call sites**

Pass `Course.class` and `ClassGroup.class` to the cache service. Change QA history keys to user plus course, handle Redis outage as empty history, and cap retained history by configured characters and TTL.

- [ ] **Step 4: Verify, commit, and push**

```bash
mvn -pl edu-common,edu-system,edu-agent -am test
git add edu-common/src edu-system/src edu-agent/src
git commit -m "fix: isolate redis caches and conversation history"
git push origin codex/project-hardening-rag
```

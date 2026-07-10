# Security Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close privilege escalation, forged identity, path traversal, cross-tenant access, and deterministic denial-of-service defects before adding RAG.

**Architecture:** Authentication becomes configuration-driven with no default signing key, the gateway strips spoofed identity headers, and Docker exposes only the gateway. Resource authorization is centralized in service-layer policies, while file paths and chunking become pure, testable units.

**Tech Stack:** Java 17, Spring Boot 3.2, JUnit 5, Mockito, Spring Cloud Gateway, MyBatis-Plus, Docker Compose

---

### Task 1: Establish the backend test surface

**Files:**
- Modify: `pom.xml`
- Modify: `edu-common/pom.xml`
- Modify: `edu-auth/pom.xml`
- Modify: `edu-gateway/pom.xml`
- Modify: `edu-system/pom.xml`
- Modify: `edu-knowledge/pom.xml`

- [ ] **Step 1: Add managed test dependencies**

Add this dependency to each module that receives tests:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Verify Maven discovers tests**

Run: `mvn -q -DskipTests=false test`

Expected: reactor succeeds and Surefire runs JUnit Platform rather than reporting a missing provider.

- [ ] **Step 3: Commit the test foundation**

```bash
git add pom.xml edu-common/pom.xml edu-auth/pom.xml edu-gateway/pom.xml edu-system/pom.xml edu-knowledge/pom.xml
git commit -m "test: establish backend test foundation"
```

### Task 2: Remove registration role escalation

**Files:**
- Modify: `edu-auth/src/main/java/com/eduplatform/auth/domain/dto/RegisterDTO.java`
- Modify: `edu-auth/src/main/java/com/eduplatform/auth/service/AuthService.java`
- Test: `edu-auth/src/test/java/com/eduplatform/auth/service/AuthServiceTest.java`

- [ ] **Step 1: Write the failing registration test**

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock SysUserMapper userMapper;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void publicRegistrationAlwaysCreatesStudent() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        AuthService service = new AuthService(userMapper, encoder);
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("new-user");
        dto.setPassword("StrongPassword123!");
        dto.setRealName("测试学生");

        service.register(dto);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("student", captor.getValue().getRole());
    }
}
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-auth -am -Dtest=AuthServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because registration still copies the absent role as `null` instead of assigning `student`.

- [ ] **Step 3: Implement the minimal rule**

Remove `role` from `RegisterDTO` and replace the assignment with:

```java
user.setRole("student");
```

- [ ] **Step 4: Run GREEN**

Run the same Maven command. Expected: PASS.

### Task 3: Replace static JWT defaults with a validated security module

**Files:**
- Create: `edu-security/pom.xml`
- Create: `edu-security/src/main/java/com/eduplatform/security/JwtProperties.java`
- Create: `edu-security/src/main/java/com/eduplatform/security/JwtService.java`
- Create: `edu-security/src/main/java/com/eduplatform/security/JwtSecurityConfiguration.java`
- Modify: `pom.xml`
- Modify: `edu-auth/src/main/java/com/eduplatform/auth/service/AuthService.java`
- Modify: `edu-gateway/src/main/java/com/eduplatform/gateway/filter/AuthGlobalFilter.java`
- Modify: `edu-auth/src/main/resources/application.yml`
- Modify: `edu-gateway/src/main/resources/application.yml`
- Test: `edu-common/src/test/java/com/eduplatform/common/security/JwtServiceTest.java`

- [ ] **Step 1: Write failing JWT tests**

```java
class JwtServiceTest {
    @Test
    void rejectsBlankSecret() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtService(new JwtProperties("", Duration.ofHours(24))));
    }

    @Test
    void tokenCannotBeVerifiedWithAnotherSecret() {
        JwtService issuer = new JwtService(new JwtProperties("a".repeat(32), Duration.ofHours(1)));
        JwtService verifier = new JwtService(new JwtProperties("b".repeat(32), Duration.ofHours(1)));
        String token = issuer.generateToken(1L, "student1", "student");
        assertThrows(JwtException.class, () -> verifier.parseToken(token));
    }
}
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-security -Dtest=JwtServiceTest test`

Expected: FAIL because the service and properties do not exist.

- [ ] **Step 3: Implement validated JWT configuration**

`JwtProperties` is a `@ConfigurationProperties(prefix = "jwt")` record with `secret` and `expiration`. `JwtService` rejects secrets shorter than 32 UTF-8 bytes in its constructor and owns generate/parse operations. `JwtSecurityConfiguration` is explicitly imported by Auth and Gateway so the reactive Gateway does not depend on the Servlet-heavy common module. Configure:

```yaml
jwt:
  secret: ${JWT_SECRET:}
  expiration: 24h
```

Inject `JwtService` into `AuthService` and `AuthGlobalFilter`; delete both hardcoded defaults and stop using static `JwtUtils` in production paths.

- [ ] **Step 4: Run GREEN and regression tests**

Run: `mvn -pl edu-common,edu-auth,edu-gateway -am test`

Expected: all three modules PASS.

- [ ] **Step 5: Commit authentication fixes**

```bash
git add edu-common/src edu-auth/src edu-gateway/src edu-common/pom.xml edu-auth/pom.xml edu-gateway/pom.xml
git commit -m "fix: close registration and jwt privilege escalation"
```

### Task 4: Enforce the gateway trust boundary

**Files:**
- Modify: `edu-gateway/src/main/java/com/eduplatform/gateway/filter/AuthGlobalFilter.java`
- Modify: `docker-compose.yml`
- Test: `edu-gateway/src/test/java/com/eduplatform/gateway/filter/AuthGlobalFilterTest.java`

- [ ] **Step 1: Write the spoofed-header test**

Build a `MockServerHttpRequest` containing forged `X-User-Id` and `X-User-Role`, pass a valid student token, and assert the downstream request contains exactly the claims from the token.

```java
assertEquals(List.of("7"), captured.getHeaders().get("X-User-Id"));
assertEquals(List.of("student"), captured.getHeaders().get("X-User-Role"));
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-gateway -am -Dtest=AuthGlobalFilterTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because the original request values are retained or duplicated.

- [ ] **Step 3: Strip and replace identity headers**

Use `headers(headers -> { headers.remove(...); headers.set(...); })` for all three identity headers. In Compose, keep `ports` only for `edu-gateway`; remove host port publication for `edu-auth`, `edu-system`, `edu-agent`, and `edu-knowledge`.

- [ ] **Step 4: Verify**

Run the gateway test and `docker compose config --quiet`.

Expected: PASS and Compose exit 0.

- [ ] **Step 5: Commit**

```bash
git add edu-gateway/src docker-compose.yml
git commit -m "fix: enforce gateway identity boundary"
```

### Task 5: Make file resolution and chunking safe

**Files:**
- Create: `edu-system/src/main/java/com/eduplatform/system/service/FilePathResolver.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/service/FileUploadService.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/controller/FileServeController.java`
- Modify: `edu-knowledge/src/main/java/com/eduplatform/knowledge/service/DocumentParsingService.java`
- Test: `edu-system/src/test/java/com/eduplatform/system/service/FilePathResolverTest.java`
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/service/DocumentParsingServiceTest.java`

- [ ] **Step 1: Write failing path and chunk tests**

```java
@Test
void rejectsTraversalOutsideUploadRoot() {
    FilePathResolver resolver = new FilePathResolver(tempDir.toString());
    assertThrows(BusinessException.class, () -> resolver.resolve("../secret.txt"));
}

@Test
void longSingleParagraphTerminatesAndCoversTail() {
    String text = "数据结构".repeat(400);
    List<String> chunks = service.splitToChunks(text, 500, 50);
    assertFalse(chunks.isEmpty());
    assertTrue(chunks.size() < 20);
    assertTrue(chunks.get(chunks.size() - 1).endsWith(text.substring(text.length() - 20)));
}
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-system,edu-knowledge -am -Dtest=FilePathResolverTest,DocumentParsingServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: path test cannot compile and chunk test times out or exceeds the asserted chunk count.

- [ ] **Step 3: Implement safe pure functions**

Resolve with an absolute normalized root and reject candidates that do not start with it. In the chunk loop validate `chunkSize > overlap && overlap >= 0`, and break when `end == text.length()` before subtracting overlap.

- [ ] **Step 4: Run GREEN and commit**

Run the same tests, then:

```bash
git add edu-system/src edu-knowledge/src
git commit -m "fix: secure file paths and text chunking"
```

### Task 6: Add object-level authorization

**Files:**
- Create: `edu-system/src/main/java/com/eduplatform/system/security/ResourceAuthorizationService.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/controller/CourseController.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/controller/ClassGroupController.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/controller/AssignmentController.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/service/AssignmentService.java`
- Test: `edu-system/src/test/java/com/eduplatform/system/security/ResourceAuthorizationServiceTest.java`

- [ ] **Step 1: Write the teacher ownership and student submission tests**

```java
@Test
void teacherCannotModifyAnotherTeachersCourse() {
    Course course = new Course();
    course.setId(9L);
    course.setTeacherId(22L);
    when(courseMapper.selectById(9L)).thenReturn(course);
    assertThrows(BusinessException.class,
            () -> service.requireCourseManager(9L, 21L, "teacher"));
}

@Test
void studentCannotReadAnotherStudentsSubmission() {
    AssignmentSubmission submission = new AssignmentSubmission();
    submission.setId(5L);
    submission.setStudentId(9L);
    submission.setAssignmentId(31L);
    when(submissionMapper.selectById(5L)).thenReturn(submission);
    assertThrows(BusinessException.class,
            () -> service.requireSubmissionAccess(5L, 8L, "student"));
}
```

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-system -am -Dtest=ResourceAuthorizationServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because the policy service does not exist.

- [ ] **Step 3: Implement and apply policies**

Add explicit methods for course manager, class manager, assignment manager, submission owner, and course membership. Pass authenticated user ID and role from controllers into service methods. Verify the submission's `assignmentId` matches the path value before grading.

- [ ] **Step 4: Run GREEN and the module suite**

Run: `mvn -pl edu-system -am test`

Expected: PASS.

- [ ] **Step 5: Commit and push the phase**

```bash
git add edu-system/src
git commit -m "fix: enforce object-level authorization"
git push -u origin codex/project-hardening-rag
```

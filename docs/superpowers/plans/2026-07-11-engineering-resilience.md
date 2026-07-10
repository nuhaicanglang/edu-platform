# Engineering Resilience Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the overhaul with correct HTTP semantics, bounded background work, dependency and repository hygiene, CI gates, and accurate operating documentation.

**Architecture:** External calls use shared timeout-aware clients, async jobs expose explicit saturation and retry states, API errors map to HTTP status codes, and CI reproduces all non-LLM verification. Secrets and generated artifacts are excluded from version control.

**Tech Stack:** Spring Boot, Java concurrency, Maven, npm, Vitest, GitHub Actions, Docker Compose

---

### Task 1: Return real HTTP status codes

**Files:**
- Modify: `edu-common/src/main/java/com/eduplatform/common/exception/GlobalExceptionHandler.java`
- Modify: `edu-common/src/main/java/com/eduplatform/common/exception/BusinessException.java`
- Test: `edu-common/src/test/java/com/eduplatform/common/exception/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write failing status tests**

```java
@Test
void forbiddenBusinessExceptionReturnsHttp403() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/course/1");
    ResponseEntity<R<?>> response = handler.handleBusinessException(
            new BusinessException(403, "权限不足"), request);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
}
```

- [ ] **Step 2: Run RED**

Expected: handler returns `R<?>`, not `ResponseEntity<R<?>>`.

- [ ] **Step 3: Map codes to statuses**

Return `ResponseEntity` for 400, 401, 403, 404, 409, 429, 500, and 503; preserve the response body code and sanitized message.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-common test
git add edu-common/src
git commit -m "fix: return correct http error statuses"
```

### Task 2: Bound AI clients and async grading

**Files:**
- Modify: `edu-system/src/main/java/com/eduplatform/system/config/AsyncConfig.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/service/AgentClientService.java`
- Modify: `edu-system/src/main/java/com/eduplatform/system/service/AssignmentService.java`
- Test: `edu-system/src/test/java/com/eduplatform/system/config/AsyncConfigTest.java`

- [ ] **Step 1: Write failing saturation and timeout tests**

Assert the executor uses `AbortPolicy`, and a rejected grading task changes the submission to a retryable state and returns a 429/503 business exception instead of running in the request thread.

- [ ] **Step 2: Run RED**

Expected: current executor uses `CallerRunsPolicy` and `RestTemplate` has no explicit timeouts.

- [ ] **Step 3: Implement bounded behavior**

Create the HTTP client through a bean with 5-second connect and configured read timeout. Use `AbortPolicy`, catch `TaskRejectedException`, persist `queued/retry` state, and make duplicate grading requests idempotent.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-system -am test
git add edu-system/src
git commit -m "fix: bound ai calls and grading concurrency"
```

### Task 3: Align schema, validation, and pagination

**Files:**
- Modify: `edu-agent/src/main/java/com/eduplatform/agent/agent/AgentTools.java`
- Modify: `edu-common/src/main/java/com/eduplatform/common/core/domain/PageQuery.java`
- Modify: controller request DTOs across `edu-system`, `edu-agent`, and `edu-knowledge`
- Modify: `sql/schema.sql`
- Test: `edu-common/src/test/java/com/eduplatform/common/core/domain/PageQueryTest.java`

- [ ] **Step 1: Write failing validation tests**

Assert `pageNum=0`, `pageSize=0`, and `pageSize=101` fail validation; assert AgentTools SQL contains only schema fields.

- [ ] **Step 2: Run RED**

Expected: invalid pagination currently reaches MyBatis and SQL still references `ai_score` before the RAG phase removes it.

- [ ] **Step 3: Implement constraints**

Use `@Min(1)` and `@Max(100)`, apply `@Validated`, align grading status enum values with schema comments, and ensure seed/reseed scripts use the same fields.

- [ ] **Step 4: Verify and commit**

```bash
mvn test
git add edu-common/src edu-agent/src edu-system/src edu-knowledge/src sql
git commit -m "fix: align api validation and database schema"
```

### Task 4: Remove secrets and generated artifacts from Git

**Files:**
- Delete from repository index: `token.txt`
- Modify: `.gitignore`
- Modify: `.env.example`
- Inspect only: local `.env`

- [ ] **Step 1: Add a secret regression check**

Run a redacted scan that fails when a JWT-shaped token, private key, or non-placeholder `sk-` value is tracked.

- [ ] **Step 2: Remove tracked credentials**

Remove `token.txt` from the inner repository, retain `.env` ignored, document required `JWT_SECRET`, and rotate the local secret before runtime verification. History cleanup is a separate explicitly reviewed operation because the token is already present in the remote history.

- [ ] **Step 3: Verify**

Run: `git grep -n -E 'eyJ[A-Za-z0-9_-]+\.eyJ|BEGIN .*PRIVATE KEY|sk-[A-Za-z0-9_-]{16,}'`

Expected: no tracked live credential matches.

- [ ] **Step 4: Commit**

```bash
git add .gitignore .env.example
git rm --cached token.txt
git commit -m "security: remove tracked credentials"
```

### Task 5: Upgrade frontend dependencies and add CI

**Files:**
- Modify: `edu-frontend/package.json`
- Modify: `edu-frontend/package-lock.json`
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Upgrade audited production dependencies**

Use `npm audit` to select non-vulnerable compatible versions of Axios and React Router, update the lock file, and do not use `--force` across major versions without a failing compatibility test.

- [ ] **Step 2: Verify locally**

Run:

```bash
npm test
npm run build
npm audit --omit=dev
```

Expected: tests and build PASS; production audit reports zero high or critical vulnerabilities.

- [ ] **Step 3: Add CI**

The workflow checks out code, installs JDK 17 and Node 20, caches Maven/npm dependencies, runs `mvn test`, `npm ci`, `npm test`, `npm run build`, `npm audit --omit=dev`, and `docker compose config --quiet`. Real Ollama tests remain opt-in.

- [ ] **Step 4: Commit**

```bash
git add edu-frontend/package.json edu-frontend/package-lock.json .github/workflows/ci.yml
git commit -m "ci: enforce tests builds and dependency audit"
```

### Task 6: Update operating documentation without overwriting user work

**Files:**
- Modify carefully: `README.md`
- Modify: `.env.example`
- Modify: `docker-compose.yml`

- [ ] **Step 1: Re-read the untracked README before editing**

Confirm the current file still belongs to the user and preserve all useful content. If it changed since planning, merge only the new security, Ollama, Elasticsearch, reindex, test, and troubleshooting sections.

- [ ] **Step 2: Document exact operation**

Include Docker Desktop startup, `ollama list`, `bge-m3` requirement, local/container Ollama URLs, JWT secret generation, Compose startup, reindex endpoint, health checks, test commands, and explicit degraded behavior.

- [ ] **Step 3: Verify commands**

Run every non-destructive documented command and `docker compose config --quiet`. Ensure README claims match current code and do not claim vector RAG before the integration test passes.

- [ ] **Step 4: Commit only after confirming README scope**

```bash
git add README.md .env.example docker-compose.yml
git commit -m "docs: document secure rag operations"
```

### Task 7: Final isolated verification and staged push

**Files:**
- No source changes expected

- [ ] **Step 1: Verify the complete backend**

Run: `mvn test`

Expected: all module tests PASS with zero failures and zero errors.

- [ ] **Step 2: Verify the frontend**

Run: `npm test && npm run build && npm audit --omit=dev`

Expected: PASS and no high/critical production vulnerabilities.

- [ ] **Step 3: Verify infrastructure and RAG smoke**

Start Docker Desktop, run `docker compose up -d` for required infrastructure, check health, run the explicit Ollama RAG integration test, and confirm reindex is idempotent.

- [ ] **Step 4: Re-read Git scope**

Run `git status --short --branch -uall`, `git diff --check`, `git log --oneline origin/codex/project-hardening-rag..HEAD`, and confirm `start-services.bat` is included only if intentionally modified by an implementation task.

- [ ] **Step 5: Push final phase**

```bash
git push origin codex/project-hardening-rag
```

Expected: remote branch contains all staged commits; no force push is used.

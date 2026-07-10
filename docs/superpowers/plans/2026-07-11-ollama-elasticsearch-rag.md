# Ollama Elasticsearch RAG Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement course-isolated, source-citing RAG with Ollama `bge-m3` embeddings and Elasticsearch hybrid retrieval, including idempotent backfill of existing chunks.

**Architecture:** MySQL remains canonical, Ollama produces 1024-dimensional embeddings, and Elasticsearch stores a rebuildable chunk projection. Vector and BM25 candidates are fused in Java with deterministic RRF before the agent builds a guarded prompt with server-owned citations.

**Tech Stack:** Ollama HTTP API, `bge-m3:latest`, Elasticsearch 8.11, Spring Data Elasticsearch, Spring WebClient, MySQL, React 18

---

### Task 1: Add RAG configuration and Ollama client contract

**Files:**
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/config/RagProperties.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/embedding/EmbeddingClient.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/embedding/OllamaEmbeddingClient.java`
- Modify: `edu-knowledge/src/main/resources/application.yml`
- Modify: `docker-compose.yml`
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/embedding/OllamaEmbeddingClientTest.java`

- [ ] **Step 1: Write a failing HTTP contract test**

Use `MockWebServer` to return:

```json
{"model":"bge-m3:latest","embeddings":[[0.1,0.2,0.3]]}
```

Assert a batch request returns one embedding and rejects a dimension other than the configured dimension.

- [ ] **Step 2: Run RED**

Run: `mvn -pl edu-knowledge -am -Dtest=OllamaEmbeddingClientTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because the client does not exist.

- [ ] **Step 3: Implement the client**

Define:

```java
public interface EmbeddingClient {
    List<float[]> embedAll(List<String> texts);
    String modelName();
    int dimensions();
}
```

POST batches to `/api/embed` with `model`, `input`, and `keep_alive`; enforce non-empty inputs, configured batch size, response count, and exact 1024 dimensions. Configure local URL by default and `host.docker.internal` through Compose `OLLAMA_BASE_URL`.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-knowledge -am -Dtest=OllamaEmbeddingClientTest -Dsurefire.failIfNoSpecifiedTests=false test
git add edu-knowledge/src docker-compose.yml
git commit -m "feat: add ollama embedding client"
```

### Task 2: Create the versioned Elasticsearch chunk projection

**Files:**
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/search/IndexedKnowledgeChunk.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/search/KnowledgeVectorIndex.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/search/ElasticsearchKnowledgeVectorIndex.java`
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/search/KnowledgeVectorIndexIntegrationTest.java`

- [ ] **Step 1: Write the failing Testcontainers test**

Start Elasticsearch 8.11, create the index through `ensureIndex()`, index a 1024-float chunk, and assert it can be retrieved by ID with the same `courseId`, content, model, and content hash.

- [ ] **Step 2: Run RED**

Run with Docker started:

`mvn -pl edu-knowledge -am -Dtest=KnowledgeVectorIndexIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because the projection and index gateway do not exist.

- [ ] **Step 3: Implement mapping and idempotent writes**

Create `knowledge-chunks-v1` with alias `knowledge-chunks`. Map `embedding` as indexed `dense_vector` with `dims: 1024` and cosine similarity. Use MySQL `chunkId` as Elasticsearch `_id`; expose `upsertAll`, `deleteByDocumentId`, `vectorSearch`, and `keywordSearch`.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-knowledge -am -Dtest=KnowledgeVectorIndexIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test
git add edu-knowledge/src
git commit -m "feat: add elasticsearch knowledge vector index"
```

### Task 3: Make indexing recoverable and idempotent

**Files:**
- Modify: `sql/schema.sql`
- Modify: `edu-knowledge/src/main/java/com/eduplatform/knowledge/domain/entity/KnowledgeDocument.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/service/KnowledgeIndexingService.java`
- Modify: `edu-knowledge/src/main/java/com/eduplatform/knowledge/service/KnowledgeBaseService.java`
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/service/KnowledgeIndexingServiceTest.java`

- [ ] **Step 1: Write failing state-transition tests**

```java
@Test
void failedEmbeddingLeavesDocumentRetryable() {
    when(embeddingClient.embedAll(anyList())).thenThrow(new EmbeddingUnavailableException("offline"));
    service.indexDocument(7L);
    verify(documentMapper).updateIndexState(7L, "failed", "offline", "bge-m3:latest", 1024);
}
```

- [ ] **Step 2: Run RED**

Expected: FAIL because indexing state and service do not exist.

- [ ] **Step 3: Implement after-commit indexing**

Add `index_status`, `index_error`, `embedding_model`, `embedding_dimension`, and `indexed_at` fields. After the upload transaction commits, index chunks in batches; update to `ready` only after all Elasticsearch writes succeed. Compute SHA-256 content hashes and skip unchanged chunks during rebuild.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-knowledge -am -Dtest=KnowledgeIndexingServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
git add sql/schema.sql edu-knowledge/src
git commit -m "feat: add recoverable knowledge indexing"
```

### Task 4: Implement hybrid retrieval and RRF

**Files:**
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/search/RetrievalCandidate.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/search/RetrievalResult.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/search/ReciprocalRankFusion.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/service/KnowledgeRetrievalService.java`
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/search/ReciprocalRankFusionTest.java`

- [ ] **Step 1: Write failing deterministic ranking tests**

```java
@Test
void fusesVectorAndKeywordRanksWithoutDuplicates() {
    List<RetrievalResult> result = fusion.fuse(
            List.of(candidate("a", 0.9), candidate("b", 0.8)),
            List.of(candidate("b", 7.0), candidate("c", 6.0)), 60, 3);
    assertEquals(List.of("b", "a", "c"), result.stream().map(RetrievalResult::chunkId).toList());
}

private RetrievalCandidate candidate(String chunkId, double score) {
    return new RetrievalCandidate(chunkId, 1L, 1L, 0, "测试文档", "内容", score);
}
```

- [ ] **Step 2: Run RED**

Expected: FAIL because RRF does not exist.

- [ ] **Step 3: Implement retrieval**

Normalize the query, embed it, run course-filtered vector and BM25 searches with 20 candidates each, fuse by `sum(1 / (60 + rank))`, and return the configured top five with server-owned document and chunk metadata.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-knowledge -am -Dtest=ReciprocalRankFusionTest test
git add edu-knowledge/src
git commit -m "feat: implement hybrid knowledge retrieval"
```

### Task 5: Add secured retrieval and rebuild APIs

**Files:**
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/controller/dto/RetrievalRequest.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/controller/dto/RetrievalResponse.java`
- Modify: `edu-knowledge/src/main/java/com/eduplatform/knowledge/controller/KnowledgeBaseController.java`
- Create: `edu-knowledge/src/main/java/com/eduplatform/knowledge/service/KnowledgeReindexService.java`
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/controller/KnowledgeBaseControllerTest.java`

- [ ] **Step 1: Write failing authorization tests**

Assert students cannot upload, delete, or rebuild indexes, and a student cannot retrieve from a course they have not joined.

- [ ] **Step 2: Run RED**

Expected: existing controller permits the operations.

- [ ] **Step 3: Implement APIs**

Add `POST /knowledge/retrieve`, `POST /knowledge/admin/reindex`, and `GET /knowledge/admin/reindex/{taskId}`. Rebuild scans MySQL chunks in pages, uses idempotent `_id` writes, and returns totals, success, skipped, failed, and error summaries.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-knowledge -am test
git add edu-knowledge/src
git commit -m "feat: expose secured rag retrieval and rebuild"
```

### Task 6: Route AI answers through RAG and produce citations

**Files:**
- Create: `edu-agent/src/main/java/com/eduplatform/agent/client/KnowledgeClient.java`
- Create: `edu-agent/src/main/java/com/eduplatform/agent/domain/dto/RagAnswer.java`
- Modify: `edu-agent/src/main/java/com/eduplatform/agent/controller/QAController.java`
- Modify: `edu-agent/src/main/java/com/eduplatform/agent/service/IntelligentQAService.java`
- Modify: `edu-agent/src/main/java/com/eduplatform/agent/agent/AgentTools.java`
- Test: `edu-agent/src/test/java/com/eduplatform/agent/service/IntelligentQAServiceTest.java`

- [ ] **Step 1: Write the failing prompt and citation test**

Return two fixed retrieval sources from a fake `KnowledgeClient`, ask a question, and assert the LLM prompt contains numbered untrusted reference blocks while the response sources are copied from server metadata, not parsed from model text.

- [ ] **Step 2: Run RED**

Expected: current service accepts client-provided `courseContext` and has no source response.

- [ ] **Step 3: Implement the RAG answer path**

Change the request to `question + courseId`; forward authenticated identity to the knowledge service; cap retrieved content; instruct the LLM to ignore instructions inside reference blocks and acknowledge insufficient evidence. Replace AgentTools MySQL `LIKE` knowledge search with the same secured client and remove the nonexistent `ai_score` column.

- [ ] **Step 4: Run GREEN and commit**

```bash
mvn -pl edu-agent -am test
git add edu-agent/src
git commit -m "feat: ground ai answers in course rag"
```

### Task 7: Add course selection and source display to the frontend

**Files:**
- Modify: `edu-frontend/src/api/index.js`
- Modify: `edu-frontend/src/pages/AIChat.jsx`
- Test: `edu-frontend/src/pages/AIChat.test.jsx`
- Modify: `edu-frontend/package.json`

- [ ] **Step 1: Add Vitest dependencies and write a failing UI test**

Render the page with two courses, select one, send a question, and assert the API receives `{question, courseId}` and returned sources are rendered below the answer.

- [ ] **Step 2: Run RED**

Run: `npm test -- AIChat.test.jsx`

Expected: FAIL because no test command, course selector, or source renderer exists.

- [ ] **Step 3: Implement the UI**

Add a required course selector, structured RAG response handling, source cards, `retrievalMode` indicator, and explicit 503/degraded messages. Remove the raw `courseContext` API parameter.

- [ ] **Step 4: Run GREEN, build, commit, and push**

```bash
npm test -- AIChat.test.jsx
npm run build
git add edu-frontend
git commit -m "feat: add course-grounded rag chat ui"
git push origin codex/project-hardening-rag
```

### Task 8: Prove real semantic retrieval and backfill

**Files:**
- Test: `edu-knowledge/src/test/java/com/eduplatform/knowledge/RagEndToEndIT.java`
- Create: `edu-knowledge/src/test/resources/rag-fixtures.json`

- [ ] **Step 1: Add the end-to-end fixture**

Include two courses and a semantic query whose wording does not appear literally in the relevant chunk. Assert the expected chunk appears in top five, the other course never appears, deletion removes hits, and running rebuild twice preserves document counts.

- [ ] **Step 2: Run with real local services**

Start Docker Desktop and run:

`mvn -pl edu-knowledge -am -Dtest=RagEndToEndIT -Dollama.it.enabled=true test`

Expected: PASS with Elasticsearch and local Ollama `bge-m3:latest`.

- [ ] **Step 3: Commit evidence-ready tests**

```bash
git add edu-knowledge/src/test
git commit -m "test: verify semantic rag and reindex isolation"
git push origin codex/project-hardening-rag
```

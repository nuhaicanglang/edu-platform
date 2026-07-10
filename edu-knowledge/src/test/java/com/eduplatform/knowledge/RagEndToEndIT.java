package com.eduplatform.knowledge;

import com.eduplatform.knowledge.config.RagProperties;
import com.eduplatform.knowledge.config.RagSearchProperties;
import com.eduplatform.knowledge.embedding.OllamaEmbeddingClient;
import com.eduplatform.knowledge.search.ElasticsearchKnowledgeVectorIndex;
import com.eduplatform.knowledge.search.IndexedKnowledgeChunk;
import com.eduplatform.knowledge.search.RetrievalCandidate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 需要本机 Ollama 与 Elasticsearch 的显式启用集成测试。 */
@EnabledIfSystemProperty(named = "ollama.it.enabled", matches = "true")
class RagEndToEndIT {

    @Test
    void semanticRetrievalIsCourseIsolatedIdempotentAndDeletable() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String physicalIndex = "knowledge-chunks-it-" + suffix;
        String alias = physicalIndex + "-alias";
        RagProperties embeddingProperties = new RagProperties(
                "bge-m3:latest", 1024, "http://127.0.0.1:11434",
                Duration.ofSeconds(5), Duration.ofSeconds(120), 16, Duration.ofMinutes(30));
        RagSearchProperties searchProperties = new RagSearchProperties(
                "http://127.0.0.1:9200", physicalIndex, alias, 20, 5, 60);
        OllamaEmbeddingClient embeddingClient = new OllamaEmbeddingClient(embeddingProperties);
        ElasticsearchKnowledgeVectorIndex index = new ElasticsearchKnowledgeVectorIndex(
                searchProperties, embeddingProperties, new ObjectMapper().findAndRegisterModules());

        try {
            index.ensureIndex();
            List<float[]> vectors = embeddingClient.embedAll(List.of(
                    "二叉树前序遍历依次访问根节点、左子树和右子树",
                    "操作系统使用时间片轮转算法调度进程",
                    "遍历二叉树时按照根、左、右的顺序叫什么"));
            List<IndexedKnowledgeChunk> documents = List.of(
                    chunk("31", 7L, 12L, "数据结构基础",
                            "二叉树前序遍历依次访问根节点、左子树和右子树", vectors.get(0)),
                    chunk("41", 8L, 13L, "操作系统基础",
                            "操作系统使用时间片轮转算法调度进程", vectors.get(1)));

            index.upsertAll(documents);
            index.upsertAll(documents);

            List<RetrievalCandidate> vectorHits = index.vectorSearch(12L, vectors.get(2), 5);
            List<RetrievalCandidate> keywordHits = index.keywordSearch(12L, "根节点", 5);
            assertThat(vectorHits).extracting(RetrievalCandidate::chunkId).containsExactly("31");
            assertThat(vectorHits).allMatch(hit -> hit.courseId().equals(12L));
            assertThat(keywordHits).extracting(RetrievalCandidate::chunkId).contains("31");

            index.deleteByDocumentId(7L);
            assertThat(index.vectorSearch(12L, vectors.get(2), 5)).isEmpty();
        } finally {
            try {
                RestClient.create().delete()
                        .uri("http://127.0.0.1:9200/" + physicalIndex)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception ignored) {
                // 测试失败时尽力清理临时索引。
            }
        }
    }

    private IndexedKnowledgeChunk chunk(
            String chunkId, Long documentId, Long courseId,
            String title, String content, float[] embedding) {
        return new IndexedKnowledgeChunk(
                chunkId, documentId, courseId, 0, title, content, embedding,
                "bge-m3:latest", "test-hash-" + chunkId, Instant.now());
    }
}

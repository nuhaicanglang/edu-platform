package com.eduplatform.knowledge.search;

import com.eduplatform.knowledge.config.RagProperties;
import com.eduplatform.knowledge.config.RagSearchProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 使用 Elasticsearch HTTP API 管理版本化知识分块索引。 */
@Component
public class ElasticsearchKnowledgeVectorIndex implements KnowledgeVectorIndex {

    private static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson");

    private final RagSearchProperties searchProperties;
    private final RagProperties embeddingProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ElasticsearchKnowledgeVectorIndex(
            RagSearchProperties searchProperties,
            RagProperties embeddingProperties,
            ObjectMapper objectMapper) {
        this.searchProperties = searchProperties;
        this.embeddingProperties = embeddingProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(searchProperties.baseUrl()).build();
    }

    @Override
    public void ensureIndex() {
        boolean exists;
        try {
            restClient.head().uri("/" + searchProperties.physicalIndex()).retrieve().toBodilessEntity();
            exists = true;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 404) throw e;
            exists = false;
        }

        if (!exists) {
            restClient.put()
                    .uri("/" + searchProperties.physicalIndex())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(indexDefinition())
                    .retrieve()
                    .toBodilessEntity();
        }
        restClient.put()
                .uri("/" + searchProperties.physicalIndex() + "/_alias/" + searchProperties.alias())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void upsertAll(List<IndexedKnowledgeChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return;
        StringBuilder bulk = new StringBuilder();
        try {
            for (IndexedKnowledgeChunk chunk : chunks) {
                bulk.append(objectMapper.writeValueAsString(Map.of(
                        "index", Map.of("_index", searchProperties.alias(), "_id", chunk.chunkId()))))
                        .append('\n');
                bulk.append(objectMapper.writeValueAsString(toDocument(chunk))).append('\n');
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("构建 Elasticsearch 批量请求失败", e);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/_bulk")
                .contentType(NDJSON)
                .body(bulk.toString().getBytes(StandardCharsets.UTF_8))
                .retrieve()
                .body(Map.class);
        if (response != null && Boolean.TRUE.equals(response.get("errors"))) {
            throw new IllegalStateException("Elasticsearch 批量写入包含失败项");
        }
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        Map<String, Object> query = Map.of(
                "query", Map.of("term", Map.of("documentId", String.valueOf(documentId))));
        restClient.post()
                .uri("/" + searchProperties.alias() + "/_delete_by_query?refresh=true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(query)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<RetrievalCandidate> vectorSearch(Long courseId, float[] queryVector, int limit) {
        if (queryVector == null || queryVector.length != embeddingProperties.dimensions()) {
            throw new IllegalArgumentException("查询向量维度错误");
        }
        Map<String, Object> knn = new LinkedHashMap<>();
        knn.put("field", "embedding");
        knn.put("query_vector", queryVector);
        knn.put("k", limit);
        knn.put("num_candidates", Math.max(limit, searchProperties.candidateCount()));
        knn.put("filter", Map.of("term", Map.of("courseId", String.valueOf(courseId))));
        return search(Map.of("size", limit, "knn", knn));
    }

    @Override
    public List<RetrievalCandidate> keywordSearch(Long courseId, String query, int limit) {
        Map<String, Object> bool = Map.of(
                "filter", List.of(Map.of("term", Map.of("courseId", String.valueOf(courseId)))),
                "must", List.of(Map.of("match", Map.of("content", query))));
        return search(Map.of("size", limit, "query", Map.of("bool", bool)));
    }

    @SuppressWarnings("unchecked")
    private List<RetrievalCandidate> search(Map<String, Object> body) {
        Map<String, Object> response = restClient.post()
                .uri("/" + searchProperties.alias() + "/_search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
        if (response == null) return List.of();
        Map<String, Object> hitsWrapper = (Map<String, Object>) response.get("hits");
        if (hitsWrapper == null) return List.of();
        List<Map<String, Object>> hits = (List<Map<String, Object>>) hitsWrapper.get("hits");
        if (hits == null) return List.of();

        List<RetrievalCandidate> result = new ArrayList<>(hits.size());
        for (Map<String, Object> hit : hits) {
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");
            if (source == null) continue;
            Number score = (Number) hit.get("_score");
            result.add(new RetrievalCandidate(
                    String.valueOf(source.get("chunkId")),
                    Long.valueOf(String.valueOf(source.get("documentId"))),
                    Long.valueOf(String.valueOf(source.get("courseId"))),
                    ((Number) source.get("chunkIndex")).intValue(),
                    String.valueOf(source.get("title")),
                    String.valueOf(source.get("content")),
                    score == null ? 0d : score.doubleValue()));
        }
        return result;
    }

    private Map<String, Object> toDocument(IndexedKnowledgeChunk chunk) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("chunkId", chunk.chunkId());
        document.put("documentId", String.valueOf(chunk.documentId()));
        document.put("courseId", String.valueOf(chunk.courseId()));
        document.put("chunkIndex", chunk.chunkIndex());
        document.put("title", chunk.title());
        document.put("content", chunk.content());
        document.put("embedding", chunk.embedding());
        document.put("embeddingModel", chunk.embeddingModel());
        document.put("contentHash", chunk.contentHash());
        document.put("updatedAt", (chunk.updatedAt() == null ? Instant.now() : chunk.updatedAt()).toString());
        return document;
    }

    private Map<String, Object> indexDefinition() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("chunkId", Map.of("type", "keyword"));
        properties.put("documentId", Map.of("type", "keyword"));
        properties.put("courseId", Map.of("type", "keyword"));
        properties.put("chunkIndex", Map.of("type", "integer"));
        properties.put("title", Map.of(
                "type", "text",
                "fields", Map.of("keyword", Map.of("type", "keyword"))));
        properties.put("content", Map.of("type", "text"));
        properties.put("embedding", Map.of(
                "type", "dense_vector",
                "dims", embeddingProperties.dimensions(),
                "index", true,
                "similarity", "cosine"));
        properties.put("embeddingModel", Map.of("type", "keyword"));
        properties.put("contentHash", Map.of("type", "keyword"));
        properties.put("updatedAt", Map.of("type", "date"));
        return Map.of("mappings", Map.of("properties", properties));
    }
}

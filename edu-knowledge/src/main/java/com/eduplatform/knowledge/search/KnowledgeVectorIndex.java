package com.eduplatform.knowledge.search;

import java.util.List;

/** Elasticsearch 知识分块投影网关。 */
public interface KnowledgeVectorIndex {

    void ensureIndex();

    void upsertAll(List<IndexedKnowledgeChunk> chunks);

    void deleteByDocumentId(Long documentId);

    List<RetrievalCandidate> vectorSearch(Long courseId, float[] queryVector, int limit);

    List<RetrievalCandidate> keywordSearch(Long courseId, String query, int limit);
}

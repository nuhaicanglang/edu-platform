package com.eduplatform.knowledge.search;

import java.time.Instant;

/** Elasticsearch 中可重建的知识分块投影。 */
public record IndexedKnowledgeChunk(
        String chunkId,
        Long documentId,
        Long courseId,
        int chunkIndex,
        String title,
        String content,
        float[] embedding,
        String embeddingModel,
        String contentHash,
        Instant updatedAt) {
}

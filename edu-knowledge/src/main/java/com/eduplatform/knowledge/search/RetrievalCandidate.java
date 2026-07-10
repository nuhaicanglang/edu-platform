package com.eduplatform.knowledge.search;

/** 单路检索返回的候选分块。 */
public record RetrievalCandidate(
        String chunkId,
        Long documentId,
        Long courseId,
        int chunkIndex,
        String title,
        String content,
        double score) {
}

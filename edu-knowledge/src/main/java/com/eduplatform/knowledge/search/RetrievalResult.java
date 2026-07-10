package com.eduplatform.knowledge.search;

/** RRF 融合后的知识分块及服务端来源元数据。 */
public record RetrievalResult(
        String chunkId,
        Long documentId,
        Long courseId,
        int chunkIndex,
        String title,
        String content,
        double score) {
}

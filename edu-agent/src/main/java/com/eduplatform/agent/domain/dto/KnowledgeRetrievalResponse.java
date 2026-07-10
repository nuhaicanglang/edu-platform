package com.eduplatform.agent.domain.dto;

import java.util.List;

/** 知识服务返回的混合检索结果。 */
public record KnowledgeRetrievalResponse(String retrievalMode, List<Source> sources) {

    public record Source(
            String chunkId,
            Long documentId,
            Long courseId,
            int chunkIndex,
            String title,
            String content,
            double score) {
    }
}

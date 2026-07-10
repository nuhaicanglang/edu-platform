package com.eduplatform.agent.domain.dto;

import java.util.List;

/** 基于课程资料生成的回答及服务端可信来源。 */
public record RagAnswer(String answer, String retrievalMode, List<Source> sources) {

    public record Source(
            Long documentId,
            String documentTitle,
            String chunkId,
            int chunkIndex,
            double score) {
    }
}

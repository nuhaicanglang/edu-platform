package com.eduplatform.knowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Elasticsearch 混合检索配置。 */
@ConfigurationProperties(prefix = "rag.search")
public record RagSearchProperties(
        String baseUrl,
        String physicalIndex,
        String alias,
        int candidateCount,
        int topK,
        int rrfConstant) {

    public RagSearchProperties {
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "http://127.0.0.1:9200";
        if (physicalIndex == null || physicalIndex.isBlank()) physicalIndex = "knowledge-chunks-v1";
        if (alias == null || alias.isBlank()) alias = "knowledge-chunks";
        if (candidateCount <= 0) candidateCount = 20;
        if (topK <= 0) topK = 5;
        if (rrfConstant <= 0) rrfConstant = 60;
    }
}

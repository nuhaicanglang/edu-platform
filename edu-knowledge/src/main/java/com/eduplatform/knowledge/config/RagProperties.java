package com.eduplatform.knowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Ollama 向量化配置。 */
@ConfigurationProperties(prefix = "rag.embedding")
public record RagProperties(
        String model,
        int dimensions,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        int batchSize,
        Duration keepAlive) {

    public RagProperties {
        if (model == null || model.isBlank()) model = "bge-m3:latest";
        if (dimensions <= 0) dimensions = 1024;
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "http://127.0.0.1:11434";
        if (connectTimeout == null) connectTimeout = Duration.ofSeconds(5);
        if (readTimeout == null) readTimeout = Duration.ofSeconds(120);
        if (batchSize <= 0) batchSize = 16;
        if (keepAlive == null) keepAlive = Duration.ofMinutes(30);
    }
}

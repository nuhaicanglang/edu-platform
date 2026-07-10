package com.eduplatform.knowledge.embedding;

/** Ollama 不可用或返回无效向量时抛出。 */
public class EmbeddingUnavailableException extends RuntimeException {

    public EmbeddingUnavailableException(String message) {
        super(message);
    }

    public EmbeddingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

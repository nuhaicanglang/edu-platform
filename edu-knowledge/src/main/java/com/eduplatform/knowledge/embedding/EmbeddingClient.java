package com.eduplatform.knowledge.embedding;

import java.util.List;

/** 文本向量化客户端契约。 */
public interface EmbeddingClient {

    List<float[]> embedAll(List<String> texts);

    String modelName();

    int dimensions();
}

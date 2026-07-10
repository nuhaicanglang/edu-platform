package com.eduplatform.knowledge.embedding;

import com.eduplatform.knowledge.config.RagProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 通过 Ollama `/api/embed` 批量生成文本向量。 */
@Component
public class OllamaEmbeddingClient implements EmbeddingClient {

    private final RagProperties properties;
    private final RestClient restClient;

    public OllamaEmbeddingClient(RagProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("向量化文本不能为空");
        }
        if (texts.stream().anyMatch(text -> text == null || text.isBlank())) {
            throw new IllegalArgumentException("向量化文本不能包含空值");
        }

        List<float[]> result = new ArrayList<>(texts.size());
        for (int start = 0; start < texts.size(); start += properties.batchSize()) {
            int end = Math.min(start + properties.batchSize(), texts.size());
            result.addAll(embedBatch(texts.subList(start, end)));
        }
        return result;
    }

    private List<float[]> embedBatch(List<String> batch) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        body.put("input", batch);
        body.put("keep_alive", properties.keepAlive().toSeconds() + "s");

        final EmbedResponse response;
        try {
            response = restClient.post()
                    .uri("/api/embed")
                    .body(body)
                    .retrieve()
                    .body(EmbedResponse.class);
        } catch (RestClientException e) {
            throw new EmbeddingUnavailableException("Ollama 向量服务不可用", e);
        }

        if (response == null || response.embeddings() == null
                || response.embeddings().size() != batch.size()) {
            throw new EmbeddingUnavailableException("Ollama 返回的向量数量与请求不一致");
        }

        List<float[]> vectors = new ArrayList<>(batch.size());
        for (List<Float> embedding : response.embeddings()) {
            if (embedding == null || embedding.size() != properties.dimensions()) {
                int actual = embedding == null ? 0 : embedding.size();
                throw new EmbeddingUnavailableException(
                        "Ollama 返回的向量维度错误，期望 " + properties.dimensions() + "，实际 " + actual);
            }
            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i);
            }
            vectors.add(vector);
        }
        return vectors;
    }

    @Override
    public String modelName() {
        return properties.model();
    }

    @Override
    public int dimensions() {
        return properties.dimensions();
    }

    private record EmbedResponse(String model, List<List<Float>> embeddings) {}
}

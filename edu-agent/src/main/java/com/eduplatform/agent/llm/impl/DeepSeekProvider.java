package com.eduplatform.agent.llm.impl;

import com.eduplatform.agent.llm.LlmMessage;
import com.eduplatform.agent.llm.LlmProvider;
import com.eduplatform.agent.llm.LlmRequest;
import com.eduplatform.agent.llm.LlmResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek LLM 提供者
 * <p>
 * 当配置 {@code llm.provider=deepseek} 时激活。调用 DeepSeek Chat Completions API。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "deepseek")
public class DeepSeekProvider implements LlmProvider {

    @Value("${llm.deepseek.api-key:sk-xxx}")
    private String apiKey;

    @Value("${llm.deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${llm.deepseek.model:deepseek-chat}")
    private String defaultModel;

    private final RestTemplate restTemplate = buildRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);   // 10s 连接超时
        factory.setReadTimeout(180_000);     // 180s 读取超时（试卷生成耗时较长）
        return new RestTemplate(factory);
    }

    @Override
    public String getName() {
        return "deepseek";
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        try {
            String model = request.getModel() != null ? request.getModel() : defaultModel;

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", request.getTemperature());
            body.put("max_tokens", request.getMaxTokens());

            List<Map<String, String>> messages = new ArrayList<>();
            for (LlmMessage msg : request.getMessages()) {
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent());
                messages.add(m);
            }
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String url = baseUrl + "/chat/completions";
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            LlmResponse response = LlmResponse.success(content);
            response.setModel(model);

            JsonNode usage = root.path("usage");
            if (!usage.isMissingNode()) {
                response.setPromptTokens(usage.path("prompt_tokens").asInt());
                response.setCompletionTokens(usage.path("completion_tokens").asInt());
                response.setTotalTokens(usage.path("total_tokens").asInt());
            }

            return response;
        } catch (Exception e) {
            log.error("DeepSeek API调用失败: {}", e.getMessage(), e);
            return LlmResponse.error("LLM调用失败: " + e.getMessage());
        }
    }
}

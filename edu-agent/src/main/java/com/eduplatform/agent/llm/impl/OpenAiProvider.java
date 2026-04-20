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
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * OpenAI 兼容接口 LLM 提供者
 * <p>
 * 默认激活（matchIfMissing=true），支持 OpenAI 及其兼容 API（如 DeepSeek 等）。
 * 通过 {@code llm.openai.base-url} 配置切换到不同的兼容服务。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiProvider implements LlmProvider {

    @Value("${llm.openai.api-key:sk-xxx}")
    private String apiKey;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${llm.openai.model:gpt-4o}")
    private String defaultModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "openai";
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
            log.error("OpenAI API调用失败: {}", e.getMessage(), e);
            return LlmResponse.error("LLM调用失败: " + e.getMessage());
        }
    }
}

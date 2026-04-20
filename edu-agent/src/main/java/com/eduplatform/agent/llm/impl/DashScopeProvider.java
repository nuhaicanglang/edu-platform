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
 * 阿里通义千问（DashScope）LLM 提供者
 * <p>
 * 当配置 {@code llm.provider=dashscope} 时激活。调用阿里云 DashScope API。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "dashscope")
public class DashScopeProvider implements LlmProvider {

    @Value("${llm.dashscope.api-key:sk-xxx}")
    private String apiKey;

    @Value("${llm.dashscope.model:qwen-max}")
    private String defaultModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    @Override
    public String getName() {
        return "dashscope";
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

            String url = BASE_URL + "/chat/completions";
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            LlmResponse response = LlmResponse.success(content);
            response.setModel(model);
            return response;
        } catch (Exception e) {
            log.error("DashScope API调用失败: {}", e.getMessage(), e);
            return LlmResponse.error("LLM调用失败: " + e.getMessage());
        }
    }
}

package com.eduplatform.system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 服务 HTTP 客户端
 * <p>
 * 通过 RestTemplate 调用 edu-agent 的 AI 批改接口（/agent/grading/text）。
 * 复用单例 RestTemplate 实例以避免每次调用创建新连接。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentClientService {

    @Value("${service.agent.url:http://localhost:8083}")
    private String agentBaseUrl;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用Agent服务进行文本作业批改
     * @return 批改结果JSON字符串
     */
    public String gradeTextAssignment(String assignmentTitle, String requirement,
                                       String studentAnswer, String referenceAnswer) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("assignmentTitle", assignmentTitle);
            body.put("assignmentRequirement", requirement != null ? requirement : "");
            body.put("studentAnswer", studentAnswer != null ? studentAnswer : "");
            body.put("referenceAnswer", referenceAnswer != null ? referenceAnswer : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    agentBaseUrl + "/agent/grading/text", entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("data").asText(response.getBody());
            }
            throw new RuntimeException("Agent服务响应异常: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("调用Agent批改服务失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI批改服务调用失败: " + e.getMessage());
        }
    }
}

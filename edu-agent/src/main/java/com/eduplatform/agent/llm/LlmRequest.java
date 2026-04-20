package com.eduplatform.agent.llm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM请求
 */
@Data
public class LlmRequest {

    private List<LlmMessage> messages = new ArrayList<>();
    private String model;
    private Double temperature = 0.7;
    private Integer maxTokens = 4096;
    private Boolean stream = false;

    public LlmRequest addMessage(LlmMessage message) {
        this.messages.add(message);
        return this;
    }

    public LlmRequest addSystemMessage(String content) {
        this.messages.add(LlmMessage.system(content));
        return this;
    }

    public LlmRequest addUserMessage(String content) {
        this.messages.add(LlmMessage.user(content));
        return this;
    }

    public static LlmRequest of(String systemPrompt, String userMessage) {
        LlmRequest request = new LlmRequest();
        request.addSystemMessage(systemPrompt);
        request.addUserMessage(userMessage);
        return request;
    }
}

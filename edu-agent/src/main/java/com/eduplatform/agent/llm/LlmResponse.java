package com.eduplatform.agent.llm;

import lombok.Data;

/**
 * LLM响应
 */
@Data
public class LlmResponse {

    private String content;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private boolean success;
    private String errorMessage;

    public static LlmResponse success(String content) {
        LlmResponse response = new LlmResponse();
        response.setContent(content);
        response.setSuccess(true);
        return response;
    }

    public static LlmResponse error(String message) {
        LlmResponse response = new LlmResponse();
        response.setSuccess(false);
        response.setErrorMessage(message);
        return response;
    }
}

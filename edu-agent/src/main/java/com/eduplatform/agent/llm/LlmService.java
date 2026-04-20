package com.eduplatform.agent.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM服务 - 封装统一调用入口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmProvider llmProvider;

    /**
     * 通用对话
     */
    public LlmResponse chat(LlmRequest request) {
        log.debug("调用LLM [{}], 消息数: {}", llmProvider.getName(), request.getMessages().size());
        LlmResponse response = llmProvider.chat(request);
        if (response.isSuccess()) {
            log.debug("LLM响应成功, tokens: {}", response.getTotalTokens());
        } else {
            log.error("LLM响应失败: {}", response.getErrorMessage());
        }
        return response;
    }

    /**
     * 简单单轮对话
     */
    public String chatSimple(String systemPrompt, String userMessage) {
        LlmResponse response = chat(LlmRequest.of(systemPrompt, userMessage));
        if (response.isSuccess()) {
            return response.getContent();
        }
        throw new RuntimeException("LLM调用失败: " + response.getErrorMessage());
    }

    /**
     * 获取当前LLM提供者名称
     */
    public String getProviderName() {
        return llmProvider.getName();
    }
}

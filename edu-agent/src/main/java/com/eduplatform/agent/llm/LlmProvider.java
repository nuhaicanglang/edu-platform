package com.eduplatform.agent.llm;

/**
 * LLM 提供者统一接口
 * <p>
 * 定义 LLM 调用规范，通过 {@code @ConditionalOnProperty} 实现多提供者切换。
 * 当前实现：{@code DeepSeekProvider}、{@code DashScopeProvider}、{@code OpenAiProvider}。
 * </p>
 */
public interface LlmProvider {

    /**
     * 获取提供者名称
     */
    String getName();

    /**
     * 同步调用LLM
     */
    LlmResponse chat(LlmRequest request);

    /**
     * 简单对话（单轮）
     */
    default LlmResponse chat(String systemPrompt, String userMessage) {
        return chat(LlmRequest.of(systemPrompt, userMessage));
    }
}

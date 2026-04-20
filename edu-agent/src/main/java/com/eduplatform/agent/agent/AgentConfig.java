package com.eduplatform.agent.agent;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j Agent 配置
 * <p>
 * 将 OpenAiChatModel（兼容 DeepSeek）、AgentTools 工具集、对话记忆窗口
 * 组装为 {@link EduAgent} 代理 Bean，供 Service 层注入使用。
 * </p>
 */
@Configuration
public class AgentConfig {

    @Value("${llm.deepseek.api-key:sk-xxx}")
    private String apiKey;

    @Value("${llm.deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${llm.deepseek.model:deepseek-chat}")
    private String model;

    /**
     * 使用 DeepSeek OpenAI 兼容接口创建 ChatModel
     */
    @Bean("agentChatModel")
    public OpenAiChatModel agentChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(model)
                .temperature(0.7)
                .maxTokens(4096)
                .timeout(Duration.ofSeconds(120))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    /**
     * 组装 EduAgent：ChatModel + 工具集 + 每用户独立记忆（窗口20条）
     */
    @Bean
    public EduAgent eduAgent(OpenAiChatModel agentChatModel, AgentTools agentTools) {
        return AiServices.builder(EduAgent.class)
                .chatLanguageModel(agentChatModel)
                .tools(agentTools)
                .chatMemoryProvider(
                        memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}

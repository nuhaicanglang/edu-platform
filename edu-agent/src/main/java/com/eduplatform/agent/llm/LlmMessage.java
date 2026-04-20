package com.eduplatform.agent.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM对话消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {

    /** 角色: system, user, assistant */
    private String role;

    /** 消息内容 */
    private String content;

    public static LlmMessage system(String content) {
        return new LlmMessage("system", content);
    }

    public static LlmMessage user(String content) {
        return new LlmMessage("user", content);
    }

    public static LlmMessage assistant(String content) {
        return new LlmMessage("assistant", content);
    }
}

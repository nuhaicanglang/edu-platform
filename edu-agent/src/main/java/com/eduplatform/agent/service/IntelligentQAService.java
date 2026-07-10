package com.eduplatform.agent.service;

import com.eduplatform.agent.llm.LlmRequest;
import com.eduplatform.agent.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 智能问答服务 - 基于课程知识的上下文感知问答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentQAService {

    private final LlmService llmService;
    private final StringRedisTemplate redisTemplate;

    @Value("${qa.history.max-characters:2000}")
    private int historyMaxCharacters = 2000;

    @Value("${qa.history.ttl:30m}")
    private Duration historyTtl = Duration.ofMinutes(30);

    private static final String SYSTEM_PROMPT = """
            你是一个专业的教学助手AI Agent，专注于为学生提供高质量的学习辅导。
            
            你的职责：
            1. 根据课程知识点，准确回答学生的学习问题
            2. 使用启发式教学方法，引导学生思考
            3. 当学生的问题涉及多个知识点时，进行关联分析
            4. 给出清晰、结构化的回答，必要时使用示例说明
            5. 对于超出课程范围的问题，明确指出并给出学习建议
            
            注意事项：
            - 回答要专业、准确、易懂
            - 适当使用markdown格式增强可读性
            - 鼓励学生深入思考，不直接给出完整答案（除非明确要求）
            - 回答使用中文
            """;

    /**
     * 智能问答（带课程上下文）
     */
    public String ask(String question, String courseContext, Long courseId, Long userId) {
        LlmRequest request = new LlmRequest();
        request.addSystemMessage(SYSTEM_PROMPT);

        if (courseContext != null && !courseContext.isEmpty()) {
            request.addSystemMessage("以下是当前课程的知识背景：\n" + courseContext);
        }

        // 加载对话历史
        String historyKey = "qa:history:" + userId + ":course:"
                + (courseId == null ? "general" : courseId);
        String history = loadHistory(historyKey);
        if (history != null && !history.isEmpty()) {
            request.addSystemMessage("以下是之前的对话记录摘要：\n" + history);
        }

        request.addUserMessage(question);
        String answer = llmService.chatSimple(SYSTEM_PROMPT, buildFullPrompt(question, courseContext, history));

        // 保存对话摘要到Redis（保留最近5轮）
        String newHistory = (history != null ? history + "\n" : "") +
                "Q: " + truncate(question, 100) + "\nA: " + truncate(answer, 200);
        saveHistory(historyKey, truncateLast(newHistory, historyMaxCharacters));

        return answer;
    }

    /**
     * 简单问答（无上下文）
     */
    public String askSimple(String question) {
        return llmService.chatSimple(SYSTEM_PROMPT, question);
    }

    /**
     * 知识点解释
     */
    public String explainKnowledgePoint(String knowledgePoint, String courseName) {
        String prompt = String.format(
                "请详细解释以下知识点，课程：%s\n\n知识点：%s\n\n" +
                "请从以下几个方面进行解释：\n" +
                "1. 基本概念和定义\n" +
                "2. 核心原理\n" +
                "3. 实际应用场景\n" +
                "4. 与其他知识点的关联\n" +
                "5. 常见考点和易错点",
                courseName, knowledgePoint);
        return llmService.chatSimple(SYSTEM_PROMPT, prompt);
    }

    private String buildFullPrompt(String question, String courseContext, String history) {
        StringBuilder sb = new StringBuilder();
        if (courseContext != null && !courseContext.isEmpty()) {
            sb.append("【课程知识背景】\n").append(courseContext).append("\n\n");
        }
        if (history != null && !history.isEmpty()) {
            sb.append("【对话历史】\n").append(history).append("\n\n");
        }
        sb.append("【学生问题】\n").append(question);
        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    private String truncateLast(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(text.length() - maxLen) : text;
    }

    private String loadHistory(String historyKey) {
        try {
            return redisTemplate.opsForValue().get(historyKey);
        } catch (DataAccessException e) {
            log.warn("[QA] Redis 历史读取失败，按空历史继续 key={}: {}", historyKey, e.getMessage());
            return null;
        }
    }

    private void saveHistory(String historyKey, String history) {
        try {
            redisTemplate.opsForValue().set(historyKey, history, historyTtl);
        } catch (DataAccessException e) {
            log.warn("[QA] Redis 历史写入失败，回答结果仍正常返回 key={}: {}", historyKey, e.getMessage());
        }
    }
}

package com.eduplatform.agent.service;

import com.eduplatform.agent.llm.LlmRequest;
import com.eduplatform.agent.llm.LlmResponse;
import com.eduplatform.agent.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 增量式个性化练习生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeGenerationService {

    private final LlmService llmService;

    private static final String GENERATION_PROMPT = """
            你是一个专业的教学出题专家。请根据要求生成高质量的练习题目。
            
            出题原则：
            1. 题目紧密围绕指定知识点
            2. 难度适当，符合学生当前水平
            3. 题型多样化（选择题、填空题、简答题、编程题等）
            4. 每道题附带标准答案和详细解析
            5. 标注相关知识点和难度等级
            
            输出格式（JSON数组）：
            [
              {
                "id": 1,
                "type": "choice/fill/short_answer/code",
                "difficulty": "easy/medium/hard",
                "knowledgePoints": ["知识点1", "知识点2"],
                "question": "题目内容",
                "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
                "answer": "正确答案",
                "explanation": "详细解析",
                "score": 10
              }
            ]
            请确保输出合法的JSON数组格式。
            """;

    /**
     * 根据知识点生成练习题
     */
    public String generateByKnowledgePoints(String courseName, List<String> knowledgePoints,
                                             String difficulty, int count) {
        String prompt = String.format("""
                课程：%s
                目标知识点：%s
                难度要求：%s
                题目数量：%d
                
                请生成符合要求的练习题。
                """,
                courseName,
                String.join("、", knowledgePoints),
                mapDifficulty(difficulty),
                count);

        return llmService.chatSimple(GENERATION_PROMPT, prompt);
    }

    /**
     * 根据学生薄弱知识点生成个性化练习
     */
    public String generatePersonalized(String courseName, String studentName,
                                        List<String> weakPoints, List<String> masteredPoints,
                                        int count) {
        String prompt = String.format("""
                课程：%s
                学生：%s
                
                学生已掌握的知识点：%s
                学生薄弱的知识点：%s
                
                请针对薄弱知识点生成%d道个性化练习题。
                要求：
                1. 重点针对薄弱知识点，约70%%的题目覆盖薄弱点
                2. 30%%的题目用于巩固已掌握知识与薄弱点的关联
                3. 难度递进，从基础到进阶
                4. 每道题的解析要详细，帮助学生理解
                """,
                courseName, studentName,
                String.join("、", masteredPoints),
                String.join("、", weakPoints),
                count);

        return llmService.chatSimple(GENERATION_PROMPT, prompt);
    }

    /**
     * 生成考前模拟卷
     * 试卷内容较多，使用 8192 max_tokens 避免截断
     */
    public String generateExamPaper(String courseName, List<String> allKnowledgePoints,
                                     int totalScore, String examType) {
        String prompt = String.format("""
                课程：%s
                考试类型：%s
                总分：%d分
                覆盖知识点：%s
                
                请生成一份完整的模拟试卷，包含：
                1. 选择题（约30%%分值）
                2. 填空题（约20%%分值）
                3. 简答题（约30%%分值）
                4. 综合题/编程题（约20%%分值）
                
                每道题请标注分值和知识点。
                """,
                courseName, examType, totalScore,
                String.join("、", allKnowledgePoints));

        LlmRequest request = LlmRequest.of(GENERATION_PROMPT, prompt);
        request.setMaxTokens(8192);
        LlmResponse response = llmService.chat(request);
        if (response.isSuccess()) {
            return response.getContent();
        }
        throw new RuntimeException("试卷生成失败: " + response.getErrorMessage());
    }

    private String mapDifficulty(String difficulty) {
        if (difficulty == null) return "中等";
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "简单（基础概念和直接应用）";
            case "hard" -> "困难（综合应用和创新思维）";
            default -> "中等（理解分析和一般应用）";
        };
    }
}

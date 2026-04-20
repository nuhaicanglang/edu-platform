package com.eduplatform.agent.service;

import com.eduplatform.agent.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 作业智能批改服务 — 细粒度批注引擎
 * <p>
 * 通过 LLM 对文本作业和编程作业进行逐项评分，输出标准化 JSON 格式的批改结果，
 * 包含 totalScore、overallComment、逐项批注 annotations、知识点掌握度和改进建议。
 * 由 edu-system 的 AsyncGradingService 异步调用。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentGradingService {

    private final LlmService llmService;

    private static final String GRADING_PROMPT = """
            你是一个严谨的教学评审专家。请对学生提交的作业进行细粒度批改。
            
            批改要求：
            1. 对每个题目/段落进行逐项评分
            2. 指出具体的错误位置和错误类型
            3. 给出详细的修改建议和正确答案
            4. 标注知识点掌握情况
            5. 给出总体评价和改进建议
            
            输出格式要求（JSON）：
            {
              "totalScore": 85,
              "maxScore": 100,
              "overallComment": "总体评价...",
              "annotations": [
                {
                  "position": "第1题",
                  "originalText": "学生的回答",
                  "errorType": "概念错误/计算错误/逻辑错误/表述不清/正确",
                  "comment": "批注内容",
                  "suggestion": "修改建议",
                  "score": 8,
                  "maxScore": 10,
                  "knowledgePoint": "相关知识点"
                }
              ],
              "knowledgeSummary": {
                "mastered": ["已掌握的知识点"],
                "needImprovement": ["需要加强的知识点"],
                "notGrasped": ["未掌握的知识点"]
              },
              "improvementPlan": "个性化改进建议..."
            }
            请确保输出是合法的JSON格式。
            """;

    /**
     * 智能批改文本作业
     */
    public String gradeTextAssignment(String assignmentTitle, String assignmentRequirement,
                                       String studentAnswer, String referenceAnswer) {
        String prompt = String.format(
                "作业标题：%s\n\n作业要求：%s\n\n参考答案：%s\n\n学生提交的答案：\n%s\n\n请进行细粒度批改。",
                assignmentTitle, assignmentRequirement,
                referenceAnswer != null ? referenceAnswer : "无标准答案，请根据专业知识评判",
                studentAnswer);

        return llmService.chatSimple(GRADING_PROMPT, prompt);
    }

    /**
     * 批改编程作业
     */
    public String gradeCodeAssignment(String assignmentTitle, String requirement,
                                       String studentCode, String testCases) {
        String codeGradingPrompt = GRADING_PROMPT + """
                
                额外编程作业批改要求：
                1. 检查代码的正确性、可读性、效率
                2. 指出潜在的bug和边界情况处理
                3. 评估代码风格和最佳实践
                4. 给出性能优化建议
                """;

        String prompt = String.format(
                "编程作业：%s\n\n要求：%s\n\n测试用例：%s\n\n学生代码：\n```\n%s\n```\n\n请进行批改。",
                assignmentTitle, requirement,
                testCases != null ? testCases : "无",
                studentCode);

        return llmService.chatSimple(codeGradingPrompt, prompt);
    }

    /**
     * 批量批改摘要（用于教师查看全班情况）
     */
    public String generateClassGradingSummary(String assignmentTitle, List<String> gradingResults) {
        String summaryPrompt = """
                你是一个教学数据分析专家。请根据以下全班作业批改结果，生成班级学情分析报告。
                
                报告应包含：
                1. 总体成绩分布（优秀/良好/及格/不及格比例）
                2. 常见错误类型统计
                3. 知识点掌握热力图（哪些知识点普遍掌握好，哪些薄弱）
                4. 典型错误案例分析
                5. 教学建议
                
                输出使用中文markdown格式。
                """;

        StringBuilder sb = new StringBuilder();
        sb.append("作业标题：").append(assignmentTitle).append("\n\n");
        sb.append("批改结果汇总（共").append(gradingResults.size()).append("份）：\n\n");
        for (int i = 0; i < Math.min(gradingResults.size(), 50); i++) {
            sb.append("--- 学生").append(i + 1).append(" ---\n");
            sb.append(gradingResults.get(i)).append("\n\n");
        }

        return llmService.chatSimple(summaryPrompt, sb.toString());
    }
}

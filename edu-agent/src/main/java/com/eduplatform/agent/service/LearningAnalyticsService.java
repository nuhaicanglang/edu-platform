package com.eduplatform.agent.service;

import com.eduplatform.agent.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 学情分析服务 - 多维度学习数据分析与画像构建
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningAnalyticsService {

    private final LlmService llmService;

    private static final String ANALYTICS_PROMPT = """
            你是一个教育数据分析专家，专注于学生学情分析和学习画像构建。
            请基于提供的学习数据，进行多维度分析。
            输出使用中文，格式为markdown。
            """;

    /**
     * 生成学生个人学情报告
     */
    public String generateStudentReport(Long studentId, String studentName,
                                         String courseName, Map<String, Object> learningData) {
        String prompt = String.format("""
                请为以下学生生成个人学情分析报告：
                
                学生：%s
                课程：%s
                
                学习数据：
                %s
                
                请从以下维度进行分析：
                1. **学习进度** - 整体课程完成情况
                2. **知识掌握度** - 各知识点掌握情况评估
                3. **学习能力画像** - 学习风格、强弱项分析
                4. **趋势分析** - 成绩变化趋势、学习投入趋势
                5. **薄弱环节** - 需要重点加强的知识点
                6. **个性化建议** - 针对性的学习改进方案
                7. **预警信息** - 可能存在的学习风险预警
                """,
                studentName, courseName, formatLearningData(learningData));

        return llmService.chatSimple(ANALYTICS_PROMPT, prompt);
    }

    /**
     * 生成班级学情概览
     */
    public String generateClassOverview(String className, String courseName,
                                         Map<String, Object> classData) {
        String prompt = String.format("""
                请为以下班级生成学情概览报告：
                
                班级：%s
                课程：%s
                
                班级数据：
                %s
                
                请从以下维度分析：
                1. 成绩分布与统计
                2. 知识点掌握情况热力图
                3. 学习参与度分析
                4. 优秀学生与需关注学生
                5. 班级整体趋势
                6. 教学建议
                """,
                className, courseName, formatLearningData(classData));

        return llmService.chatSimple(ANALYTICS_PROMPT, prompt);
    }

    /**
     * 知识图谱掌握度分析
     */
    public String analyzeKnowledgeGraph(String courseName, List<String> knowledgePoints,
                                         Map<String, Double> masteryScores) {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("课程：").append(courseName).append("\n\n知识点掌握情况：\n");
        for (String kp : knowledgePoints) {
            Double score = masteryScores.getOrDefault(kp, 0.0);
            dataBuilder.append("- ").append(kp).append(": ").append(String.format("%.1f%%", score * 100)).append("\n");
        }

        String prompt = dataBuilder.toString() + "\n请分析知识图谱掌握情况，标识薄弱环节和前置知识依赖关系，给出学习路径建议。";

        return llmService.chatSimple(ANALYTICS_PROMPT, prompt);
    }

    /**
     * 学习预警分析
     */
    public String analyzeRisk(String studentName, Map<String, Object> riskData) {
        String prompt = String.format("""
                请对以下学生进行学习风险评估：
                
                学生：%s
                
                风险指标数据：
                %s
                
                请评估：
                1. 风险等级（高/中/低）
                2. 风险因素分析
                3. 干预建议
                4. 预期改善措施
                """,
                studentName, formatLearningData(riskData));

        return llmService.chatSimple(ANALYTICS_PROMPT, prompt);
    }

    private String formatLearningData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return "暂无数据";
        StringBuilder sb = new StringBuilder();
        data.forEach((key, value) -> sb.append("- ").append(key).append(": ").append(value).append("\n"));
        return sb.toString();
    }
}

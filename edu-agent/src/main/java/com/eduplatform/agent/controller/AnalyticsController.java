package com.eduplatform.agent.controller;

import com.eduplatform.agent.service.LearningAnalyticsService;
import com.eduplatform.common.core.domain.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 学情分析控制器
 * <p>
 * 提供基于 LLM 的学生报告、班级概览、知识图谱分析、学习预警等接口。
 * 前端将 DB 真实数据传入，AI 生成分析报告。
 * </p>
 */
@RestController
@RequestMapping("/agent/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final LearningAnalyticsService analyticsService;

    /** 生成学生个人学情报告 */
    @PostMapping("/student-report")
    public R<String> studentReport(@RequestBody StudentReportRequest request) {
        String report = analyticsService.generateStudentReport(
                request.getStudentId(), request.getStudentName(),
                request.getCourseName(), request.getLearningData());
        return R.ok(report);
    }

    /** 生成班级学情概览 */
    @PostMapping("/class-overview")
    public R<String> classOverview(@RequestBody ClassOverviewRequest request) {
        String report = analyticsService.generateClassOverview(
                request.getClassName(), request.getCourseName(),
                request.getClassData());
        return R.ok(report);
    }

    /** 知识点掌握度图谱分析 */
    @PostMapping("/knowledge-graph")
    public R<String> knowledgeGraph(@RequestBody KnowledgeGraphRequest request) {
        String analysis = analyticsService.analyzeKnowledgeGraph(
                request.getCourseName(), request.getKnowledgePoints(),
                request.getMasteryScores());
        return R.ok(analysis);
    }

    /** 学习风险预警分析 */
    @PostMapping("/risk-analysis")
    public R<String> riskAnalysis(@RequestBody RiskAnalysisRequest request) {
        String analysis = analyticsService.analyzeRisk(
                request.getStudentName(), request.getRiskData());
        return R.ok(analysis);
    }

    @Data
    public static class StudentReportRequest {
        private Long studentId;
        private String studentName;
        private String courseName;
        private Map<String, Object> learningData;
    }

    @Data
    public static class ClassOverviewRequest {
        private String className;
        private String courseName;
        private Map<String, Object> classData;
    }

    @Data
    public static class KnowledgeGraphRequest {
        private String courseName;
        private List<String> knowledgePoints;
        private Map<String, Double> masteryScores;
    }

    @Data
    public static class RiskAnalysisRequest {
        private String studentName;
        private Map<String, Object> riskData;
    }
}

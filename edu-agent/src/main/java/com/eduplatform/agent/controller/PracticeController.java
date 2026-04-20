package com.eduplatform.agent.controller;

import com.eduplatform.agent.service.ChatRecordService;
import com.eduplatform.agent.service.PracticeGenerationService;
import com.eduplatform.common.core.domain.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 练习生成控制器
 * <p>
 * 提供基于 LLM 的练习题生成、个性化出题和试卷生成接口。
 * </p>
 */
@RestController
@RequestMapping("/agent/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeGenerationService practiceService;
    private final ChatRecordService chatRecordService;

    /** 按知识点生成练习题 */
    @PostMapping("/generate")
    public R<String> generate(@RequestBody GenerateRequest request,
                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String result = practiceService.generateByKnowledgePoints(
                request.getCourseName(), request.getKnowledgePoints(),
                request.getDifficulty(), request.getCount() != null ? request.getCount() : 5);
        chatRecordService.savePracticeRecord(userId, null, "练习生成: " + request.getCourseName() + " " + request.getKnowledgePoints(), result, "deepseek-chat");
        return R.ok(result);
    }

    /** 个性化练习（根据学生薄弱点出题） */
    @PostMapping("/personalized")
    public R<String> personalized(@RequestBody PersonalizedRequest request,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String result = practiceService.generatePersonalized(
                request.getCourseName(), request.getStudentName(),
                request.getWeakPoints(), request.getMasteredPoints(),
                request.getCount() != null ? request.getCount() : 5);
        chatRecordService.savePracticeRecord(userId, null, "个性化练习: " + request.getStudentName(), result, "deepseek-chat");
        return R.ok(result);
    }

    /** 生成模拟试卷 */
    @PostMapping("/exam-paper")
    public R<String> examPaper(@RequestBody ExamPaperRequest request,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String result = practiceService.generateExamPaper(
                request.getCourseName(), request.getKnowledgePoints(),
                request.getTotalScore() != null ? request.getTotalScore() : 100,
                request.getExamType() != null ? request.getExamType() : "期末考试");
        chatRecordService.savePracticeRecord(userId, null, "试卷生成: " + request.getCourseName(), result, "deepseek-chat");
        return R.ok(result);
    }

    @Data
    public static class GenerateRequest {
        private String courseName;
        private List<String> knowledgePoints;
        private String difficulty;
        private Integer count;
    }

    @Data
    public static class PersonalizedRequest {
        private String courseName;
        private String studentName;
        private List<String> weakPoints;
        private List<String> masteredPoints;
        private Integer count;
    }

    @Data
    public static class ExamPaperRequest {
        private String courseName;
        private List<String> knowledgePoints;
        private Integer totalScore;
        private String examType;
    }
}

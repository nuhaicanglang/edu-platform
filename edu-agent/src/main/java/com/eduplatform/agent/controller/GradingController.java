package com.eduplatform.agent.controller;

import com.eduplatform.agent.service.AssignmentGradingService;
import com.eduplatform.agent.service.ChatRecordService;
import com.eduplatform.common.core.domain.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/grading")
@RequiredArgsConstructor
public class GradingController {

    private final AssignmentGradingService gradingService;
    private final ChatRecordService chatRecordService;

    @PostMapping("/text")
    public R<String> gradeText(@RequestBody TextGradingRequest request,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String result = gradingService.gradeTextAssignment(
                request.getAssignmentTitle(),
                request.getAssignmentRequirement(),
                request.getStudentAnswer(),
                request.getReferenceAnswer());
        chatRecordService.saveQAPair(userId, null, "AI批改(文本): " + request.getAssignmentTitle(), result, "deepseek-chat");
        return R.ok(result);
    }

    @PostMapping("/code")
    public R<String> gradeCode(@RequestBody CodeGradingRequest request,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String result = gradingService.gradeCodeAssignment(
                request.getAssignmentTitle(),
                request.getRequirement(),
                request.getStudentCode(),
                request.getTestCases());
        chatRecordService.saveQAPair(userId, null, "AI批改(代码): " + request.getAssignmentTitle(), result, "deepseek-chat");
        return R.ok(result);
    }

    @Data
    public static class TextGradingRequest {
        private String assignmentTitle;
        private String assignmentRequirement;
        private String studentAnswer;
        private String referenceAnswer;
    }

    @Data
    public static class CodeGradingRequest {
        private String assignmentTitle;
        private String requirement;
        private String studentCode;
        private String testCases;
    }
}

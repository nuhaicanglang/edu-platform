package com.eduplatform.agent.controller;

import com.eduplatform.agent.service.ChatRecordService;
import com.eduplatform.agent.service.IntelligentQAService;
import com.eduplatform.common.core.domain.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 智能问答控制器
 * <p>
 * 提供基于 LLM 的课程知识问答、简单问答和知识点讲解接口。
 * </p>
 */
@RestController
@RequestMapping("/agent/qa")
@RequiredArgsConstructor
public class QAController {

    private final IntelligentQAService qaService;
    private final ChatRecordService chatRecordService;

    /** 智能问答（带课程上下文） */
    @PostMapping("/ask")
    public R<String> ask(@RequestBody QARequest request,
                         @RequestHeader("X-User-Id") Long userId) {
        String answer = qaService.ask(request.getQuestion(), request.getCourseContext(),
                request.getCourseId(), userId);
        chatRecordService.saveQAPair(userId, request.getCourseId(), request.getQuestion(), answer, "deepseek-chat");
        return R.ok(answer);
    }

    /** 简单问答（无上下文） */
    @PostMapping("/ask-simple")
    public R<String> askSimple(@RequestBody QARequest request,
                               @RequestHeader("X-User-Id") Long userId) {
        String answer = qaService.askSimple(request.getQuestion());
        chatRecordService.saveQAPair(userId, null, request.getQuestion(), answer, "deepseek-chat");
        return R.ok(answer);
    }

    /** 知识点讲解 */
    @PostMapping("/explain")
    public R<String> explainKnowledgePoint(@RequestBody ExplainRequest request,
                                           @RequestHeader("X-User-Id") Long userId) {
        String answer = qaService.explainKnowledgePoint(request.getKnowledgePoint(), request.getCourseName());
        chatRecordService.saveQAPair(userId, null, "讲解知识点: " + request.getKnowledgePoint(), answer, "deepseek-chat");
        return R.ok(answer);
    }

    @Data
    public static class QARequest {
        private String question;
        private String courseContext;
        private Long courseId;
    }

    @Data
    public static class ExplainRequest {
        private String knowledgePoint;
        private String courseName;
    }
}

package com.eduplatform.agent.controller;

import com.eduplatform.agent.agent.EduAgent;
import com.eduplatform.common.core.domain.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 智能 Agent 控制器
 * <p>
 * 对外暴露 {@code POST /agent/smart/ask} 接口，由真正的 AI Agent（LangChain4j）处理，
 * LLM 会自主调用工具（查数据库、搜知识库）后给出基于真实数据的回答。
 * 与 /agent/qa/ask 的区别：后者是固定流程，本接口是 LLM 自主规划。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/agent/smart")
@RequiredArgsConstructor
public class SmartAgentController {

    private final EduAgent eduAgent;

    /**
     * 智能 Agent 问答 - LLM 自主决策调用工具
     *
     * @param request 包含 question（必填）、courseId（可选提示）
     * @param userId  由 Gateway 从 JWT 解析后通过 X-User-Id Header 传入
     */
    @PostMapping("/ask")
    public R<String> ask(@RequestBody AgentAskRequest request,
                         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        long uid = userId != null ? userId : 0L;
        log.info("SmartAgent request: userId={}, question={}", uid, request.getQuestion());

        String question = request.getQuestion();
        if (request.getCourseId() != null) {
            question = "[课程ID=" + request.getCourseId() + "] " + question;
        }
        if (request.getStudentId() != null) {
            question = "[学生ID=" + request.getStudentId() + "] " + question;
        }

        String answer = eduAgent.chat(uid, question);
        return R.ok(answer);
    }

    @Data
    public static class AgentAskRequest {
        /** 用户问题（必填） */
        private String question;
        /** 课程ID（可选，提供后 Agent 可更精准查询） */
        private Long courseId;
        /** 学生ID（可选，查询特定学生数据时使用） */
        private Long studentId;
    }
}

package com.eduplatform.agent.service;

import com.eduplatform.agent.domain.entity.AiChatRecord;
import com.eduplatform.agent.mapper.AiChatRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * AI对话记录持久化服务
 * <p>
 * 异步保存对话记录到数据库，避免阻塞主请求。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRecordService {

    private final AiChatRecordMapper chatRecordMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 异步保存一轮对话（用户问题 + AI回答）
     * 同时写入 learning_record 表以便学习轨迹展示。
     */
    @Async
    public void saveQAPair(Long userId, Long courseId, String question, String answer, String model) {
        try {
            String sessionId = generateSessionId(userId);
            long uid = userId != null ? userId : 0L;

            // 保存用户消息
            AiChatRecord userRecord = new AiChatRecord();
            userRecord.setUserId(uid);
            userRecord.setCourseId(courseId);
            userRecord.setSessionId(sessionId);
            userRecord.setRole("user");
            userRecord.setContent(question);
            userRecord.setModel(model);
            userRecord.setTokensUsed(0);
            chatRecordMapper.insert(userRecord);

            // 保存AI回复
            AiChatRecord assistantRecord = new AiChatRecord();
            assistantRecord.setUserId(uid);
            assistantRecord.setCourseId(courseId);
            assistantRecord.setSessionId(sessionId);
            assistantRecord.setRole("assistant");
            assistantRecord.setContent(answer);
            assistantRecord.setModel(model);
            assistantRecord.setTokensUsed(0);
            chatRecordMapper.insert(assistantRecord);

            // 同步写入 learning_record（学习轨迹）
            String actionType = "qa";
            String detail = "AI问答: " + (question.length() > 100 ? question.substring(0, 100) + "..." : question);
            jdbcTemplate.update(
                "INSERT INTO learning_record (student_id, course_id, action_type, action_detail, duration) VALUES (?,?,?,?,0)",
                uid, courseId != null ? courseId : 0L, actionType, detail);

            log.debug("保存AI对话记录成功, userId={}, sessionId={}", userId, sessionId);
        } catch (Exception e) {
            log.error("保存AI对话记录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 异步保存练习生成记录
     */
    @Async
    public void savePracticeRecord(Long userId, Long courseId, String question, String answer, String model) {
        saveQAPair(userId, courseId, question, answer, model);
        try {
            String detail = "练习生成: " + (question.length() > 100 ? question.substring(0, 100) + "..." : question);
            jdbcTemplate.update(
                "INSERT INTO learning_record (student_id, course_id, action_type, action_detail, duration) VALUES (?,?,?,?,0)",
                userId != null ? userId : 0L, courseId != null ? courseId : 0L, "practice", detail);
        } catch (Exception e) {
            log.error("保存练习学习记录失败: {}", e.getMessage(), e);
        }
    }

    private String generateSessionId(Long userId) {
        return "sess_" + (userId != null ? userId : 0) + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}

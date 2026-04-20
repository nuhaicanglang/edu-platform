package com.eduplatform.system.service;

import com.eduplatform.system.domain.entity.LearningRecord;
import com.eduplatform.system.mapper.LearningRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 学习记录服务
 * <p>
 * 异步记录学生的学习行为（浏览课程、提交作业、AI问答、练习生成等），
 * 为学情分析提供数据支撑。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRecordService {

    private final LearningRecordMapper learningRecordMapper;

    /**
     * 记录学习行为（内部方法，不加 @Async）
     * <p>
     * 注意：此方法不加 @Async 因为会被类内方法（recordView/recordSubmit 等）自调用，
     * Spring AOP 代理在类内自调用时失效，外层方法的 @Async 已保证异步执行。
     * </p>
     */
    public void record(Long studentId, Long courseId, Long knowledgePointId,
                       String actionType, String actionDetail, BigDecimal score, Integer duration) {
        try {
            LearningRecord record = new LearningRecord();
            record.setStudentId(studentId != null ? studentId : 0L);
            record.setCourseId(courseId);
            record.setKnowledgePointId(knowledgePointId);
            record.setActionType(actionType);
            record.setActionDetail(actionDetail != null && actionDetail.length() > 500
                    ? actionDetail.substring(0, 500) : actionDetail);
            record.setScore(score);
            record.setDuration(duration != null ? duration : 0);
            learningRecordMapper.insert(record);
            log.debug("记录学习行为: studentId={}, type={}, course={}", studentId, actionType, courseId);
        } catch (Exception e) {
            log.error("记录学习行为失败: {}", e.getMessage(), e);
        }
    }

    /** 记录浏览行为 */
    @Async
    public void recordView(Long studentId, Long courseId, String detail) {
        record(studentId, courseId, null, "view", detail, null, null);
    }

    /** 记录提交作业行为 */
    @Async
    public void recordSubmit(Long studentId, Long courseId, String detail, BigDecimal score) {
        record(studentId, courseId, null, "submit", detail, score, null);
    }

    /** 记录AI问答行为 */
    @Async
    public void recordQA(Long studentId, Long courseId, String detail) {
        record(studentId, courseId, null, "qa", detail, null, null);
    }

    /** 记录练习行为 */
    @Async
    public void recordPractice(Long studentId, Long courseId, String detail) {
        record(studentId, courseId, null, "practice", detail, null, null);
    }
}

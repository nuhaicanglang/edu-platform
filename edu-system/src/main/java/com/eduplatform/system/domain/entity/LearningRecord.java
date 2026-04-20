package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 学习记录（用于学情分析）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learning_record")
public class LearningRecord extends BaseEntity {

    /** 学生ID */
    private Long studentId;

    /** 课程ID */
    private Long courseId;

    /** 知识点ID */
    private Long knowledgePointId;

    /** 行为类型: view/practice/submit/qa */
    private String actionType;

    /** 行为详情 */
    private String actionDetail;

    /** 得分(如有) */
    private BigDecimal score;

    /** 时长(秒) */
    private Integer duration;
}

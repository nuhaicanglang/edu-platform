package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教学班级
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_group")
public class ClassGroup extends BaseEntity {

    /** 班级名称 */
    private String className;

    /** 课程ID */
    private Long courseId;

    /** 教师ID */
    private Long teacherId;

    /** 学期 如: 2024-2025-1 */
    private String semester;

    /** 学生人数 */
    private Integer studentCount;

    /** 状态 0-进行中 1-已结束 */
    private Integer status;
}

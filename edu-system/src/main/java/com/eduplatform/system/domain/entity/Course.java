package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    /** 课程名称 */
    private String courseName;

    /** 课程编码 */
    private String courseCode;

    /** 课程描述 */
    private String description;

    /** 课程封面图片URL */
    private String coverUrl;

    /** 课程类别: theory-理论课, practice-实践课, mixed-混合课 */
    private String category;

    /** 学分 */
    private Double credit;

    /** 学时 */
    private Integer classHours;

    /** 创建教师ID */
    private Long teacherId;

    /** 状态 active-正常 archived-归档 */
    private String status;
}

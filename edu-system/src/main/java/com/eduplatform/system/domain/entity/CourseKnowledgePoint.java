package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程知识点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_knowledge_point")
public class CourseKnowledgePoint extends BaseEntity {

    /** 课程ID */
    private Long courseId;

    /** 父知识点ID */
    private Long parentId;

    /** 知识点名称 */
    @TableField("name")
    private String pointName;

    /** 知识点编码 */
    private String pointCode;

    /** 知识点描述 */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    /** 难度等级 1-5 */
    private Integer difficulty;

    /** 层级 */
    private Integer level;
}

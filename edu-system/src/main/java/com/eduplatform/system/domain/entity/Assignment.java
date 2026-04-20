package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment")
public class Assignment extends BaseEntity {

    /** 作业标题 */
    private String title;

    /** 作业描述 */
    private String description;

    /** 课程ID */
    private Long courseId;

    /** 班级ID */
    private Long classId;

    /** 教师ID */
    private Long teacherId;

    /** 作业类型: text-文本, code-代码, report-实验报告, mixed-混合 */
    @TableField("type")
    private String assignmentType;

    /** 关联知识点IDs (JSON数组) */
    private String knowledgePointIds;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /** 满分 */
    @TableField("max_score")
    private Integer totalScore;

    /** 状态: draft-草稿, published-已发布, closed-已关闭 */
    private String status;

    /** 是否启用AI批改 */
    private Boolean aiGradingEnabled;

    /** 作业附件URL（教师上传的题目文件） */
    private String attachmentUrl;

    /** 作业附件原始文件名 */
    private String attachmentName;
}

package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业提交
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment_submission")
public class AssignmentSubmission extends BaseEntity {

    /** 作业ID */
    private Long assignmentId;

    /** 学生ID */
    private Long studentId;

    /** 提交内容(文本) */
    private String content;

    /** 提交文件URL */
    private String fileUrl;

    /** 原始文件名 */
    private String fileName;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /** 得分 */
    private Integer score;

    /** AI批改状态: pending-待批改, grading-批改中, completed-已完成, failed-失败 */
    @TableField("status")
    private String gradingStatus;

    /** AI批改结果(JSON) */
    @TableField("ai_grading_result")
    private String gradingResult;

    /** 带批注的文档URL */
    private String annotatedFileUrl;

    /** AI总评 */
    private String aiComment;

    /** 学生姓名（非数据库字段，接口返回时填充） */
    @TableField(exist = false)
    private String studentName;
}

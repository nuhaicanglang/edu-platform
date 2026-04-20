package com.eduplatform.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级学生关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_student")
public class ClassStudent extends BaseEntity {

    /** 班级ID */
    private Long classId;

    /** 学生用户ID */
    private Long studentId;

    /** 加入时间 */
    private String joinTime;

    /** 学号（用户名）- 非数据库字段 */
    @TableField(exist = false)
    private String username;

    /** 姓名 - 非数据库字段 */
    @TableField(exist = false)
    private String realName;
}

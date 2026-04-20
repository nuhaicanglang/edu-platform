package com.eduplatform.common.core.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有业务实体继承此类，统一提供 id、createBy、createTime、updateBy、updateTime、remark、deleted 字段。
 * 公共字段由 {@link com.eduplatform.common.config.MyMetaObjectHandler} 自动填充。
 * </p>
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新者 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 逻辑删除 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}

package com.eduplatform.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 在执行 INSERT/UPDATE 时自动填充 createTime、updateTime、createBy、updateBy 等公共字段，
 * 避免开发者在每个 Service 中手动设置。
 * </p>
 *
 * @see com.eduplatform.common.core.domain.BaseEntity
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /** 新增时自动填充 createTime、updateTime、createBy、updateBy */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", String.class, "system");
        this.strictInsertFill(metaObject, "updateBy", String.class, "system");
    }

    /** 更新时自动填充 updateTime、updateBy */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, "system");
    }
}

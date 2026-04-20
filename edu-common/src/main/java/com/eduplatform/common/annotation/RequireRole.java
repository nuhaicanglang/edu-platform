package com.eduplatform.common.annotation;

import java.lang.annotation.*;

/**
 * 角色权限校验注解
 * <p>
 * 标注在 Controller 方法或类上，AOP 自动校验请求头中的 X-User-Role 是否匹配。
 * 不匹配则直接返回 403 错误。
 * </p>
 *
 * @author edu-platform
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /** 允许访问的角色列表，如 {"teacher", "admin"} */
    String[] value();
}

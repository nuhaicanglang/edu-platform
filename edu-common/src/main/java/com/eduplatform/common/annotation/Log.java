package com.eduplatform.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，AOP 自动记录：操作人、请求参数、执行耗时、操作结果。
 * </p>
 *
 * @author edu-platform
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 操作模块 */
    String module() default "";

    /** 操作描述 */
    String value() default "";
}

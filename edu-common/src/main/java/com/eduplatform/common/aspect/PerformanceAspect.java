package com.eduplatform.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 接口性能监控 AOP 切面
 * <p>
 * 自动拦截所有 Controller 层方法，统计执行耗时。
 * 耗时超过阈值（默认 3 秒）自动 WARN 告警。
 * </p>
 *
 * @author edu-platform
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    /** 慢接口告警阈值（毫秒） */
    private static final long SLOW_THRESHOLD_MS = 3000;

    /** 匹配所有 controller 包下的公共方法 */
    @Pointcut("execution(public * com.eduplatform..controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object monitorPerformance(ProceedingJoinPoint point) throws Throwable {
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();
        String fullMethod = className + "." + methodName;

        long start = System.currentTimeMillis();
        try {
            return point.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= SLOW_THRESHOLD_MS) {
                log.warn("[性能告警] {} 耗时 {}ms，超过阈值 {}ms", fullMethod, cost, SLOW_THRESHOLD_MS);
            } else if (log.isDebugEnabled()) {
                log.debug("[性能监控] {} 耗时 {}ms", fullMethod, cost);
            }
        }
    }
}

package com.eduplatform.common.aspect;

import com.eduplatform.common.annotation.RequireRole;
import com.eduplatform.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 角色权限校验 AOP 切面
 * <p>
 * 拦截标注了 {@link RequireRole} 注解的方法或类，
 * 从请求头 X-User-Role 中获取当前用户角色，与注解声明的允许角色进行匹配。
 * 不匹配则抛出 {@link BusinessException}（HTTP 403）。
 * </p>
 * <p>
 * 方法级注解优先于类级注解。
 * </p>
 *
 * @author edu-platform
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    @Around("@annotation(com.eduplatform.common.annotation.RequireRole) || @within(com.eduplatform.common.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint point) throws Throwable {
        // 获取注解（方法级优先，类级兜底）
        MethodSignature sig = (MethodSignature) point.getSignature();
        Method method = sig.getMethod();
        RequireRole annotation = method.getAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = point.getTarget().getClass().getAnnotation(RequireRole.class);
        }
        if (annotation == null) {
            return point.proceed();
        }

        String[] allowedRoles = annotation.value();

        // 获取当前用户角色
        HttpServletRequest request = getRequest();
        String currentRole = request != null ? request.getHeader("X-User-Role") : null;

        if (currentRole == null || currentRole.isEmpty()) {
            log.warn("[权限拦截] {}.{} 无角色信息，拒绝访问", point.getTarget().getClass().getSimpleName(), method.getName());
            throw new BusinessException(403, "未登录或角色信息缺失");
        }

        boolean hasRole = Arrays.asList(allowedRoles).contains(currentRole);
        if (!hasRole) {
            log.warn("[权限拦截] {}.{} 需要角色 {}，当前角色 {}，拒绝访问",
                    point.getTarget().getClass().getSimpleName(), method.getName(),
                    Arrays.toString(allowedRoles), currentRole);
            throw new BusinessException(403, "权限不足，需要角色: " + Arrays.toString(allowedRoles));
        }

        return point.proceed();
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

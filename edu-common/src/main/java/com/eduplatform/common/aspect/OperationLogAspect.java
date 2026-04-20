package com.eduplatform.common.aspect;

import com.eduplatform.common.annotation.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志 AOP 切面
 * <p>
 * 拦截标注了 {@link Log} 注解的方法，自动记录：
 * <ul>
 *   <li>操作模块、操作描述</li>
 *   <li>请求 URI、HTTP 方法、请求 IP</li>
 *   <li>操作人（X-User-Id / X-User-Name）</li>
 *   <li>请求参数（自动过滤文件类型参数）</li>
 *   <li>执行耗时（毫秒）</li>
 *   <li>操作结果（成功 / 异常信息）</li>
 * </ul>
 * </p>
 *
 * @author edu-platform
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint point, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String module = logAnnotation.module();
        String operation = logAnnotation.value();

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";
        String ip = request != null ? getClientIp(request) : "unknown";
        String userId = request != null ? request.getHeader("X-User-Id") : "anonymous";
        String userName = request != null ? request.getHeader("X-User-Name") : "anonymous";

        // 获取方法参数（过滤文件类型）
        String params = getParams(point);

        Object result = null;
        String status = "SUCCESS";
        String errorMsg = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            status = "FAIL";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costMs = System.currentTimeMillis() - startTime;
            log.info("[操作日志] module={}, op={}, user={}({}), method={} {}, ip={}, params={}, status={}, cost={}ms{}",
                    module, operation, userName, userId, method, uri, ip,
                    params, status, costMs,
                    errorMsg != null ? ", error=" + errorMsg : "");
        }
    }

    private String getParams(ProceedingJoinPoint point) {
        try {
            MethodSignature sig = (MethodSignature) point.getSignature();
            String[] paramNames = sig.getParameterNames();
            Object[] args = point.getArgs();
            if (paramNames == null || paramNames.length == 0) return "{}";
            Map<String, Object> paramMap = new LinkedHashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] instanceof MultipartFile f) {
                    paramMap.put(paramNames[i], "file(" + f.getOriginalFilename() + ")");
                } else if (args[i] instanceof HttpServletRequest) {
                    // skip
                } else {
                    paramMap.put(paramNames[i], args[i]);
                }
            }
            String json = objectMapper.writeValueAsString(paramMap);
            return json.length() > 500 ? json.substring(0, 500) + "..." : json;
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}

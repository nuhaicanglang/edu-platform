package com.eduplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * edu-gateway API 网关启动类
 * <p>
 * 统一入口（端口 9000），负责路由分发、JWT 认证过滤和跨域处理。
 * 路由规则：/api/auth/** → edu-auth:8081，/api/system/** → edu-system:8082，
 * /api/agent/** → edu-agent:8083，/api/knowledge/** → edu-knowledge:8084。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class EduGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduGatewayApplication.class, args);
    }
}

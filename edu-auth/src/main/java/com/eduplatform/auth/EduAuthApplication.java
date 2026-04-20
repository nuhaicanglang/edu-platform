package com.eduplatform.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * edu-auth 认证服务启动类
 * <p>
 * 提供用户登录、注册、JWT 签发与验证等认证功能。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.eduplatform"})
@MapperScan("com.eduplatform.auth.mapper")
public class EduAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduAuthApplication.class, args);
    }
}

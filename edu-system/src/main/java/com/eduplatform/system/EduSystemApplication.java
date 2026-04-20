package com.eduplatform.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * edu-system 核心业务服务启动类
 * <p>
 * 提供课程、班级、作业、知识点、学情分析等核心模块。
 * 扫描 com.eduplatform 全包以加载 edu-common 中的公共组件（Redis、异常处理、MyBatis 填充等）。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@ComponentScan(basePackages = {"com.eduplatform"})
@MapperScan("com.eduplatform.system.mapper")
public class EduSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduSystemApplication.class, args);
    }
}

package com.eduplatform.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import com.eduplatform.knowledge.config.RagProperties;

/**
 * edu-knowledge 知识库服务启动类
 * <p>
 * 提供知识文档上传、文本提取、分块存储和关键词检索功能，
 * 为 AI 问答系统提供课程知识上下文。
 * </p>
 */
@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.eduplatform"})
@MapperScan("com.eduplatform.knowledge.mapper")
public class EduKnowledgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduKnowledgeApplication.class, args);
    }
}

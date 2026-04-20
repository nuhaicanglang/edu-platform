package com.eduplatform.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * edu-agent AI 智能服务启动类
 * <p>
 * 提供 AI 批改、智能问答、学情分析、练习生成等 LLM 驱动的功能。
 * 通过 LlmService 统一调用不同 LLM 提供商（DeepSeek/DashScope/OpenAI）。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@ComponentScan(basePackages = {"com.eduplatform"})
@MapperScan("com.eduplatform.agent.mapper")
public class EduAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduAgentApplication.class, args);
    }
}

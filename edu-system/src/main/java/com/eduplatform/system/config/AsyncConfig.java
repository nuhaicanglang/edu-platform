package com.eduplatform.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * <p>
 * 定义 AI 批改专用的线程池 gradingExecutor，避免长时间的 LLM 调用占用主线程。
 * 队列饱和时立即拒绝，由业务层返回可重试状态，禁止阻塞请求线程。
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("gradingExecutor")
    public Executor gradingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);          // 核心线程数：同时处理4个批改任务
        executor.setMaxPoolSize(10);          // 最大线程数：突发时最多10个
        executor.setQueueCapacity(50);        // 队列：最多排队50个任务
        executor.setThreadNamePrefix("grading-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public RestTemplate agentRestTemplate(
            @Value("${service.agent.connect-timeout:5s}") Duration connectTimeout,
            @Value("${service.agent.read-timeout:120s}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return new RestTemplate(requestFactory);
    }
}

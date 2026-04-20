package com.eduplatform.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * <p>
 * 定义 AI 批改专用的线程池 gradingExecutor，避免长时间的 LLM 调用占用主线程。
 * 拒绝策略采用 CallerRunsPolicy，队列满时由调用方线程执行（保证不丢失任务）。
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
        // 队列满时由调用线程直接执行（降级兜底，不丢任务）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

package com.eduplatform.agent.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 教育 AI Agent 接口
 * <p>
 * LangChain4j 通过 {@code AiServices.builder()} 为该接口生成代理实现。
 * LLM 接收用户问题后，自主规划是否需要调用工具（searchKnowledge、getAssignmentStats 等），
 * 工具调用结果会自动注入对话上下文，LLM 多轮推理后给出最终回答。
 * {@code @MemoryId} 实现每个用户独立的对话历史（窗口10条消息）。
 * </p>
 */
public interface EduAgent {

    @SystemMessage("""
            你是一个智能教育助手 AI Agent，服务于高校教学管理平台。
            
            你拥有以下工具，请根据用户问题自主决定是否调用及调用顺序：
            - searchKnowledge：搜索课程知识库
            - getCourseInfo：获取课程基本信息
            - getAssignmentStats：获取课程作业统计
            - getStudentProgress：获取学生学习进度和成绩
            - getClassOverview：获取班级整体学情
            
            工作原则：
            1. 优先通过工具获取真实数据，不凭空编造
            2. 如需多个信息，可连续调用多个工具
            3. 基于真实数据给出专业、有依据的分析和建议
            4. 回答使用中文，适当使用 Markdown 格式
            5. 如果问题与教学无关，礼貌说明自己的职责范围
            """)
    String chat(@MemoryId long userId, @UserMessage String question);
}

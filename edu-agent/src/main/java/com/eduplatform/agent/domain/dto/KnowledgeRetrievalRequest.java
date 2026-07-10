package com.eduplatform.agent.domain.dto;

/** 调用知识服务的检索请求。 */
public record KnowledgeRetrievalRequest(String question, Long courseId) {
}

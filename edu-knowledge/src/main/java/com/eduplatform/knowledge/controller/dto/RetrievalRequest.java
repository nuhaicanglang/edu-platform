package com.eduplatform.knowledge.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 课程 RAG 检索请求。 */
public record RetrievalRequest(
        @NotBlank(message = "问题不能为空") String question,
        @NotNull(message = "courseId 不能为空") Long courseId) {
}

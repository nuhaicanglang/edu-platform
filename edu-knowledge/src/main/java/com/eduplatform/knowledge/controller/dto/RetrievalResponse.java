package com.eduplatform.knowledge.controller.dto;

import com.eduplatform.knowledge.search.RetrievalResult;

import java.util.List;

/** 结构化 RAG 检索结果。 */
public record RetrievalResponse(String retrievalMode, List<RetrievalResult> sources) {
}

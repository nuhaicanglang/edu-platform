package com.eduplatform.knowledge.service;

import com.eduplatform.knowledge.config.RagSearchProperties;
import com.eduplatform.knowledge.embedding.EmbeddingClient;
import com.eduplatform.knowledge.search.KnowledgeVectorIndex;
import com.eduplatform.knowledge.search.ReciprocalRankFusion;
import com.eduplatform.knowledge.search.RetrievalCandidate;
import com.eduplatform.knowledge.search.RetrievalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 执行课程过滤后的向量 + BM25 混合检索。 */
@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private final EmbeddingClient embeddingClient;
    private final KnowledgeVectorIndex vectorIndex;
    private final ReciprocalRankFusion fusion;
    private final RagSearchProperties properties;

    public List<RetrievalResult> retrieve(String question, Long courseId) {
        String normalized = normalize(question);
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("courseId 必须为正数");
        }

        float[] queryVector = embeddingClient.embedAll(List.of(normalized)).get(0);
        List<RetrievalCandidate> vectorCandidates = vectorIndex.vectorSearch(
                courseId, queryVector, properties.candidateCount());
        List<RetrievalCandidate> keywordCandidates = vectorIndex.keywordSearch(
                courseId, normalized, properties.candidateCount());
        return fusion.fuse(
                vectorCandidates, keywordCandidates, properties.rrfConstant(), properties.topK());
    }

    private String normalize(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
        return question.trim().replaceAll("\\s+", " ");
    }
}

package com.eduplatform.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 对多路检索排名执行确定性的 Reciprocal Rank Fusion。 */
@Component
public class ReciprocalRankFusion {

    public List<RetrievalResult> fuse(
            List<RetrievalCandidate> vectorCandidates,
            List<RetrievalCandidate> keywordCandidates,
            int constant,
            int topK) {
        if (constant <= 0 || topK <= 0) {
            throw new IllegalArgumentException("RRF 参数必须为正数");
        }

        Map<String, Accumulator> scores = new LinkedHashMap<>();
        accumulate(scores, vectorCandidates, constant);
        accumulate(scores, keywordCandidates, constant);

        List<Accumulator> ranked = new ArrayList<>(scores.values());
        ranked.sort(Comparator.comparingDouble(Accumulator::score).reversed());
        return ranked.stream().limit(topK).map(accumulator -> {
            RetrievalCandidate source = accumulator.candidate();
            return new RetrievalResult(
                    source.chunkId(), source.documentId(), source.courseId(), source.chunkIndex(),
                    source.title(), source.content(), accumulator.score());
        }).toList();
    }

    private void accumulate(
            Map<String, Accumulator> scores,
            List<RetrievalCandidate> candidates,
            int constant) {
        if (candidates == null) return;
        for (int i = 0; i < candidates.size(); i++) {
            RetrievalCandidate candidate = candidates.get(i);
            double contribution = 1d / (constant + i + 1d);
            scores.compute(candidate.chunkId(), (chunkId, current) -> current == null
                    ? new Accumulator(candidate, contribution)
                    : new Accumulator(current.candidate(), current.score() + contribution));
        }
    }

    private record Accumulator(RetrievalCandidate candidate, double score) {}
}

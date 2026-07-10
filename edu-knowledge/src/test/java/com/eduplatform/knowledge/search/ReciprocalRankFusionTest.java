package com.eduplatform.knowledge.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReciprocalRankFusionTest {

    private final ReciprocalRankFusion fusion = new ReciprocalRankFusion();

    @Test
    void fusesVectorAndKeywordRanksWithoutDuplicates() {
        List<RetrievalResult> result = fusion.fuse(
                List.of(candidate("a", 0.9), candidate("b", 0.8)),
                List.of(candidate("b", 7.0), candidate("c", 6.0)),
                60, 3);

        assertThat(result).extracting(RetrievalResult::chunkId)
                .containsExactly("b", "a", "c");
        assertThat(result).extracting(RetrievalResult::chunkId).doesNotHaveDuplicates();
    }

    @Test
    void respectsTopKLimit() {
        List<RetrievalResult> result = fusion.fuse(
                List.of(candidate("a", 0.9), candidate("b", 0.8)),
                List.of(candidate("c", 7.0)), 60, 2);

        assertThat(result).hasSize(2);
    }

    private RetrievalCandidate candidate(String chunkId, double score) {
        return new RetrievalCandidate(chunkId, 1L, 1L, 0, "测试文档", "内容", score);
    }
}

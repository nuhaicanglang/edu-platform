package com.eduplatform.knowledge.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class DocumentParsingServiceTest {

    private final DocumentParsingService service = new DocumentParsingService();

    @Test
    void longSingleParagraphTerminatesAndCoversTail() {
        String text = "数据结构".repeat(400);

        List<String> chunks = assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> service.splitToChunks(text, 500, 50));

        assertThat(chunks).isNotEmpty().hasSizeLessThan(20);
        assertThat(chunks.get(chunks.size() - 1))
                .endsWith(text.substring(text.length() - 20));
    }

    @Test
    void rejectsOverlapThatCannotAdvanceCursor() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.splitToChunks("示例文本", 50, 50));
    }

    @Test
    void rejectsNegativeOverlap() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.splitToChunks("示例文本", 50, -1));
    }
}

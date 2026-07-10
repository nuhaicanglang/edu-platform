package com.eduplatform.knowledge.service;

import com.eduplatform.knowledge.domain.entity.KnowledgeChunk;
import com.eduplatform.knowledge.domain.entity.KnowledgeDocument;
import com.eduplatform.knowledge.embedding.EmbeddingClient;
import com.eduplatform.knowledge.embedding.EmbeddingUnavailableException;
import com.eduplatform.knowledge.mapper.KnowledgeChunkMapper;
import com.eduplatform.knowledge.mapper.KnowledgeDocumentMapper;
import com.eduplatform.knowledge.search.IndexedKnowledgeChunk;
import com.eduplatform.knowledge.search.KnowledgeVectorIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeIndexingServiceTest {

    @Mock KnowledgeDocumentMapper documentMapper;
    @Mock KnowledgeChunkMapper chunkMapper;
    @Mock EmbeddingClient embeddingClient;
    @Mock KnowledgeVectorIndex vectorIndex;

    @Test
    void failedEmbeddingLeavesDocumentRetryable() {
        KnowledgeDocument document = document();
        when(documentMapper.selectById(7L)).thenReturn(document);
        when(chunkMapper.selectList(any())).thenReturn(List.of(chunk()));
        when(embeddingClient.modelName()).thenReturn("bge-m3:latest");
        when(embeddingClient.dimensions()).thenReturn(1024);
        when(embeddingClient.embedAll(any()))
                .thenThrow(new EmbeddingUnavailableException("offline"));
        KnowledgeIndexingService service = service();

        service.indexDocument(7L);

        ArgumentCaptor<KnowledgeDocument> captor = ArgumentCaptor.forClass(KnowledgeDocument.class);
        verify(documentMapper, org.mockito.Mockito.atLeast(2)).updateById(captor.capture());
        KnowledgeDocument last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(last.getIndexStatus()).isEqualTo("failed");
        assertThat(last.getIndexError()).contains("offline");
    }

    @Test
    void readyStateIsWrittenOnlyAfterIndexUpsert() {
        KnowledgeDocument document = document();
        when(documentMapper.selectById(7L)).thenReturn(document);
        when(chunkMapper.selectList(any())).thenReturn(List.of(chunk()));
        when(embeddingClient.modelName()).thenReturn("bge-m3:latest");
        when(embeddingClient.dimensions()).thenReturn(3);
        when(embeddingClient.embedAll(List.of("前序遍历先访问根节点")))
                .thenReturn(List.of(new float[]{0.1f, 0.2f, 0.3f}));
        KnowledgeIndexingService service = service();

        service.indexDocument(7L);

        ArgumentCaptor<List<IndexedKnowledgeChunk>> chunks = ArgumentCaptor.forClass(List.class);
        verify(vectorIndex).upsertAll(chunks.capture());
        assertThat(chunks.getValue()).hasSize(1);
        assertThat(chunks.getValue().get(0).chunkId()).isEqualTo("31");
        assertThat(chunks.getValue().get(0).contentHash()).hasSize(64);
        assertThat(document.getIndexStatus()).isEqualTo("ready");
    }

    private KnowledgeIndexingService service() {
        return new KnowledgeIndexingService(documentMapper, chunkMapper, embeddingClient, vectorIndex);
    }

    private KnowledgeDocument document() {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(7L);
        document.setCourseId(12L);
        document.setTitle("数据结构基础");
        return document;
    }

    private KnowledgeChunk chunk() {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(31L);
        chunk.setDocumentId(7L);
        chunk.setCourseId(12L);
        chunk.setChunkIndex(4);
        chunk.setContent("前序遍历先访问根节点");
        return chunk;
    }
}

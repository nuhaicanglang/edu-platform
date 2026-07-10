package com.eduplatform.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduplatform.knowledge.domain.entity.KnowledgeChunk;
import com.eduplatform.knowledge.domain.entity.KnowledgeDocument;
import com.eduplatform.knowledge.embedding.EmbeddingClient;
import com.eduplatform.knowledge.mapper.KnowledgeChunkMapper;
import com.eduplatform.knowledge.mapper.KnowledgeDocumentMapper;
import com.eduplatform.knowledge.search.IndexedKnowledgeChunk;
import com.eduplatform.knowledge.search.KnowledgeVectorIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** 将 MySQL 知识分块投影到 Elasticsearch。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIndexingService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingClient embeddingClient;
    private final KnowledgeVectorIndex vectorIndex;

    public void indexDocument(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            log.warn("跳过不存在的知识文档索引 documentId={}", documentId);
            return;
        }

        document.setIndexStatus("processing");
        document.setIndexError(null);
        document.setEmbeddingModel(embeddingClient.modelName());
        document.setEmbeddingDimension(embeddingClient.dimensions());
        documentMapper.updateById(document);

        try {
            List<KnowledgeChunk> chunks = chunkMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeChunk>()
                            .eq(KnowledgeChunk::getDocumentId, documentId)
                            .orderByAsc(KnowledgeChunk::getChunkIndex));
            vectorIndex.ensureIndex();

            if (!chunks.isEmpty()) {
                List<String> texts = chunks.stream().map(KnowledgeChunk::getContent).toList();
                List<float[]> vectors = embeddingClient.embedAll(texts);
                if (vectors.size() != chunks.size()) {
                    throw new IllegalStateException("向量数量与知识分块数量不一致");
                }

                List<IndexedKnowledgeChunk> projections = new ArrayList<>(chunks.size());
                for (int i = 0; i < chunks.size(); i++) {
                    KnowledgeChunk chunk = chunks.get(i);
                    projections.add(new IndexedKnowledgeChunk(
                            String.valueOf(chunk.getId()),
                            chunk.getDocumentId(),
                            chunk.getCourseId(),
                            chunk.getChunkIndex(),
                            document.getTitle(),
                            chunk.getContent(),
                            vectors.get(i),
                            embeddingClient.modelName(),
                            sha256(chunk.getContent()),
                            Instant.now()));
                }
                vectorIndex.upsertAll(projections);
            }

            document.setIndexStatus("ready");
            document.setIndexError(null);
            document.setIndexedAt(LocalDateTime.now());
            documentMapper.updateById(document);
            log.info("知识文档索引完成 documentId={}, chunks={}", documentId, chunks.size());
        } catch (Exception e) {
            document.setIndexStatus("failed");
            document.setIndexError(truncate(e.getMessage(), 1000));
            documentMapper.updateById(document);
            log.error("知识文档索引失败 documentId={}: {}", documentId, e.getMessage(), e);
        }
    }

    private String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) return "未知索引错误";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}

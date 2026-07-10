package com.eduplatform.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.knowledge.domain.entity.KnowledgeChunk;
import com.eduplatform.knowledge.domain.entity.KnowledgeDocument;
import com.eduplatform.knowledge.mapper.KnowledgeChunkMapper;
import com.eduplatform.knowledge.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 知识库管理服务 - 文档上传、解析、存储和检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final DocumentParsingService parsingService;
    private final KnowledgeIndexingService indexingService;
    private final com.eduplatform.knowledge.search.KnowledgeVectorIndex vectorIndex;

    /**
     * 上传并解析文档到知识库
     */
    @Transactional
    public KnowledgeDocument uploadAndParse(MultipartFile file, Long courseId, Long userId) {
        try {
            // 上传文件
            String filePath = parsingService.uploadFile(file);

            // 创建文档记录
            KnowledgeDocument doc = new KnowledgeDocument();
            doc.setCourseId(courseId);
            doc.setTitle(file.getOriginalFilename());
            doc.setFileName(file.getOriginalFilename());
            doc.setFileUrl(filePath);
            doc.setFileType(getExtension(file.getOriginalFilename()));
            doc.setFileSize(file.getSize());
            doc.setUploadUserId(userId);
            doc.setStatus("parsing");
            documentMapper.insert(doc);

            // 提取文本
            String text = parsingService.extractText(filePath);
            doc.setContent(text);

            // 分块
            List<String> chunks = parsingService.splitToChunks(text);
            doc.setChunkCount(chunks.size());
            doc.setStatus("completed");
            doc.setIndexStatus("pending");
            documentMapper.updateById(doc);

            // 保存分块
            for (int i = 0; i < chunks.size(); i++) {
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(doc.getId());
                chunk.setCourseId(courseId);
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunkMapper.insert(chunk);
            }

            log.info("文档解析完成: {}, 共{}个分块", doc.getTitle(), chunks.size());
            runAfterCommit(() -> indexingService.indexDocument(doc.getId()));
            return doc;
        } catch (IOException e) {
            log.error("文档上传解析失败: {}", e.getMessage(), e);
            throw new BusinessException("文档上传解析失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询课程文档
     */
    public PageResult<KnowledgeDocument> pageDocuments(int pageNum, int pageSize, Long courseId) {
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId != null, KnowledgeDocument::getCourseId, courseId);
        wrapper.orderByDesc(KnowledgeDocument::getCreateTime);

        Page<KnowledgeDocument> page = documentMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 获取文档详情
     */
    public KnowledgeDocument getDocument(Long documentId) {
        KnowledgeDocument doc = documentMapper.selectById(documentId);
        if (doc == null) throw new BusinessException("文档不存在");
        return doc;
    }

    /**
     * 删除文档及其分块
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        documentMapper.deleteById(documentId);
        chunkMapper.delete(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, documentId));
        runAfterCommit(() -> {
            try {
                vectorIndex.deleteByDocumentId(documentId);
            } catch (Exception e) {
                log.error("删除 Elasticsearch 文档投影失败 documentId={}: {}", documentId, e.getMessage());
            }
        });
    }

    /**
     * 获取文档的所有分块
     */
    public List<KnowledgeChunk> getChunks(Long documentId) {
        return chunkMapper.selectList(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, documentId)
                .orderByAsc(KnowledgeChunk::getChunkIndex));
    }

    /**
     * 根据课程ID获取所有知识块内容（用于AI上下文）
     */
    public String getCourseKnowledgeContext(Long courseId, int maxChunks) {
        List<KnowledgeChunk> chunks = chunkMapper.selectList(
                new LambdaQueryWrapper<KnowledgeChunk>()
                        .eq(KnowledgeChunk::getCourseId, courseId)
                        .orderByAsc(KnowledgeChunk::getDocumentId)
                        .orderByAsc(KnowledgeChunk::getChunkIndex)
                        .last("LIMIT " + maxChunks));

        StringBuilder sb = new StringBuilder();
        for (KnowledgeChunk chunk : chunks) {
            sb.append(chunk.getContent()).append("\n\n---\n\n");
        }
        return sb.toString();
    }

    /**
     * 关键词搜索知识块
     */
    public List<KnowledgeChunk> searchChunks(Long courseId, String keyword) {
        return chunkMapper.selectList(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(courseId != null, KnowledgeChunk::getCourseId, courseId)
                .like(KnowledgeChunk::getContent, keyword)
                .orderByAsc(KnowledgeChunk::getChunkIndex)
                .last("LIMIT 20"));
    }

    private String getExtension(String filename) {
        if (filename == null) return "unknown";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1) : "unknown";
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}

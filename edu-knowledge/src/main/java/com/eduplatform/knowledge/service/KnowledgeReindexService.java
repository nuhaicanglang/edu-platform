package com.eduplatform.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduplatform.knowledge.domain.entity.KnowledgeDocument;
import com.eduplatform.knowledge.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 可重复执行的知识索引重建任务。 */
@Service
@RequiredArgsConstructor
public class KnowledgeReindexService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeIndexingService indexingService;
    private final Map<String, ReindexTaskStatus> tasks = new ConcurrentHashMap<>();

    public ReindexTaskStatus start(Long courseId) {
        String taskId = UUID.randomUUID().toString();
        List<KnowledgeDocument> documents = documentMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(courseId != null, KnowledgeDocument::getCourseId, courseId)
                        .orderByAsc(KnowledgeDocument::getId));
        tasks.put(taskId, new ReindexTaskStatus(taskId, "running", documents.size(), 0, 0, List.of()));

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        for (KnowledgeDocument document : documents) {
            indexingService.indexDocument(document.getId());
            KnowledgeDocument refreshed = documentMapper.selectById(document.getId());
            if (refreshed != null && "ready".equals(refreshed.getIndexStatus())) {
                success++;
            } else {
                failed++;
                errors.add("documentId=" + document.getId() + ": "
                        + (refreshed == null ? "文档不存在" : refreshed.getIndexError()));
            }
        }
        ReindexTaskStatus completed = new ReindexTaskStatus(
                taskId, failed == 0 ? "completed" : "completed-with-errors",
                documents.size(), success, failed, List.copyOf(errors));
        tasks.put(taskId, completed);
        return completed;
    }

    public ReindexTaskStatus get(String taskId) {
        return tasks.get(taskId);
    }

    public record ReindexTaskStatus(
            String taskId, String status, int total, int success, int failed, List<String> errors) {
    }
}

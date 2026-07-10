package com.eduplatform.knowledge.controller;

import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.core.domain.R;
import com.eduplatform.knowledge.domain.entity.KnowledgeChunk;
import com.eduplatform.knowledge.domain.entity.KnowledgeDocument;
import com.eduplatform.knowledge.service.KnowledgeBaseService;
import com.eduplatform.knowledge.service.KnowledgeRetrievalService;
import com.eduplatform.knowledge.service.KnowledgeReindexService;
import com.eduplatform.knowledge.security.KnowledgeAuthorizationService;
import com.eduplatform.knowledge.controller.dto.RetrievalRequest;
import com.eduplatform.knowledge.controller.dto.RetrievalResponse;
import com.eduplatform.common.annotation.RequireRole;
import com.eduplatform.common.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库管理控制器
 * <p>
 * 提供文档上传解析、分页查询、删除、分块查看和关键词搜索等接口。
 * 上传的文档自动提取文本并分块存储，供 AI 问答系统检索使用。
 * </p>
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeRetrievalService retrievalService;
    private final KnowledgeReindexService reindexService;
    private final KnowledgeAuthorizationService authorizationService;

    /** 上传文档并解析入库（支持 TXT/MD/PDF/DOCX） */
    @RequireRole({"teacher", "admin"})
    @PostMapping("/upload")
    public R<KnowledgeDocument> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam("courseId") Long courseId,
                                        @RequestHeader("X-User-Id") Long userId,
                                        @RequestHeader("X-User-Role") String role) {
        authorizationService.requireCourseManager(courseId, userId, role);
        return R.ok(knowledgeBaseService.uploadAndParse(file, courseId, userId));
    }

    /** 分页查询知识库文档（可按课程ID过滤） */
    @GetMapping("/documents")
    public R<PageResult<KnowledgeDocument>> pageDocuments(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        authorizationService.requireOptionalCourseScope(courseId, userId, role);
        return R.ok(knowledgeBaseService.pageDocuments(pageNum, pageSize, courseId));
    }

    /** 获取文档详情 */
    @GetMapping("/documents/{id}")
    public R<KnowledgeDocument> getDocument(@PathVariable Long id,
                                             @RequestHeader("X-User-Id") Long userId,
                                             @RequestHeader("X-User-Role") String role) {
        authorizationService.requireDocumentAccess(id, userId, role);
        return R.ok(knowledgeBaseService.getDocument(id));
    }

    /** 删除文档及其所有分块 */
    @RequireRole({"teacher", "admin"})
    @DeleteMapping("/documents/{id}")
    public R<Void> deleteDocument(@PathVariable Long id,
                                  @RequestHeader("X-User-Id") Long userId,
                                  @RequestHeader("X-User-Role") String role) {
        authorizationService.requireDocumentManager(id, userId, role);
        knowledgeBaseService.deleteDocument(id);
        return R.ok();
    }

    /** 获取文档的所有知识分块 */
    @GetMapping("/documents/{id}/chunks")
    public R<List<KnowledgeChunk>> getChunks(@PathVariable Long id,
                                             @RequestHeader("X-User-Id") Long userId,
                                             @RequestHeader("X-User-Role") String role) {
        authorizationService.requireDocumentAccess(id, userId, role);
        return R.ok(knowledgeBaseService.getChunks(id));
    }

    /** 获取课程知识上下文（供 AI 问答系统使用） */
    @GetMapping("/course/{courseId}/context")
    public R<String> getCourseContext(@PathVariable Long courseId,
                                      @RequestParam(value = "maxChunks", defaultValue = "20") int maxChunks,
                                      @RequestHeader("X-User-Id") Long userId,
                                      @RequestHeader("X-User-Role") String role) {
        authorizationService.requireCourseAccess(courseId, userId, role);
        return R.ok(knowledgeBaseService.getCourseKnowledgeContext(courseId, maxChunks));
    }

    /** 关键词搜索知识分块 */
    @GetMapping("/search")
    public R<List<KnowledgeChunk>> search(@RequestParam(value = "courseId", required = false) Long courseId,
                                           @RequestParam("keyword") String keyword,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestHeader("X-User-Role") String role) {
        authorizationService.requireCourseAccess(courseId, userId, role);
        return R.ok(knowledgeBaseService.searchChunks(courseId, keyword));
    }

    /** 执行课程隔离的向量 + BM25 混合检索。 */
    @PostMapping("/retrieve")
    public R<RetrievalResponse> retrieve(
            @Valid @RequestBody RetrievalRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        authorizationService.requireCourseAccess(request.courseId(), userId, role);
        return R.ok(new RetrievalResponse(
                "hybrid", retrievalService.retrieve(request.question(), request.courseId())));
    }

    /** 管理员触发全量或单课程重建。 */
    @RequireRole("admin")
    @PostMapping("/admin/reindex")
    public R<KnowledgeReindexService.ReindexTaskStatus> reindex(
            @RequestParam(value = "courseId", required = false) Long courseId) {
        return R.ok(reindexService.start(courseId));
    }

    @RequireRole("admin")
    @GetMapping("/admin/reindex/{taskId}")
    public R<KnowledgeReindexService.ReindexTaskStatus> reindexStatus(@PathVariable String taskId) {
        KnowledgeReindexService.ReindexTaskStatus status = reindexService.get(taskId);
        if (status == null) throw new BusinessException(404, "重建任务不存在");
        return R.ok(status);
    }
}

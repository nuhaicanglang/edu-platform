package com.eduplatform.knowledge.controller;

import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.core.domain.R;
import com.eduplatform.knowledge.domain.entity.KnowledgeChunk;
import com.eduplatform.knowledge.domain.entity.KnowledgeDocument;
import com.eduplatform.knowledge.service.KnowledgeBaseService;
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

    /** 上传文档并解析入库（支持 TXT/MD/PDF/DOCX） */
    @PostMapping("/upload")
    public R<KnowledgeDocument> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam("courseId") Long courseId,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return R.ok(knowledgeBaseService.uploadAndParse(file, courseId, userId != null ? userId : 0L));
    }

    /** 分页查询知识库文档（可按课程ID过滤） */
    @GetMapping("/documents")
    public R<PageResult<KnowledgeDocument>> pageDocuments(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "courseId", required = false) Long courseId) {
        return R.ok(knowledgeBaseService.pageDocuments(pageNum, pageSize, courseId));
    }

    /** 获取文档详情 */
    @GetMapping("/documents/{id}")
    public R<KnowledgeDocument> getDocument(@PathVariable Long id) {
        return R.ok(knowledgeBaseService.getDocument(id));
    }

    /** 删除文档及其所有分块 */
    @DeleteMapping("/documents/{id}")
    public R<Void> deleteDocument(@PathVariable Long id) {
        knowledgeBaseService.deleteDocument(id);
        return R.ok();
    }

    /** 获取文档的所有知识分块 */
    @GetMapping("/documents/{id}/chunks")
    public R<List<KnowledgeChunk>> getChunks(@PathVariable Long id) {
        return R.ok(knowledgeBaseService.getChunks(id));
    }

    /** 获取课程知识上下文（供 AI 问答系统使用） */
    @GetMapping("/course/{courseId}/context")
    public R<String> getCourseContext(@PathVariable Long courseId,
                                      @RequestParam(value = "maxChunks", defaultValue = "20") int maxChunks) {
        return R.ok(knowledgeBaseService.getCourseKnowledgeContext(courseId, maxChunks));
    }

    /** 关键词搜索知识分块 */
    @GetMapping("/search")
    public R<List<KnowledgeChunk>> search(@RequestParam(value = "courseId", required = false) Long courseId,
                                           @RequestParam("keyword") String keyword) {
        return R.ok(knowledgeBaseService.searchChunks(courseId, keyword));
    }
}

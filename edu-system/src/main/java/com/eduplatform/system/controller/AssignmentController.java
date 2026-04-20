package com.eduplatform.system.controller;

import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.core.domain.R;
import com.eduplatform.system.domain.entity.Assignment;
import com.eduplatform.system.domain.entity.AssignmentSubmission;
import com.eduplatform.system.service.AssignmentService;
import com.eduplatform.system.service.FileUploadService;
import com.eduplatform.system.service.LearningRecordService;
import com.eduplatform.common.annotation.Log;
import com.eduplatform.common.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 作业管理控制器
 * <p>
 * 提供作业 CRUD、发布、学生提交、AI 智能批改（异步）、批改状态轮询、
 * 独立快速批改等接口。AI 批改采用 @Async 异步架构，接口立即返回，
 * 前端通过轮询 grading-status 获取批改结果。
 * </p>
 */
@RestController
@RequestMapping("/system/assignment")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final FileUploadService fileUploadService;
    private final LearningRecordService learningRecordService;

    /** 分页查询作业（支持按课程ID、班级ID过滤） */
    @GetMapping("/page")
    public R<PageResult<Assignment>> page(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "classId", required = false) Long classId) {
        return R.ok(assignmentService.page(pageNum, pageSize, courseId, classId));
    }

    /** 根据 ID 查询作业详情 */
    @GetMapping("/{id}")
    public R<Assignment> getById(@PathVariable Long id) {
        return R.ok(assignmentService.getById(id));
    }

    /** 创建作业（JSON Body，自动绑定教师ID） */
    @Log(module = "作业管理", value = "创建作业")
    @RequireRole({"teacher", "admin"})
    @PostMapping
    public R<Void> create(@RequestBody Assignment assignment, @RequestHeader("X-User-Id") Long userId) {
        assignment.setTeacherId(userId);
        assignmentService.create(assignment);
        return R.ok();
    }

    /** 创建作业并上传附件（multipart form） */
    @Log(module = "作业管理", value = "创建作业(带附件)")
    @RequireRole({"teacher", "admin"})
    @PostMapping("/create-with-file")
    public R<Assignment> createWithFile(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "assignmentType", defaultValue = "homework") String assignmentType,
            @RequestParam(value = "totalScore", defaultValue = "100") Integer totalScore,
            @RequestParam(value = "status", defaultValue = "published") String status,
            @RequestParam(value = "aiGradingEnabled", defaultValue = "true") Boolean aiGradingEnabled,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) {

        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setCourseId(courseId);
        assignment.setClassId(classId);
        assignment.setAssignmentType(assignmentType);
        assignment.setTotalScore(totalScore);
        assignment.setStatus(status);
        assignment.setAiGradingEnabled(aiGradingEnabled);
        assignment.setTeacherId(userId);

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileUploadService.upload(file, "assignments");
            assignment.setAttachmentUrl(fileUrl);
            assignment.setAttachmentName(file.getOriginalFilename());
        }

        assignmentService.create(assignment);
        return R.ok(assignment);
    }

    /** 更新作业信息 */
    @Log(module = "作业管理", value = "更新作业")
    @RequireRole({"teacher", "admin"})
    @PutMapping
    public R<Void> update(@RequestBody Assignment assignment) {
        assignmentService.update(assignment);
        return R.ok();
    }

    /** 删除作业（逻辑删除） */
    @Log(module = "作业管理", value = "删除作业")
    @RequireRole({"teacher", "admin"})
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return R.ok();
    }

    /** 发布作业（状态从 draft 变为 published） */
    @Log(module = "作业管理", value = "发布作业")
    @RequireRole({"teacher", "admin"})
    @PostMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        assignmentService.publish(id);
        return R.ok();
    }

    // ========================= 学生提交 =========================

    /** 学生提交作业（支持文本 + 文件上传，重复提交会覆盖） */
    @Log(module = "作业提交", value = "学生提交作业")
    @PostMapping("/submit")
    public R<Void> submit(@RequestParam("assignmentId") Long assignmentId,
                          @RequestParam(value = "content", required = false) String content,
                          @RequestParam(value = "file", required = false) MultipartFile file,
                          @RequestHeader("X-User-Id") Long userId) {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(userId);
        submission.setContent(content);

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileUploadService.upload(file, "submissions");
            submission.setFileUrl(fileUrl);
            submission.setFileName(file.getOriginalFilename());
        }

        assignmentService.submit(submission);
        learningRecordService.recordSubmit(userId, null, "提交作业: assignmentId=" + assignmentId, null);
        return R.ok();
    }

    /** 分页查询某作业的提交记录 */
    @GetMapping("/{assignmentId}/submissions")
    public R<PageResult<AssignmentSubmission>> pageSubmissions(
            @PathVariable Long assignmentId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(assignmentService.pageSubmissions(pageNum, pageSize, assignmentId));
    }

    /** 获取作业下全部提交（含学生姓名） */
    @GetMapping("/{assignmentId}/all-submissions")
    public R<List<AssignmentSubmission>> listAllSubmissions(@PathVariable Long assignmentId) {
        return R.ok(assignmentService.listSubmissionsWithStudentName(assignmentId));
    }

    /**
     * 触发 AI 批改（异步）：立即返回 status=grading，前端轮询 /submission/{id}/grading-status
     */
    @PostMapping("/{assignmentId}/ai-grade/{submissionId}")
    public R<AssignmentSubmission> aiGrade(@PathVariable Long assignmentId,
                                           @PathVariable Long submissionId) {
        return R.ok(assignmentService.aiGradeSubmission(assignmentId, submissionId));
    }

    /**
     * 轮询批改状态（前端每3秒调一次，直到 status=completed/failed）
     */
    @GetMapping("/submission/{submissionId}/grading-status")
    public R<AssignmentSubmission> gradingStatus(@PathVariable Long submissionId) {
        return R.ok(assignmentService.getGradingStatus(submissionId));
    }

    /** 查询单个提交详情 */
    @GetMapping("/submission/{submissionId}")
    public R<AssignmentSubmission> getSubmission(@PathVariable Long submissionId) {
        return R.ok(assignmentService.getSubmission(submissionId));
    }

    /** 学生查询自己的提交记录 */
    @GetMapping("/{assignmentId}/my-submission")
    public R<AssignmentSubmission> mySubmission(@PathVariable Long assignmentId,
                                                @RequestHeader("X-User-Id") Long userId) {
        return R.ok(assignmentService.getStudentSubmission(assignmentId, userId));
    }

    /** 独立AI批改：直接提交文件或文字，无需关联作业/提交记录 */
    @PostMapping("/quick-grade")
    public R<java.util.Map<String, Object>> quickGrade(
            @RequestParam(value = "assignmentTitle", defaultValue = "") String title,
            @RequestParam(value = "assignmentRequirement", defaultValue = "") String requirement,
            @RequestParam(value = "referenceAnswer", required = false) String referenceAnswer,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String fileRelPath = null;
        String originalFileName = null;
        if (file != null && !file.isEmpty()) {
            fileRelPath = fileUploadService.upload(file, "quick-grade");
            originalFileName = file.getOriginalFilename();
        }
        java.util.Map<String, Object> result = assignmentService.quickGrade(
                title, requirement, referenceAnswer, content, fileRelPath, originalFileName);
        return R.ok(result);
    }
}

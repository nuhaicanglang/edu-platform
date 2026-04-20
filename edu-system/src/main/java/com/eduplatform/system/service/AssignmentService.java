package com.eduplatform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.system.domain.entity.Assignment;
import com.eduplatform.system.domain.entity.AssignmentSubmission;
import com.eduplatform.system.mapper.AssignmentMapper;
import com.eduplatform.system.mapper.AssignmentSubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduplatform.system.util.GradingJsonParser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业业务服务
 * <p>
 * 提供作业 CRUD、学生提交、AI 异步批改、批改状态查询、独立快速批改等功能。
 * AI 批改通过 {@link AsyncGradingService} 在独立线程池中异步执行，
 * 接口立即返回，前端轮询结果。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmissionMapper submissionMapper;
    private final DocxProcessingService docxProcessingService;
    private final AgentClientService agentClientService;
    private final JdbcTemplate jdbcTemplate;
    private final AsyncGradingService asyncGradingService;

    /** 分页查询作业（支持课程ID、班级ID过滤） */
    public PageResult<Assignment> page(int pageNum, int pageSize, Long courseId, Long classId) {
        Page<Assignment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId != null, Assignment::getCourseId, courseId);
        wrapper.eq(classId != null, Assignment::getClassId, classId);
        wrapper.orderByDesc(Assignment::getCreateTime);
        Page<Assignment> result = assignmentMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    /** 根据 ID 查询作业详情 */
    public Assignment getById(Long id) {
        Assignment a = assignmentMapper.selectById(id);
        if (a == null) throw new BusinessException("作业不存在");
        return a;
    }

    /** 创建作业（默认状态 draft） */
    public void create(Assignment assignment) {
        if (assignment.getStatus() == null) assignment.setStatus("draft");
        assignmentMapper.insert(assignment);
    }

    /** 更新作业 */
    public void update(Assignment assignment) {
        assignmentMapper.updateById(assignment);
    }

    /** 删除作业（逻辑删除） */
    public void delete(Long id) {
        assignmentMapper.deleteById(id);
    }

    /** 发布作业（draft → published） */
    public void publish(Long id) {
        Assignment a = getById(id);
        a.setStatus("published");
        assignmentMapper.updateById(a);
    }

    // ========================= 学生提交 =========================

    /**
     * 学生提交作业（重复提交自动覆盖）
     * <p>
     * 使用 @Transactional + SELECT FOR UPDATE 防止同一学生快速双击导致的 check-then-act 竞态，
     * 避免唯一索引冲突抛 DuplicateKeyException。
     * 重新提交时用原生 SQL 显式清空旧批改结果（MyBatis-Plus updateById 默认不更新 null 字段）。
     * </p>
     */
    @Transactional
    public void submit(AssignmentSubmission submission) {
        submission.setSubmitTime(LocalDateTime.now());
        submission.setGradingStatus("submitted");

        // SELECT FOR UPDATE：悲观锁，串行化同一学生对同一作业的提交操作
        Long existingId;
        try {
            existingId = jdbcTemplate.queryForObject(
                    "SELECT id FROM assignment_submission " +
                            "WHERE assignment_id = ? AND student_id = ? AND deleted = 0 " +
                            "LIMIT 1 FOR UPDATE",
                    Long.class,
                    submission.getAssignmentId(), submission.getStudentId());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            existingId = null;
        }

        if (existingId != null) {
            // 用原生 SQL 更新：updateById 默认策略不更新 null 字段，无法清空旧批改结果
            jdbcTemplate.update(
                    "UPDATE assignment_submission SET " +
                            "content = ?, file_url = ?, file_name = ?, submit_time = ?, " +
                            "grading_status = 'submitted', " +
                            "score = NULL, ai_comment = NULL, grading_result = NULL, annotated_file_url = NULL, " +
                            "grade_time = NULL " +
                            "WHERE id = ?",
                    submission.getContent(),
                    submission.getFileUrl(),
                    submission.getFileName(),
                    submission.getSubmitTime(),
                    existingId);
        } else {
            submissionMapper.insert(submission);
        }
    }

    /** 查询单个提交记录 */
    public AssignmentSubmission getSubmission(Long submissionId) {
        return submissionMapper.selectById(submissionId);
    }

    /** 查询某作业的所有提交记录 */
    public List<AssignmentSubmission> listSubmissions(Long assignmentId) {
        return submissionMapper.selectList(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                        .orderByDesc(AssignmentSubmission::getSubmitTime));
    }

    /** 查询某学生在某作业的最新提交 */
    public AssignmentSubmission getStudentSubmission(Long assignmentId, Long studentId) {
        return submissionMapper.selectOne(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                        .eq(AssignmentSubmission::getStudentId, studentId)
                        .orderByDesc(AssignmentSubmission::getSubmitTime)
                        .last("LIMIT 1"));
    }

    /** 更新提交记录 */
    public void updateSubmission(AssignmentSubmission submission) {
        submissionMapper.updateById(submission);
    }

    /** 分页查询提交记录 */
    public PageResult<AssignmentSubmission> pageSubmissions(int pageNum, int pageSize, Long assignmentId) {
        Page<AssignmentSubmission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        wrapper.orderByDesc(AssignmentSubmission::getSubmitTime);
        Page<AssignmentSubmission> result = submissionMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取作业下所有提交（附加学生姓名）
     */
    public List<AssignmentSubmission> listSubmissionsWithStudentName(Long assignmentId) {
        List<AssignmentSubmission> list = listSubmissions(assignmentId);
        list.forEach(s -> {
            try {
                s.setStudentName(getStudentName(s.getStudentId()));
            } catch (Exception ignored) {}
        });
        return list;
    }

    /** 根据用户ID查询学生姓名（优先 real_name，否则 username） */
    public String getStudentName(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COALESCE(real_name, username) FROM sys_user WHERE id = ?",
                String.class, userId);
        } catch (Exception e) {
            return "学生" + userId;
        }
    }

    /**
     * AI智能批改作业提交（异步非阻塞）
     * 立即返回 gradingStatus=grading，实际批改在后台线程池执行
     * 前端通过 GET /submission/{id}/grading-status 轮询结果
     */
    public AssignmentSubmission aiGradeSubmission(Long assignmentId, Long submissionId) {
        AssignmentSubmission submission = getSubmission(submissionId);
        if (submission == null) throw new BusinessException("提交记录不存在");

        // 立即标记为批改中，持久化后返回给前端
        submission.setGradingStatus("grading");
        submissionMapper.updateById(submission);

        // 触发异步批改（在 gradingExecutor 线程池中执行，不阻塞当前请求线程）
        asyncGradingService.gradeAsync(assignmentId, submissionId);

        log.info("[Grading] 异步批改已触发: submissionId={}", submissionId);
        return submission;
    }

    /**
     * 查询批改状态（前端轮询用）
     */
    public AssignmentSubmission getGradingStatus(Long submissionId) {
        AssignmentSubmission s = submissionMapper.selectById(submissionId);
        if (s == null) throw new BusinessException("提交记录不存在");
        return s;
    }

    /**
     * 独立AI批改（无需已有作业/提交记录）：可传入文件路径或纯文本
     * @return Map含 gradingJson, annotatedFileUrl(可能为null), score, overallComment
     */
    public java.util.Map<String, Object> quickGrade(
            String title, String requirement, String referenceAnswer,
            String textContent, String fileRelPath, String originalFileName) {

        try {
            // 1. 获取学生内容
            String studentText;
            if (fileRelPath != null && !fileRelPath.isBlank()) {
                studentText = docxProcessingService.extractText(fileRelPath);
            } else if (textContent != null && !textContent.isBlank()) {
                studentText = textContent;
            } else {
                throw new BusinessException("请提供文字内容或上传文件");
            }

            // 2. AI批改
            String gradingJson = agentClientService.gradeTextAssignment(
                    title, requirement, studentText, referenceAnswer);

            // 3. 生成批注文档（仅当有文件时）
            String annotatedPath = null;
            if (fileRelPath != null && !fileRelPath.isBlank()) {
                try {
                    annotatedPath = docxProcessingService.createAnnotatedDocx(
                            fileRelPath, gradingJson, originalFileName);
                } catch (Exception e) {
                    log.warn("独立批改生成批注文档失败: {}", e.getMessage());
                }
            }

            int score = GradingJsonParser.parseScore(gradingJson);
            String comment = GradingJsonParser.parseOverallComment(gradingJson);

            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("gradingJson", gradingJson);
            result.put("annotatedFileUrl", annotatedPath);
            result.put("score", score);
            result.put("overallComment", comment);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("独立AI批改失败: {}", e.getMessage(), e);
            throw new BusinessException("AI批改失败: " + e.getMessage());
        }
    }

}

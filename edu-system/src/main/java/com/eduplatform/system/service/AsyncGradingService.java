package com.eduplatform.system.service;

import com.eduplatform.system.domain.entity.Assignment;
import com.eduplatform.system.domain.entity.AssignmentSubmission;
import com.eduplatform.system.mapper.AssignmentMapper;
import com.eduplatform.system.mapper.AssignmentSubmissionMapper;
import com.eduplatform.system.util.GradingJsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AI 批改异步执行服务
 * 必须独立为单独的 Spring Bean，避免 @Async 在同类自调用时 Spring 代理失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncGradingService {

    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmissionMapper submissionMapper;
    private final DocxProcessingService docxProcessingService;
    private final AgentClientService agentClientService;

    /**
     * 异步执行 AI 批改，在独立线程池 gradingExecutor 中运行
     * 调用方立即返回，本方法在后台执行，结果写回数据库
     */
    @Async("gradingExecutor")
    public void gradeAsync(Long assignmentId, Long submissionId) {
        log.info("[AsyncGrading] 开始批改: assignmentId={}, submissionId={}", assignmentId, submissionId);

        AssignmentSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            log.warn("[AsyncGrading] 提交记录不存在: {}", submissionId);
            return;
        }

        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            markFailed(submission, "作业不存在");
            return;
        }

        try {
            // 1. 提取学生提交文本
            String studentText;
            if (submission.getFileUrl() != null && !submission.getFileUrl().isBlank()) {
                studentText = docxProcessingService.extractText(submission.getFileUrl());
            } else if (submission.getContent() != null && !submission.getContent().isBlank()) {
                studentText = submission.getContent();
            } else {
                markFailed(submission, "提交内容为空，无法批改");
                return;
            }

            // 2. 调用 AI 批改（此处为耗时操作，在独立线程中不阻塞主线程）
            String gradingJson = agentClientService.gradeTextAssignment(
                    assignment.getTitle(),
                    assignment.getDescription(),
                    studentText,
                    null);

            // 3. 生成带批注 Word 文档
            String annotatedPath = null;
            if (submission.getFileUrl() != null && !submission.getFileUrl().isBlank()) {
                try {
                    annotatedPath = docxProcessingService.createAnnotatedDocx(
                            submission.getFileUrl(), gradingJson, submission.getFileName());
                } catch (Exception e) {
                    log.warn("[AsyncGrading] 生成批注文档失败: {}", e.getMessage());
                }
            }

            // 4. 解析分数和总评
            int score = GradingJsonParser.parseScore(gradingJson);
            String comment = GradingJsonParser.parseOverallComment(gradingJson);

            // 5. 写回数据库
            submission.setGradingStatus("completed");
            submission.setGradingResult(gradingJson);
            submission.setScore(score);
            submission.setAiComment(comment);
            if (annotatedPath != null) submission.setAnnotatedFileUrl(annotatedPath);
            submissionMapper.updateById(submission);

            log.info("[AsyncGrading] 批改完成: submissionId={}, score={}", submissionId, score);

        } catch (Exception e) {
            log.error("[AsyncGrading] 批改失败: submissionId={}, error={}", submissionId, e.getMessage(), e);
            markFailed(submission, e.getMessage());
        }
    }

    private void markFailed(AssignmentSubmission submission, String reason) {
        submission.setGradingStatus("failed");
        submission.setAiComment("批改失败：" + reason);
        submissionMapper.updateById(submission);
    }

}

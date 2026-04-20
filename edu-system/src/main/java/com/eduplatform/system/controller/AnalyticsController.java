package com.eduplatform.system.controller;

import com.eduplatform.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学情数据接口
 * <p>
 * 提供学生个人学情和班级学情的真实数据查询，
 * 返回结果将作为 AI 学情分析的数据源（DB 真实数据 → AI 分析）。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/system/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final JdbcTemplate jdbc;

    /**
     * 查询某学生在某课程的真实学习数据
     * GET /system/analytics/student-data?studentName=Li Student&courseName=数据结构与算法
     */
    @GetMapping("/student-data")
    public R<Map<String, Object>> studentData(
            @RequestParam("studentName") String studentName,
            @RequestParam("courseName") String courseName) {
      try {
        // 1. 查学生
        List<Map<String, Object>> users = jdbc.queryForList(
            "SELECT id, username, real_name FROM sys_user WHERE real_name = ? OR username = ? LIMIT 1",
            studentName, studentName);
        if (users.isEmpty()) return R.fail("找不到学生: " + studentName);
        Long studentId = ((Number) users.get(0).get("id")).longValue();
        String realName = (String) users.get(0).get("real_name");

        // 2. 查课程
        List<Map<String, Object>> courses = jdbc.queryForList(
            "SELECT id FROM course WHERE course_name = ? LIMIT 1", courseName);
        if (courses.isEmpty()) return R.fail("找不到课程: " + courseName);
        Long courseId = ((Number) courses.get(0).get("id")).longValue();

        // 3. 查该课程所有作业
        List<Map<String, Object>> assignments = jdbc.queryForList(
            "SELECT id, title FROM assignment WHERE course_id = ? AND deleted = 0", courseId);

        // 4. 查该学生在该课程的提交
        List<Map<String, Object>> submissions = jdbc.queryForList(
            "SELECT s.assignment_id, s.score, s.status, s.submit_time, s.ai_comment, a.title " +
            "FROM assignment_submission s " +
            "JOIN assignment a ON s.assignment_id = a.id " +
            "WHERE s.student_id = ? AND a.course_id = ? AND s.deleted = 0 " +
            "ORDER BY s.submit_time",
            studentId, courseId);

        // 5. 统计
        int totalAssignments = assignments.size();
        int submittedCount = submissions.size();
        int gradedCount = (int) submissions.stream()
            .filter(s -> s.get("score") != null).count();
        List<Integer> scores = new ArrayList<>();
        for (Map<String, Object> s : submissions) {
            if (s.get("score") != null) scores.add(((Number) s.get("score")).intValue());
        }
        double avgScore = scores.isEmpty() ? 0 : scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        int maxScore = scores.isEmpty() ? 0 : scores.stream().mapToInt(Integer::intValue).max().orElse(0);
        int minScore = scores.isEmpty() ? 0 : scores.stream().mapToInt(Integer::intValue).min().orElse(0);

        // 成绩趋势（最近5条）
        List<Map<String, Object>> recent = submissions.size() > 5
            ? submissions.subList(submissions.size() - 5, submissions.size()) : submissions;

        // 6. 组装返回
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentName", realName);
        data.put("courseName", courseName);
        data.put("totalAssignments", totalAssignments);
        data.put("submittedCount", submittedCount);
        data.put("gradedCount", gradedCount);
        data.put("completionRate", totalAssignments == 0 ? "0%" :
            String.format("%.0f%%", submittedCount * 100.0 / totalAssignments));
        data.put("averageScore", String.format("%.1f", avgScore));
        data.put("maxScore", maxScore);
        data.put("minScore", minScore);
        data.put("scores", scores);
        data.put("scoreTrend", buildScoreTrend(recent));
        data.put("submissionDetails", buildDetails(submissions));
        // 成绩趋势文字描述
        data.put("trendDescription", describeTrend(scores));

        return R.ok(data);
      } catch (Exception e) {
        log.error("studentData error", e);
        return R.fail("学情查询失败: " + e.getMessage());
      }
    }

    /**
     * 查询某班级在某课程的真实学习数据
     * GET /system/analytics/class-data?className=X&courseName=Y
     */
    @GetMapping("/class-data")
    public R<Map<String, Object>> classData(
            @RequestParam("className") String className,
            @RequestParam("courseName") String courseName) {
      try {
        // 查班级
        List<Map<String, Object>> classes = jdbc.queryForList(
            "SELECT id, class_name FROM class_group WHERE class_name = ? LIMIT 1", className);
        if (classes.isEmpty()) return R.fail("找不到班级: " + className);
        Long classId = ((Number) classes.get(0).get("id")).longValue();

        // 查该班级学生
        List<Map<String, Object>> students = jdbc.queryForList(
            "SELECT u.id, COALESCE(u.real_name, u.username) as name " +
            "FROM class_student cs JOIN sys_user u ON cs.student_id = u.id " +
            "WHERE cs.class_id = ?", classId);

        // 查课程
        List<Map<String, Object>> courses = jdbc.queryForList(
            "SELECT id FROM course WHERE course_name = ? LIMIT 1", courseName);
        if (courses.isEmpty()) return R.fail("找不到课程: " + courseName);
        Long courseId = ((Number) courses.get(0).get("id")).longValue();

        // 查所有提交
        List<Map<String, Object>> submissions = jdbc.queryForList(
            "SELECT s.student_id, s.score, s.status, u.real_name as student_name " +
            "FROM assignment_submission s " +
            "JOIN assignment a ON s.assignment_id = a.id " +
            "JOIN sys_user u ON s.student_id = u.id " +
            "WHERE a.course_id = ? AND s.deleted = 0 AND s.score IS NOT NULL", courseId);

        List<Integer> allScores = new ArrayList<>();
        for (Map<String, Object> s : submissions) {
            if (s.get("score") != null) allScores.add(((Number) s.get("score")).intValue());
        }
        double avg = allScores.isEmpty() ? 0 : allScores.stream().mapToInt(i -> i).average().orElse(0);
        int max = allScores.isEmpty() ? 0 : allScores.stream().mapToInt(i -> i).max().orElse(0);
        int min = allScores.isEmpty() ? 0 : allScores.stream().mapToInt(i -> i).min().orElse(0);
        long passCount = allScores.stream().filter(s -> s >= 60).count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("className", className);
        data.put("courseName", courseName);
        data.put("studentCount", students.size());
        data.put("gradedSubmissions", allScores.size());
        data.put("averageScore", String.format("%.1f", avg));
        data.put("maxScore", max);
        data.put("minScore", min);
        data.put("passRate", allScores.isEmpty() ? "0%" :
            String.format("%.0f%%", passCount * 100.0 / allScores.size()));
        data.put("scoreDistribution", buildDistribution(allScores));

        return R.ok(data);
      } catch (Exception e) {
        log.error("classData error", e);
        return R.fail("班级学情查询失败: " + e.getMessage());
      }
    }

    /** 构建最近成绩趋势文本 */
    private String buildScoreTrend(List<Map<String, Object>> recent) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> s : recent) {
            sb.append(s.get("title")).append(": ");
            sb.append(s.get("score") != null ? s.get("score") + "分" : "未批改");
            sb.append("; ");
        }
        return sb.toString();
    }

    /** 构建提交详情列表 */
    private List<Map<String, Object>> buildDetails(List<Map<String, Object>> submissions) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> s : submissions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", s.get("title"));
            item.put("score", s.get("score"));
            item.put("status", s.get("status"));
            item.put("aiComment", s.get("ai_comment"));
            list.add(item);
        }
        return list;
    }

    /** 构建成绩分布图（90-100/80-89/70-79/60-69/60以下） */
    private Map<String, Integer> buildDistribution(List<Integer> scores) {
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("90-100", (int) scores.stream().filter(s -> s >= 90).count());
        dist.put("80-89",  (int) scores.stream().filter(s -> s >= 80 && s < 90).count());
        dist.put("70-79",  (int) scores.stream().filter(s -> s >= 70 && s < 80).count());
        dist.put("60-69",  (int) scores.stream().filter(s -> s >= 60 && s < 70).count());
        dist.put("60以下", (int) scores.stream().filter(s -> s < 60).count());
        return dist;
    }

    /**
     * 查询学生学习行为记录
     * GET /system/analytics/learning-records?pageNum=1&pageSize=20
     */
    @GetMapping("/learning-records")
    public R<Map<String, Object>> learningRecords(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        if (userId == null) return R.ok(Map.of("records", List.of(), "total", 0));

        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> records = jdbc.queryForList(
            "SELECT lr.id, lr.student_id, lr.course_id, lr.action_type, lr.action_detail, " +
            "lr.score, lr.duration, lr.create_time, c.course_name " +
            "FROM learning_record lr LEFT JOIN course c ON lr.course_id = c.id " +
            "WHERE lr.student_id = ? AND lr.deleted = 0 ORDER BY lr.create_time DESC LIMIT ? OFFSET ?",
            userId, pageSize, offset);

        Long total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM learning_record WHERE student_id = ? AND deleted = 0", Long.class, userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total != null ? total : 0);
        return R.ok(data);
    }

    /**
     * 查询全部学习行为记录（教师/管理员）
     * GET /system/analytics/all-learning-records?pageNum=1&pageSize=20
     */
    @GetMapping("/all-learning-records")
    public R<Map<String, Object>> allLearningRecords(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> records = jdbc.queryForList(
            "SELECT lr.id, lr.student_id, lr.course_id, lr.action_type, lr.action_detail, " +
            "lr.score, lr.duration, lr.create_time, c.course_name, u.real_name as student_name " +
            "FROM learning_record lr LEFT JOIN course c ON lr.course_id = c.id " +
            "LEFT JOIN sys_user u ON lr.student_id = u.id " +
            "WHERE lr.deleted = 0 ORDER BY lr.create_time DESC LIMIT ? OFFSET ?",
            pageSize, offset);

        Long total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM learning_record WHERE deleted = 0", Long.class);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total != null ? total : 0);
        return R.ok(data);
    }

    /** 生成成绩趋势描述文本 */
    private String describeTrend(List<Integer> scores) {
        if (scores.size() < 2) return scores.isEmpty() ? "暂无成绩" : "仅一次成绩: " + scores.get(0) + "分";
        int first = scores.get(0), last = scores.get(scores.size() - 1);
        if (last > first + 5) return "持续上升";
        if (last < first - 5) return "连续下降";
        return "基本稳定";
    }
}

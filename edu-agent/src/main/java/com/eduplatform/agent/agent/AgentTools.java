package com.eduplatform.agent.agent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI Agent 工具集 - LLM 可自主决策调用的函数
 * <p>
 * 每个 {@code @Tool} 方法都是一个工具，LLM 根据用户问题自主规划调用顺序和参数，
 * 无需 Java 代码硬编码流程。这是真正 AI Agent 与普通 LLM 调用的核心区别。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentTools {

    private final JdbcTemplate jdbcTemplate;

    @Tool("搜索指定课程的知识库，根据关键词查找相关知识内容片段，用于回答与课程知识相关的问题")
    public String searchKnowledge(
            @P("课程ID，如果不知道可以传0表示搜索所有课程") long courseId,
            @P("搜索关键词，尽量精炼") String keyword) {
        try {
            String sql = courseId > 0
                    ? "SELECT content FROM knowledge_chunk WHERE course_id = ? AND content LIKE ? LIMIT 5"
                    : "SELECT content FROM knowledge_chunk WHERE content LIKE ? LIMIT 5";
            List<Map<String, Object>> rows = courseId > 0
                    ? jdbcTemplate.queryForList(sql, courseId, "%" + keyword + "%")
                    : jdbcTemplate.queryForList(sql, "%" + keyword + "%");

            if (rows.isEmpty()) return "未找到与「" + keyword + "」相关的知识内容。";

            StringBuilder sb = new StringBuilder("找到 ").append(rows.size()).append(" 条相关知识：\n\n");
            for (int i = 0; i < rows.size(); i++) {
                sb.append("【片段").append(i + 1).append("】\n")
                  .append(rows.get(i).get("content")).append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("searchKnowledge 工具调用失败", e);
            return "知识库查询失败：" + e.getMessage();
        }
    }

    @Tool("获取课程的基本信息，包括课程名称、描述、教师等")
    public String getCourseInfo(@P("课程ID") long courseId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT course_name, description, teacher_id, status FROM course WHERE id = ? AND deleted = 0",
                    courseId);
            if (rows.isEmpty()) return "未找到课程ID=" + courseId + "的信息。";
            Map<String, Object> c = rows.get(0);
            return String.format("课程名：%s\n描述：%s\n状态：%s",
                    c.get("course_name"), c.get("description"), c.get("status"));
        } catch (Exception e) {
            log.error("getCourseInfo 工具调用失败", e);
            return "课程信息查询失败：" + e.getMessage();
        }
    }

    @Tool("获取某课程的作业列表及统计信息，包括作业数量、平均分、提交率")
    public String getAssignmentStats(@P("课程ID") long courseId) {
        try {
            List<Map<String, Object>> assignments = jdbcTemplate.queryForList(
                    "SELECT a.title, a.max_score, " +
                    "COUNT(s.id) as submit_count, AVG(s.score) as avg_score " +
                    "FROM assignment a LEFT JOIN assignment_submission s ON a.id = s.assignment_id " +
                    "WHERE a.course_id = ? AND a.deleted = 0 " +
                    "GROUP BY a.id, a.title, a.max_score LIMIT 10",
                    courseId);
            if (assignments.isEmpty()) return "该课程暂无作业。";
            StringBuilder sb = new StringBuilder("课程作业统计（共" + assignments.size() + "个）：\n\n");
            for (Map<String, Object> a : assignments) {
                sb.append("- ").append(a.get("title"))
                  .append("  满分:").append(a.get("max_score"))
                  .append("  提交数:").append(a.get("submit_count"))
                  .append("  平均分:").append(a.get("avg_score") != null
                          ? String.format("%.1f", ((Number) a.get("avg_score")).doubleValue()) : "暂无")
                  .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("getAssignmentStats 工具调用失败", e);
            return "作业统计查询失败：" + e.getMessage();
        }
    }

    @Tool("获取指定学生在某课程的学习情况，包括作业提交记录和成绩")
    public String getStudentProgress(
            @P("学生用户ID") long studentId,
            @P("课程ID，如果是全部课程可以传0") long courseId) {
        try {
            String sql = courseId > 0
                    ? "SELECT a.title, s.score, s.ai_score, s.submit_time, s.status " +
                      "FROM assignment_submission s JOIN assignment a ON s.assignment_id = a.id " +
                      "WHERE s.student_id = ? AND a.course_id = ? ORDER BY s.submit_time DESC LIMIT 10"
                    : "SELECT a.title, s.score, s.ai_score, s.submit_time, s.status " +
                      "FROM assignment_submission s JOIN assignment a ON s.assignment_id = a.id " +
                      "WHERE s.student_id = ? ORDER BY s.submit_time DESC LIMIT 10";
            List<Map<String, Object>> rows = courseId > 0
                    ? jdbcTemplate.queryForList(sql, studentId, courseId)
                    : jdbcTemplate.queryForList(sql, studentId);

            if (rows.isEmpty()) return "该学生暂无作业提交记录。";

            StringBuilder sb = new StringBuilder("学生作业记录（最近" + rows.size() + "条）：\n\n");
            for (Map<String, Object> r : rows) {
                sb.append("- 《").append(r.get("title")).append("》")
                  .append("  得分:").append(r.get("score") != null ? r.get("score") : "未批改")
                  .append("  AI评分:").append(r.get("ai_score") != null ? r.get("ai_score") : "无")
                  .append("  状态:").append(r.get("status"))
                  .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("getStudentProgress 工具调用失败", e);
            return "学生学习记录查询失败：" + e.getMessage();
        }
    }

    @Tool("获取班级整体学情统计，包括人数、平均分、完成率等")
    public String getClassOverview(@P("课程ID") long courseId) {
        try {
            List<Map<String, Object>> stats = jdbcTemplate.queryForList(
                    "SELECT COUNT(DISTINCT s.student_id) as student_count, " +
                    "COUNT(s.id) as total_submissions, " +
                    "AVG(s.score) as avg_score, " +
                    "SUM(CASE WHEN s.score >= 60 THEN 1 ELSE 0 END) as pass_count " +
                    "FROM assignment_submission s " +
                    "JOIN assignment a ON s.assignment_id = a.id " +
                    "WHERE a.course_id = ? AND s.score IS NOT NULL",
                    courseId);
            if (stats.isEmpty() || stats.get(0).get("student_count") == null) {
                return "暂无班级学情数据。";
            }
            Map<String, Object> stat = stats.get(0);
            return String.format("班级学情概览：\n学生数：%s\n总提交数：%s\n平均分：%s\n及格人数：%s",
                    stat.get("student_count"), stat.get("total_submissions"),
                    stat.get("avg_score") != null
                            ? String.format("%.1f", ((Number) stat.get("avg_score")).doubleValue()) : "暂无",
                    stat.get("pass_count"));
        } catch (Exception e) {
            log.error("getClassOverview 工具调用失败", e);
            return "班级统计查询失败：" + e.getMessage();
        }
    }
}

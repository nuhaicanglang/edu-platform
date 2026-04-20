package com.eduplatform.system.util;

/**
 * AI 批改结果 JSON 解析工具
 * <p>
 * 从 AI 返回的批改 JSON 中提取 totalScore 和 overallComment，
 * 采用轻量字符串截取方式，避免引入完整 JSON 解析库的开销。
 * </p>
 */
public final class GradingJsonParser {

    private GradingJsonParser() {}

    /**
     * 从批改结果 JSON 中提取总分
     *
     * @param json AI 返回的批改 JSON 字符串
     * @return 总分，解析失败返回 0
     */
    public static int parseScore(String json) {
        try {
            int start = json.indexOf("\"totalScore\"");
            if (start < 0) return 0;
            int colon = json.indexOf(':', start);
            int comma = json.indexOf(',', colon);
            int brace = json.indexOf('}', colon);
            int end = (comma > 0 && comma < brace) ? comma : brace;
            return Integer.parseInt(json.substring(colon + 1, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 从批改结果 JSON 中提取总评语
     *
     * @param json AI 返回的批改 JSON 字符串
     * @return 总评语，解析失败返回空字符串
     */
    public static String parseOverallComment(String json) {
        try {
            int start = json.indexOf("\"overallComment\"");
            if (start < 0) return "";
            int colon = json.indexOf(':', start);
            int q1 = json.indexOf('"', colon + 1);
            int q2 = json.indexOf('"', q1 + 1);
            return json.substring(q1 + 1, q2);
        } catch (Exception e) {
            return "";
        }
    }
}

package com.eduplatform.system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文档处理服务 — 文本提取与 AI 批改报告生成
 * <p>
 * 主要功能：
 * 1. 从 DOCX/PDF/TXT 等格式中提取纯文本（供 AI 批改使用）
 * 2. 将 AI 返回的批改 JSON 渲染为带格式的 Word 批改报告（分数、逐项批注、知识点分析、改进建议）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocxProcessingService {

    @Value("${file.upload.path:C:/edu-uploads}")
    private String uploadBasePath;

    private final ObjectMapper objectMapper;

    /**
     * 从文件路径提取纯文本（支持 docx/doc/pdf/txt/md）
     */
    public String extractText(String relativePath) throws IOException {
        Path path = Paths.get(uploadBasePath, relativePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + relativePath);
        }
        String ext = getExt(relativePath).toLowerCase();
        return switch (ext) {
            case "docx" -> extractDocx(path);
            case "pdf"  -> extractPdf(path);
            default     -> Files.readString(path, StandardCharsets.UTF_8);
        };
    }

    private String extractDocx(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             XWPFDocument doc = new XWPFDocument(in)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText().trim();
                if (!text.isEmpty()) sb.append(text).append("\n");
            }
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String text = cell.getText().trim();
                        if (!text.isEmpty()) sb.append(text).append("\t");
                    }
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }

    private String extractPdf(Path path) throws IOException {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    /**
     * 生成带批注的Word文档，将AI批改结果附加到原文档末尾
     * @param submissionRelPath 学生提交文件相对路径
     * @param gradingJson       AI批改结果JSON字符串
     * @return 新文件相对路径
     */
    public String createAnnotatedDocx(String submissionRelPath, String gradingJson) throws IOException {
        return createAnnotatedDocx(submissionRelPath, gradingJson, null);
    }

    public String createAnnotatedDocx(String submissionRelPath, String gradingJson, String originalFileName) throws IOException {
        Path sourcePath = Paths.get(uploadBasePath, submissionRelPath);

        XWPFDocument doc;
        if (submissionRelPath.toLowerCase().endsWith(".docx")) {
            try (InputStream in = Files.newInputStream(sourcePath)) {
                doc = new XWPFDocument(in);
            }
        } else {
            doc = new XWPFDocument();
            XWPFParagraph intro = doc.createParagraph();
            XWPFRun introRun = intro.createRun();
            introRun.setText("[原文件为非docx格式，仅附批改报告]");
            introRun.setColor("888888");
        }

        appendGradingReport(doc, gradingJson);

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String baseName = (originalFileName != null && !originalFileName.isBlank())
                ? "AI批改_" + originalFileName.replaceAll("\\.docx$", "").replaceAll("\\.doc$", "")
                : "graded_" + UUID.randomUUID().toString().replace("-", "");
        String newFilename = baseName + ".docx";
        Path outDir = Paths.get(uploadBasePath, "graded", dateDir);
        Files.createDirectories(outDir);
        Path outPath = outDir.resolve(newFilename);

        try (OutputStream out = Files.newOutputStream(outPath)) {
            doc.write(out);
        }
        doc.close();

        return "graded/" + dateDir + "/" + newFilename;
    }

    private void appendGradingReport(XWPFDocument doc, String gradingJson) {
        try {
            addSeparator(doc);
            addHeading(doc, "📝 AI 批改报告", "2563EB");

            JsonNode root = objectMapper.readTree(cleanJson(gradingJson));

            int totalScore = root.path("totalScore").asInt(0);
            int maxScore   = root.path("maxScore").asInt(100);
            String scoreColor = totalScore >= maxScore * 0.9 ? "16A34A"
                    : totalScore >= maxScore * 0.6 ? "D97706" : "DC2626";
            addKeyValue(doc, "总分", totalScore + " / " + maxScore + " 分", scoreColor);

            String overall = root.path("overallComment").asText("").trim();
            if (!overall.isEmpty()) {
                addSection(doc, "总体评价", overall, "1E40AF");
            }

            JsonNode annotations = root.path("annotations");
            if (annotations.isArray() && annotations.size() > 0) {
                addHeading(doc, "逐项批注", "374151");
                for (JsonNode ann : annotations) {
                    String position  = ann.path("position").asText("-");
                    String comment   = ann.path("comment").asText("");
                    String suggest   = ann.path("suggestion").asText("");
                    String errType   = ann.path("errorType").asText("");
                    int    score     = ann.path("score").asInt(-1);
                    int    maxSc     = ann.path("maxScore").asInt(-1);
                    String kp        = ann.path("knowledgePoint").asText("");

                    StringBuilder sb = new StringBuilder();
                    if (!errType.isEmpty()) sb.append("【").append(errType).append("】 ");
                    if (!comment.isEmpty())  sb.append(comment).append(" ");
                    if (!suggest.isEmpty())  sb.append("\n建议：").append(suggest);
                    if (!kp.isEmpty())       sb.append("\n知识点：").append(kp);

                    String scoreStr = (score >= 0 && maxSc > 0) ? score + "/" + maxSc : "";
                    boolean isError = errType.contains("错误") || errType.contains("Error");

                    addAnnotationEntry(doc, position, sb.toString().trim(), scoreStr, isError);
                }
            }

            JsonNode ks = root.path("knowledgeSummary");
            if (!ks.isMissingNode()) {
                StringBuilder ksSb = new StringBuilder();
                appendList(ksSb, "✅ 已掌握", ks.path("mastered"));
                appendList(ksSb, "⚠️ 需加强", ks.path("needImprovement"));
                appendList(ksSb, "❌ 未掌握", ks.path("notGrasped"));
                if (ksSb.length() > 0) addSection(doc, "知识点掌握情况", ksSb.toString(), "374151");
            }

            String plan = root.path("improvementPlan").asText("").trim();
            if (!plan.isEmpty()) addSection(doc, "个性化改进建议", plan, "065F46");

        } catch (Exception e) {
            log.warn("解析批改JSON失败，直接附加原始内容: {}", e.getMessage());
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText(gradingJson);
        }
    }

    private void addSeparator(XWPFDocument doc) {
        // Page break so AI report starts on a new page
        XWPFParagraph pb = doc.createParagraph();
        pb.setPageBreak(true);
        XWPFRun pbr = pb.createRun();
        pbr.setText("");

        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText("══════════════════════════════════════════");
        r.setBold(true);
        r.setColor("2563EB");
        r.addBreak();
    }

    private void addHeading(XWPFDocument doc, String text, String color) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(240);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(14);
        r.setColor(color);
        r.addBreak();
    }

    private void addKeyValue(XWPFDocument doc, String key, String value, String valueColor) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun kr = p.createRun();
        kr.setText(key + "：");
        kr.setBold(true);
        kr.setFontSize(12);
        XWPFRun vr = p.createRun();
        vr.setText(value);
        vr.setBold(true);
        vr.setFontSize(13);
        vr.setColor(valueColor);
    }

    private void addSection(XWPFDocument doc, String title, String content, String titleColor) {
        XWPFParagraph tp = doc.createParagraph();
        tp.setSpacingBefore(160);
        XWPFRun tr = tp.createRun();
        tr.setText(title + "：");
        tr.setBold(true);
        tr.setColor(titleColor);

        XWPFParagraph cp = doc.createParagraph();
        XWPFRun cr = cp.createRun();
        cr.setText(content);
        cr.setColor("374151");
    }

    private void addAnnotationEntry(XWPFDocument doc, String position, String comment,
                                    String score, boolean isError) {
        XWPFParagraph p = doc.createParagraph();
        p.setIndentationLeft(360);
        p.setSpacingBefore(100);

        XWPFRun posRun = p.createRun();
        posRun.setText("▸ " + position);
        posRun.setBold(true);
        posRun.setColor(isError ? "DC2626" : "16A34A");

        if (!score.isEmpty()) {
            XWPFRun scoreRun = p.createRun();
            scoreRun.setText("  [" + score + "]");
            scoreRun.setColor("6B7280");
        }

        if (!comment.isEmpty()) {
            XWPFParagraph cp = doc.createParagraph();
            cp.setIndentationLeft(720);
            XWPFRun cr = cp.createRun();
            cr.setText(comment);
            cr.setColor("374151");
        }
    }

    private void appendList(StringBuilder sb, String label, JsonNode arr) {
        if (arr.isArray() && arr.size() > 0) {
            sb.append(label).append(": ");
            for (int i = 0; i < arr.size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(arr.get(i).asText());
            }
            sb.append("\n");
        }
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        raw = raw.trim();
        int start = raw.indexOf('{');
        int end   = raw.lastIndexOf('}');
        return (start >= 0 && end > start) ? raw.substring(start, end + 1) : raw;
    }

    private String getExt(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1) : "";
    }
}

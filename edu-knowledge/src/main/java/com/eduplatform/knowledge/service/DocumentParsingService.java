package com.eduplatform.knowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文档解析服务 - 支持TXT/Markdown/PDF文本提取和分块
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParsingService {

    @Value("${file.upload.path:./uploads}")
    private String uploadBasePath;

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    /**
     * 上传并保存文件，返回存储路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dirPath = Paths.get(uploadBasePath, "knowledge", dateDir);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return "knowledge/" + dateDir + "/" + fileName;
    }

    /**
     * 从文件提取纯文本内容
     */
    public String extractText(String filePath) throws IOException {
        Path path = Paths.get(uploadBasePath, filePath);
        String ext = getExtension(filePath);

        return switch (ext.toLowerCase()) {
            case "txt", "md", "markdown", "csv" -> Files.readString(path, StandardCharsets.UTF_8);
            case "pdf" -> {
                try (PDDocument doc = Loader.loadPDF(path.toFile())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    yield stripper.getText(doc);
                }
            }
            case "docx" -> {
                try (InputStream in = Files.newInputStream(path);
                     XWPFDocument docx = new XWPFDocument(in)) {
                    StringBuilder sb = new StringBuilder();
                    for (XWPFParagraph p : docx.getParagraphs()) {
                        sb.append(p.getText()).append("\n");
                    }
                    yield sb.toString();
                }
            }
            default -> {
                log.warn("不支持的文件类型: {}, 尝试按文本读取", ext);
                yield Files.readString(path, StandardCharsets.UTF_8);
            }
        };
    }

    /**
     * 将文本内容分割为知识块
     */
    public List<String> splitToChunks(String text) {
        return splitToChunks(text, DEFAULT_CHUNK_SIZE, CHUNK_OVERLAP);
    }

    /**
     * 将文本内容分割为知识块（自定义参数）
     */
    public List<String> splitToChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 优先按段落分割
        String[] paragraphs = text.split("\n\n+");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;

            if (currentChunk.length() + trimmed.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                // 保留重叠部分
                String overlapText = currentChunk.substring(
                        Math.max(0, currentChunk.length() - overlap));
                currentChunk = new StringBuilder(overlapText);
            }
            currentChunk.append(trimmed).append("\n\n");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        // 对超长块再次分割
        List<String> result = new ArrayList<>();
        for (String chunk : chunks) {
            if (chunk.length() > chunkSize * 2) {
                result.addAll(splitLongText(chunk, chunkSize, overlap));
            } else {
                result.add(chunk);
            }
        }

        return result;
    }

    private List<String> splitLongText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
        }
        return chunks;
    }

    private String getExtension(String filename) {
        if (filename == null) return "txt";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1) : "txt";
    }
}

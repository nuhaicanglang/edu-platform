package com.eduplatform.system.controller;

import com.eduplatform.system.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件服务控制器
 * <p>
 * 提供上传文件的在线预览和下载，支持根据 URL 相对路径访问存储的文件。
 * 文件访问必须经 Gateway 完成身份认证。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/system/files")
@RequiredArgsConstructor
public class FileServeController {

    private final FileUploadService fileUploadService;

    /**
     * 根据相对路径提供文件访问
     * <p>URL 格式：/system/files/{relativePath}，加 ?download=true 触发下载</p>
     */
    @GetMapping("/**")
    public ResponseEntity<InputStreamResource> serveFile(
            @RequestParam(required = false, defaultValue = "false") boolean download,
            jakarta.servlet.http.HttpServletRequest request) throws IOException {

        String uri = request.getRequestURI();
        String rawRelative = uri.contains("/system/files/") ?
                uri.substring(uri.indexOf("/system/files/") + "/system/files/".length()) : "";
        String relativePath;
        try {
            relativePath = java.net.URLDecoder.decode(rawRelative, StandardCharsets.UTF_8);
        } catch (Exception e) {
            relativePath = rawRelative;
        }

        Path filePath = fileUploadService.getAbsolutePath(relativePath);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        String filename = filePath.getFileName().toString();
        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        InputStream in = Files.newInputStream(filePath);
        HttpHeaders headers = new HttpHeaders();
        if (download || contentType.contains("octet-stream")) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);
        } else {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(Files.size(filePath))
                .body(new InputStreamResource(in));
    }
}

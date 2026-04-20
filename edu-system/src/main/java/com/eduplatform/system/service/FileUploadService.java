package com.eduplatform.system.service;

import com.eduplatform.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件上传服务
 * <p>
 * 将上传文件存储到本地磁盘，按"分类/日期/UUID"路径组织，
 * 返回相对路径供前端通过 FileServeController 访问。
 * </p>
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload.path:C:/edu-uploads}")
    private String uploadBasePath;

    /**
     * 上传文件到本地磁盘
     *
     * @param file     上传的文件
     * @param category 分类目录（如 assignments、submissions）
     * @return 相对存储路径
     */
    public String upload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf(".")) : "";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path dirPath = Paths.get(uploadBasePath, category, dateDir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(newFilename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = category + "/" + dateDir + "/" + newFilename;
            log.info("文件上传成功: {}", filePath);
            return relativePath;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /** 兼容旧调用（无category参数） */
    public String upload(MultipartFile file) {
        return upload(file, "misc");
    }

    /** 根据相对路径获取绝对路径 */
    public Path getAbsolutePath(String relativePath) {
        return Paths.get(uploadBasePath, relativePath);
    }

    public String getUploadBasePath() {
        return uploadBasePath;
    }
}

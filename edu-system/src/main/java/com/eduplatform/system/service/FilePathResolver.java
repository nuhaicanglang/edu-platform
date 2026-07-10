package com.eduplatform.system.service;

import com.eduplatform.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将业务相对路径限制在上传目录内，阻止目录穿越和绝对路径逃逸。
 */
@Component
public class FilePathResolver {

    private final Path uploadRoot;

    public FilePathResolver(@Value("${file.upload.path:C:/edu-uploads}") String uploadBasePath) {
        if (uploadBasePath == null || uploadBasePath.isBlank()) {
            throw new IllegalArgumentException("文件上传根目录不能为空");
        }
        this.uploadRoot = Paths.get(uploadBasePath).toAbsolutePath().normalize();
    }

    public Path resolve(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new BusinessException(400, "非法文件路径");
        }
        Path candidate = uploadRoot.resolve(relativePath).toAbsolutePath().normalize();
        if (!candidate.startsWith(uploadRoot)) {
            throw new BusinessException(400, "非法文件路径");
        }
        return candidate;
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }
}

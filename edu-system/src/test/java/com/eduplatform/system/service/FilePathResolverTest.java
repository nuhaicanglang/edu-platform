package com.eduplatform.system.service;

import com.eduplatform.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilePathResolverTest {

    @TempDir
    Path uploadRoot;

    @Test
    void resolvesNormalizedPathInsideUploadRoot() {
        FilePathResolver resolver = new FilePathResolver(uploadRoot.toString());

        Path resolved = resolver.resolve("assignments/2026/report.docx");

        assertThat(resolved).isEqualTo(
                uploadRoot.resolve("assignments/2026/report.docx").toAbsolutePath().normalize());
    }

    @Test
    void rejectsTraversalOutsideUploadRoot() {
        FilePathResolver resolver = new FilePathResolver(uploadRoot.toString());

        assertThatThrownBy(() -> resolver.resolve("../secret.txt"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("非法文件路径");
    }

    @Test
    void rejectsAbsolutePathOutsideUploadRoot() {
        FilePathResolver resolver = new FilePathResolver(uploadRoot.toString());
        Path outside = uploadRoot.getParent().resolve("secret.txt").toAbsolutePath();

        assertThatThrownBy(() -> resolver.resolve(outside.toString()))
                .isInstanceOf(BusinessException.class);
    }
}

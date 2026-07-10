package com.eduplatform.knowledge.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 知识库文档实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_document")
public class KnowledgeDocument extends BaseEntity {

    /** 关联课程ID */
    private Long courseId;
    /** 文档标题 */
    private String title;
    /** 原始文件名 */
    private String fileName;
    /** 存储路径 */
    private String fileUrl;
    /** 文件类型（pdf/docx/txt/md） */
    private String fileType;
    /** 文件大小（字节） */
    private Long fileSize;
    /** 提取的纯文本内容 */
    private String content;
    /** 状态（parsing/completed/failed） */
    private String status;
    /** 上传者用户ID */
    private Long uploadUserId;
    /** 分块数量 */
    private Integer chunkCount;
    /** 向量索引状态（pending/processing/ready/failed） */
    private String indexStatus;
    /** 最近一次索引错误摘要 */
    private String indexError;
    /** 生成向量所用模型 */
    private String embeddingModel;
    /** 向量维度 */
    private Integer embeddingDimension;
    /** 最近成功完成索引的时间 */
    private LocalDateTime indexedAt;
}

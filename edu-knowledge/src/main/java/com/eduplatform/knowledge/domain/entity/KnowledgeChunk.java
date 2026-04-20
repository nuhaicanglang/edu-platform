package com.eduplatform.knowledge.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eduplatform.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识分块实体（文档拆分后的知识块）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_chunk")
public class KnowledgeChunk extends BaseEntity {

    /** 关联文档ID */
    private Long documentId;
    /** 关联课程ID */
    private Long courseId;
    /** 分块序号 */
    private Integer chunkIndex;
    /** 分块文本内容 */
    private String content;
    /** 提取的关键词 */
    private String keywords;
    /** 关联的知识点 */
    private String knowledgePoints;
}

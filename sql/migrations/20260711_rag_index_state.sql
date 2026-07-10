-- 已有数据库升级到可恢复 RAG 索引状态；仅执行一次。
ALTER TABLE knowledge_document
    ADD COLUMN index_status VARCHAR(32) NOT NULL DEFAULT 'pending'
        COMMENT '向量索引状态: pending/processing/ready/failed' AFTER chunk_count,
    ADD COLUMN index_error VARCHAR(1000) DEFAULT NULL
        COMMENT '最近一次索引错误摘要' AFTER index_status,
    ADD COLUMN embedding_model VARCHAR(128) DEFAULT NULL
        COMMENT '向量模型名称' AFTER index_error,
    ADD COLUMN embedding_dimension INT DEFAULT NULL
        COMMENT '向量维度' AFTER embedding_model,
    ADD COLUMN indexed_at DATETIME DEFAULT NULL
        COMMENT '最近成功索引时间' AFTER embedding_dimension,
    ADD INDEX idx_index_status (index_status, deleted);

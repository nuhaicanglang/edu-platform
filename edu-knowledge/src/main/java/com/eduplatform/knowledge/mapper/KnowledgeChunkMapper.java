package com.eduplatform.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.knowledge.domain.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;

/** 知识分块 Mapper */
@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {
}

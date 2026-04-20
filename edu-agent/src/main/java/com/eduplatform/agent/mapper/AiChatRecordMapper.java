package com.eduplatform.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.agent.domain.entity.AiChatRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话记录 Mapper
 */
@Mapper
public interface AiChatRecordMapper extends BaseMapper<AiChatRecord> {
}

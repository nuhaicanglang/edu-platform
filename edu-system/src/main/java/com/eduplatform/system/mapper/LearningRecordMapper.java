package com.eduplatform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.system.domain.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习记录 Mapper
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {
}

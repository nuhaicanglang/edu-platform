package com.eduplatform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.system.domain.entity.AssignmentSubmission;
import org.apache.ibatis.annotations.Mapper;

/** 作业提交 Mapper */
@Mapper
public interface AssignmentSubmissionMapper extends BaseMapper<AssignmentSubmission> {
}

package com.eduplatform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.system.domain.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;

/** 作业 Mapper（继承 MyBatis-Plus BaseMapper，提供标准 CRUD + 分页） */
@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {
}

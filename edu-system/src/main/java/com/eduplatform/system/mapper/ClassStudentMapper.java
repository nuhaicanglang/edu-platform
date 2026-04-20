package com.eduplatform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.system.domain.entity.ClassStudent;
import org.apache.ibatis.annotations.Mapper;

/** 班级-学生关联 Mapper */
@Mapper
public interface ClassStudentMapper extends BaseMapper<ClassStudent> {
}

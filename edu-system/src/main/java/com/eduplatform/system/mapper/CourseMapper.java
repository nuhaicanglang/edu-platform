package com.eduplatform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.system.domain.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/** 课程 Mapper */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}

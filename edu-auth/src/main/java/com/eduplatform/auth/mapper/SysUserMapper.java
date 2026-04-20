package com.eduplatform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.auth.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/** 系统用户 Mapper（继承 MyBatis-Plus BaseMapper，提供标准 CRUD） */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

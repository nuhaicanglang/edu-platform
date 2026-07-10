package com.eduplatform.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduplatform.auth.domain.dto.LoginDTO;
import com.eduplatform.auth.domain.dto.RegisterDTO;
import com.eduplatform.auth.domain.entity.SysUser;
import com.eduplatform.auth.domain.vo.LoginVO;
import com.eduplatform.auth.mapper.SysUserMapper;
import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证业务服务
 * <p>
 * 处理用户登录（MD5 密码校验 + JWT 签发）、注册（用户名唯一性检查）和用户信息查询。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    /**
     * 用户注册
     */
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        // 公开注册只能创建学生账号，教师和管理员必须通过受保护的管理流程创建。
        user.setRole("student");
        user.setUserCode(dto.getUserCode());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(0);
        userMapper.insert(user);
    }

    /**
     * 获取当前用户信息
     */
    public SysUser getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }
}

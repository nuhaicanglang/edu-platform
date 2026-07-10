package com.eduplatform.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduplatform.auth.domain.dto.RegisterDTO;
import com.eduplatform.auth.domain.entity.SysUser;
import com.eduplatform.auth.mapper.SysUserMapper;
import com.eduplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Test
    void publicRegistrationAlwaysCreatesStudent() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("StrongPassword123!")).thenReturn("encoded-password");
        AuthService service = new AuthService(userMapper, passwordEncoder, jwtService);

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("new-user");
        dto.setPassword("StrongPassword123!");
        dto.setRealName("测试学生");

        service.register(dto);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("student", captor.getValue().getRole());
    }
}

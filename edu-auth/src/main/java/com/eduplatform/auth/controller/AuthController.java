package com.eduplatform.auth.controller;

import com.eduplatform.auth.domain.dto.LoginDTO;
import com.eduplatform.auth.domain.dto.RegisterDTO;
import com.eduplatform.auth.domain.vo.LoginVO;
import com.eduplatform.auth.service.AuthService;
import com.eduplatform.common.core.domain.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供用户登录、注册、获取当前用户信息等接口。
 * 登录/注册接口在 Gateway 白名单中，无需携带 Token。
 * </p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 用户登录（返回 JWT Token + 用户信息） */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /** 用户注册（密码 MD5 加密存储） */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return R.ok();
    }

    /** 获取当前登录用户信息（密码字段已脱敏） */
    @GetMapping("/info")
    public R<?> getUserInfo(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(authService.getUserInfo(userId));
    }
}

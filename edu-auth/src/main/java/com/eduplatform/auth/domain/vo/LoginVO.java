package com.eduplatform.auth.domain.vo;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色 */
    private String role;

    /** 头像 */
    private String avatar;
}

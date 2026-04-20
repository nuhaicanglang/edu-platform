package com.eduplatform.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 提供 Token 的生成、解析、校验功能。Token 包含 userId、username、role 三个 Claim。
 * 有效期 24 小时，使用 HMAC-SHA 签名。
 * </p>
 * <p>
 * 密钥优先从环境变量 {@code JWT_SECRET} 读取，再从系统属性读取，最后使用默认值（仅开发环境）。
 * 生产环境请务必通过环境变量配置 JWT_SECRET，避免密钥硬编码泄露。
 * </p>
 */
public class JwtUtils {

    /** 默认密钥（仅开发环境兜底使用，生产必须通过 JWT_SECRET 环境变量覆盖） */
    private static final String DEFAULT_SECRET = "EduPlatformSecretKey2024ForJWTTokenGeneration!!";

    /** 运行时从环境变量或系统属性加载的密钥 */
    private static final String SECRET = resolveSecret();

    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24小时

    private static String resolveSecret() {
        String s = System.getenv("JWT_SECRET");
        if (s == null || s.isBlank()) s = System.getProperty("jwt.secret");
        if (s == null || s.isBlank()) s = DEFAULT_SECRET;
        return s;
    }

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     */
    public static String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 获取用户名
     */
    public static String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 获取角色
     */
    public static String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 验证Token是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

package com.eduplatform.gateway.filter;

import com.eduplatform.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 网关全局 JWT 认证过滤器
 * <p>
 * 拦截所有请求，对白名单以外的路径校验 JWT Token。
 * 校验通过后将 userId、username、role 以 X-Header 形式透传给下游微服务。
 * 白名单包括：登录、注册、文件服务、Swagger 文档等公开路径。
 * </p>
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public AuthGlobalFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /** 白名单路径 */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/captcha",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/adapter/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单放行
        for (String pattern : WHITE_LIST) {
            if (PATH_MATCHER.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 获取Token
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            return unauthorized(exchange.getResponse(), "未提供认证令牌");
        }

        // 验证Token
        try {
            Claims claims = jwtService.parseToken(token);

            // 将用户信息传递到下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        // 身份头只能来自已验证的 JWT，禁止保留客户端伪造值。
                        headers.set("X-User-Id", String.valueOf(claims.get("userId")));
                        headers.set("X-User-Name", claims.getSubject());
                        headers.set("X-User-Role", String.valueOf(claims.get("role")));
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return unauthorized(exchange.getResponse(), "认证令牌无效或已过期");
        }
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

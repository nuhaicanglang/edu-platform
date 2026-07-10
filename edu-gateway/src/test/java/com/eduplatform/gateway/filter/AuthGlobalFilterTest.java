package com.eduplatform.gateway.filter;

import com.eduplatform.security.JwtProperties;
import com.eduplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuthGlobalFilterTest {

    private static final String SECRET = "gateway-test-secret-must-be-at-least-32-bytes";

    @Test
    void replacesForgedIdentityHeadersWithVerifiedClaims() {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofHours(1)));
        String token = jwtService.generateToken(42L, "alice", "student");
        AuthGlobalFilter filter = new AuthGlobalFilter(jwtService);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/system/courses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-User-Id", "999")
                .header("X-User-Name", "attacker")
                .header("X-User-Role", "admin")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        GatewayFilterChain chain = current -> {
            forwarded.set(current);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = forwarded.get().getRequest().getHeaders();
        assertThat(headers.get("X-User-Id")).isEqualTo(List.of("42"));
        assertThat(headers.get("X-User-Name")).isEqualTo(List.of("alice"));
        assertThat(headers.get("X-User-Role")).isEqualTo(List.of("student"));
    }
}

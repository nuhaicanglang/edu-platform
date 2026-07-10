package com.eduplatform.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    @Test
    void rejectsBlankSecret() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtService(new JwtProperties("", Duration.ofHours(24))));
    }

    @Test
    void tokenCannotBeVerifiedWithAnotherSecret() {
        JwtService issuer = new JwtService(new JwtProperties("a".repeat(32), Duration.ofHours(1)));
        JwtService verifier = new JwtService(new JwtProperties("b".repeat(32), Duration.ofHours(1)));

        String token = issuer.generateToken(1L, "student1", "student");

        assertThrows(JwtException.class, () -> verifier.parseToken(token));
    }

    @Test
    void generatedTokenPreservesIdentityClaims() {
        JwtService service = new JwtService(new JwtProperties("c".repeat(32), Duration.ofHours(1)));

        String token = service.generateToken(7L, "teacher1", "teacher");

        assertEquals(7L, service.parseToken(token).get("userId", Long.class));
        assertEquals("teacher1", service.parseToken(token).getSubject());
        assertEquals("teacher", service.parseToken(token).get("role", String.class));
    }
}

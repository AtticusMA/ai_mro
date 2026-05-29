package com.mro.auth;

import com.mro.auth.config.JwtProperties;
import com.mro.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("mro-backend-jwt-secret-key-must-be-at-least-256bits");
        props.setAccessTokenExpire(7200);
        props.setRefreshTokenExpire(604800);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void generateAndParseAccessToken() {
        String token = jwtUtil.generateAccessToken(
            1L, "admin", 10L,
            List.of("admin"),
            List.of("*:*:*")
        );

        assertThat(token).isNotBlank();
        Claims claims = jwtUtil.parse(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username", String.class)).isEqualTo("admin");
        assertThat(claims.get("roles", List.class)).contains("admin");
        assertThat(claims.get("permissions", List.class)).contains("*:*:*");
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void generateAndParseRefreshToken() {
        String token = jwtUtil.generateRefreshToken(1L);

        Claims claims = jwtUtil.parse(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
    }

    @Test
    void remainingSecondsIsPositive() {
        String token = jwtUtil.generateAccessToken(1L, "admin", 1L, List.of(), List.of());
        Claims claims = jwtUtil.parse(token);
        assertThat(jwtUtil.remainingSeconds(claims)).isGreaterThan(0);
    }

    @Test
    void parseInvalidTokenThrows() {
        assertThatThrownBy(() -> jwtUtil.parse("invalid.token.here"))
            .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}

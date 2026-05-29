package com.mro.auth.util;

import com.mro.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 工具 — 生成/解析 accessToken 和 refreshToken
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final JwtProperties props;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        this.signingKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, Long deptId,
                                       List<String> roles, List<String> permissions) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            .claim("deptId", deptId)
            .claim("roles", roles)
            .claim("permissions", permissions)
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date(now))
            .expiration(new Date(now + props.getAccessTokenExpire() * 1000))
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("type", "refresh")
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date(now))
            .expiration(new Date(now + props.getRefreshTokenExpire() * 1000))
            .signWith(signingKey)
            .compact();
    }

    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /** 返回 Token 剩余有效秒数（已过期返回 0） */
    public long remainingSeconds(Claims claims) {
        long expMs = claims.getExpiration().getTime();
        long remaining = (expMs - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0);
    }

    public long getAccessTokenExpire() {
        return props.getAccessTokenExpire();
    }
}

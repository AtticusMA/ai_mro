package com.mro.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.constant.HeaderConstants;
import com.mro.common.core.response.R;
import com.mro.gateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 鉴权全局过滤器
 * 白名单路径放行；其余路径校验 Bearer Token，验证通过后注入用户上下文 Header
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final GatewayProperties gatewayProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SecretKey signingKey;

    public AuthFilter(
        GatewayProperties gatewayProperties,
        ReactiveStringRedisTemplate redisTemplate,
        ObjectMapper objectMapper,
        @Value("${mro.jwt.secret}") String jwtSecret
    ) {
        this.gatewayProperties = gatewayProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 白名单放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeUnauthorized(exchange, ErrorCode.AUTH_TOKEN_INVALID, "缺少 Authorization Header");
        }

        String token = authHeader.substring(7);

        Claims claims;
        try {
            claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException e) {
            return writeUnauthorized(exchange, ErrorCode.AUTH_TOKEN_INVALID, "Token 无效或已过期");
        }

        String jti = claims.getId();

        // 检查 Redis 黑名单
        String blacklistKey = "auth:blacklist:" + jti;
        return redisTemplate.hasKey(blacklistKey).flatMap(blacklisted -> {
            if (Boolean.TRUE.equals(blacklisted)) {
                return writeUnauthorized(exchange, ErrorCode.AUTH_TOKEN_BLACKLISTED, "Token 已失效，请重新登录");
            }

            // 注入用户上下文 Header，转发给下游
            String userId = claims.getSubject();
            String deptId = claims.get("deptId", String.class);
            if (deptId == null) {
                Object deptIdObj = claims.get("deptId");
                deptId = deptIdObj != null ? deptIdObj.toString() : "";
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles", List.class);
            @SuppressWarnings("unchecked")
            List<String> perms = (List<String>) claims.get("permissions", List.class);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HeaderConstants.USER_ID, userId)
                .header(HeaderConstants.USER_DEPT_ID, deptId != null ? deptId : "")
                .header(HeaderConstants.USER_ROLES, roles != null ? String.join(",", roles) : "")
                .header(HeaderConstants.USER_PERMISSIONS, perms != null ? String.join(",", perms) : "")
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
    }

    private boolean isWhiteListed(String path) {
        return gatewayProperties.getWhiteList().stream().anyMatch(path::equals);
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, int code, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        R<Void> body = R.fail(code, msg);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":" + code + ",\"msg\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}

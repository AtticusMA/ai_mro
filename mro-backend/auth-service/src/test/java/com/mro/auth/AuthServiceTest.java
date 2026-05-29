package com.mro.auth;

import com.mro.auth.dto.*;
import com.mro.auth.entity.SysUser;
import com.mro.auth.mapper.SysUserMapper;
import com.mro.auth.service.AuthService;
import com.mro.auth.config.JwtProperties;
import com.mro.auth.util.JwtUtil;
import com.mro.common.core.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock SysUserMapper userMapper;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;

    private AuthService authService;
    private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("mro-backend-jwt-secret-key-must-be-at-least-256bits");
        props.setAccessTokenExpire(7200);
        props.setRefreshTokenExpire(604800);
        jwtUtil = new JwtUtil(props);
        authService = new AuthService(userMapper, jwtUtil, redisTemplate);
    }

    @Test
    void loginSuccess() {
        SysUser user = mockUser();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.selectRoleKeysByUserId(1L)).thenReturn(List.of("mro_operator"));
        when(userMapper.selectPermsByUserId(1L)).thenReturn(List.of("health:list"));
        when(userMapper.updateById((SysUser) any())).thenReturn(1);

        LoginResponse resp = authService.login(new LoginRequest("testuser", "password123"));

        assertThat(resp.accessToken()).isNotBlank();
        assertThat(resp.refreshToken()).isNotBlank();
        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginWithAdminRoleReturnsSuperPermission() {
        SysUser user = mockUser();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.selectRoleKeysByUserId(1L)).thenReturn(List.of("admin"));
        when(userMapper.selectPermsByUserId(1L)).thenReturn(List.of("health:list"));
        when(userMapper.updateById((SysUser) any())).thenReturn(1);

        LoginResponse resp = authService.login(new LoginRequest("testuser", "password123"));

        // admin 角色登录后 accessToken 中 permissions 应包含 *:*:*
        io.jsonwebtoken.Claims claims = jwtUtil.parse(resp.accessToken());
        @SuppressWarnings("unchecked")
        List<String> perms = claims.get("permissions", List.class);
        assertThat(perms).contains("*:*:*");
    }

    @Test
    void loginWrongPasswordThrows() {
        SysUser user = mockUser();
        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.login(new LoginRequest("testuser", "wrongpass")))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void loginDisabledUserThrows() {
        SysUser user = mockUser();
        user.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.login(new LoginRequest("testuser", "password123")))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("禁用");
    }

    @Test
    void logoutAddsToBlacklist() {
        String accessToken = jwtUtil.generateAccessToken(1L, "admin", 1L, List.of(), List.of());
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        authService.logout("Bearer " + accessToken);

        verify(valueOps).set(startsWith("auth:blacklist:"), eq("1"), anyLong(), any());
    }

    @Test
    void changePasswordSamePasswordThrows() {
        SysUser user = mockUser();
        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() ->
            authService.changePassword(1L, new ChangePasswordRequest("password123", "password123", "password123"))
        ).isInstanceOf(BizException.class).hasMessageContaining("不能相同");
    }

    private SysUser mockUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(encoder.encode("password123"));
        user.setRealName("测试用户");
        user.setDeptId(1L);
        user.setStatus(1);
        return user;
    }
}

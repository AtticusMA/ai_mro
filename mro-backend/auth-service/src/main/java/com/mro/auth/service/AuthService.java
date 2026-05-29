package com.mro.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.auth.dto.*;
import com.mro.auth.entity.SysUser;
import com.mro.auth.mapper.SysUserMapper;
import com.mro.auth.util.JwtUtil;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.auth.response.TokenDTO;
import com.mro.common.dubbo.common.response.UserInfoDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(SysUserMapper userMapper, JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    public LoginResponse login(LoginRequest req) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, req.username())
            .eq(SysUser::getIsDeleted, 0));

        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BizException(ErrorCode.AUTH_INVALID_CREDENTIALS, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BizException(ErrorCode.AUTH_USER_DISABLED, "账号已禁用");
        }

        List<String> roles = userMapper.selectRoleKeysByUserId(user.getId());
        List<String> permissions = userMapper.selectPermsByUserId(user.getId());

        // super-admin 特判
        if (roles.contains("admin")) {
            permissions = List.of("*:*:*");
        }

        String accessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getUsername(), user.getDeptId(), roles, permissions);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 更新最后登录时间
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(update);

        return LoginResponse.of(accessToken, refreshToken, jwtUtil.getAccessTokenExpire(),
            user.getId(), user.getUsername(), user.getRealName(), user.getAvatar());
    }

    public void logout(String authHeader) {
        String token = extractToken(authHeader);
        try {
            Claims claims = jwtUtil.parse(token);
            long ttl = jwtUtil.remainingSeconds(claims);
            if (ttl > 0) {
                redisTemplate.opsForValue()
                    .set(BLACKLIST_PREFIX + claims.getId(), "1", ttl, TimeUnit.SECONDS);
            }
        } catch (JwtException ignored) {
            // 无效 token 无需加黑名单
        }
    }

    public TokenDTO refreshToken(RefreshTokenRequest req) {
        Claims claims;
        try {
            claims = jwtUtil.parse(req.refreshToken());
        } catch (JwtException e) {
            throw new BizException(ErrorCode.AUTH_REFRESH_INVALID, "refreshToken 无效或已过期");
        }

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BizException(ErrorCode.AUTH_REFRESH_INVALID, "refreshToken 类型错误");
        }

        Long userId = Long.parseLong(claims.getSubject());
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BizException(ErrorCode.AUTH_USER_DISABLED, "账号不存在或已禁用");
        }

        List<String> roles = userMapper.selectRoleKeysByUserId(userId);
        List<String> permissions = userMapper.selectPermsByUserId(userId);
        if (roles.contains("admin")) {
            permissions = List.of("*:*:*");
        }

        String newAccessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getUsername(), user.getDeptId(), roles, permissions);
        return new TokenDTO(newAccessToken, jwtUtil.getAccessTokenExpire());
    }

    public UserInfoDTO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }

        List<String> roles = userMapper.selectRoleKeysByUserId(userId);
        List<String> permissions = userMapper.selectPermsByUserId(userId);
        if (roles.contains("admin")) {
            permissions = List.of("*:*:*");
        }

        // deptName 需要调 system-service，此处先返回空（manage-web 层补充）
        return new UserInfoDTO(user.getId(), user.getUsername(), user.getRealName(),
            user.getAvatar(), user.getDeptId(), null, roles, permissions);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BizException(400, "两次输入的密码不一致");
        }

        // 查询时需要 password 字段（@TableField select=false 需用 select 方式取）
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .select(SysUser::getId, SysUser::getPassword));

        if (user == null) {
            throw new BizException(ErrorCode.SYS_USER_NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.AUTH_OLD_PWD_WRONG, "旧密码校验失败");
        }
        if (req.oldPassword().equals(req.newPassword())) {
            throw new BizException(ErrorCode.AUTH_SAME_PASSWORD, "新旧密码不能相同");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(req.newPassword()));
        userMapper.updateById(update);
    }

    public void logoutByJti(String jti, long remainingTtlSeconds) {
        if (remainingTtlSeconds > 0) {
            redisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + jti, "1", remainingTtlSeconds, TimeUnit.SECONDS);
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new BizException(ErrorCode.AUTH_TOKEN_INVALID, "缺少 Authorization Header");
    }
}

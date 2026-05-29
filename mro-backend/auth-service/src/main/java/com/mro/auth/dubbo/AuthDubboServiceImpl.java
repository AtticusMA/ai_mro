package com.mro.auth.dubbo;

import com.mro.auth.service.AuthService;
import com.mro.auth.dto.ChangePasswordRequest;
import com.mro.auth.dto.RefreshTokenRequest;
import com.mro.common.dubbo.auth.response.TokenDTO;
import com.mro.common.dubbo.common.response.UserInfoDTO;
import com.mro.common.dubbo.auth.service.AuthDubboService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class AuthDubboServiceImpl implements AuthDubboService {

    private final AuthService authService;

    public AuthDubboServiceImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        return authService.getUserInfo(userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        authService.changePassword(userId,
            new ChangePasswordRequest(oldPassword, newPassword, newPassword));
    }

    @Override
    public TokenDTO refreshToken(String refreshToken) {
        return authService.refreshToken(new RefreshTokenRequest(refreshToken));
    }

    @Override
    public void logout(String jti, long remainingTtlSeconds) {
        // Gateway 直接操作 Redis 黑名单（见 AuthFilter），此处作为备用 Dubbo 入口
        authService.logoutByJti(jti, remainingTtlSeconds);
    }
}

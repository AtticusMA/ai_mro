package com.mro.auth.controller;

import com.mro.auth.dto.*;
import com.mro.auth.service.AuthService;
import com.mro.common.core.constant.HeaderConstants;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.auth.response.TokenDTO;
import com.mro.common.dubbo.common.response.UserInfoDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口 — 路由由 Gateway 直接转发，不经过 manage-web
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return R.ok();
    }

    @PostMapping("/refresh-token")
    public R<TokenDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest req) {
        return R.ok(authService.refreshToken(req));
    }

    @GetMapping("/user-info")
    public R<UserInfoDTO> userInfo(
        @RequestHeader(HeaderConstants.USER_ID) Long userId
    ) {
        return R.ok(authService.getUserInfo(userId));
    }

    @PostMapping("/change-password")
    public R<Void> changePassword(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @Valid @RequestBody ChangePasswordRequest req
    ) {
        authService.changePassword(userId, req);
        return R.ok();
    }
}

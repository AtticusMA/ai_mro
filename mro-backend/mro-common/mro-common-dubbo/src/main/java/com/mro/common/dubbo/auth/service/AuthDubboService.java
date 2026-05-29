package com.mro.common.dubbo.auth.service;

import com.mro.common.dubbo.auth.response.TokenDTO;
import com.mro.common.dubbo.common.response.UserInfoDTO;

public interface AuthDubboService {

    UserInfoDTO getUserInfo(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    TokenDTO refreshToken(String refreshToken);

    void logout(String jti, long remainingTtlSeconds);
}

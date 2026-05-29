package com.mro.auth.dto;

/**
 * 登录响应
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    Long userId,
    String username,
    String realName,
    String avatar
) {
    public static LoginResponse of(String accessToken, String refreshToken, long expiresIn,
                                   Long userId, String username, String realName, String avatar) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresIn,
            userId, username, realName, avatar);
    }
}

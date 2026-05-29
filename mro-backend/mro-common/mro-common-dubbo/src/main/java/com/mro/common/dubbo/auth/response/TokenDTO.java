package com.mro.common.dubbo.auth.response;

import java.io.Serializable;

/**
 * Token 刷新响应 DTO
 */
public record TokenDTO(
    String accessToken,
    long expiresIn
) implements Serializable {}

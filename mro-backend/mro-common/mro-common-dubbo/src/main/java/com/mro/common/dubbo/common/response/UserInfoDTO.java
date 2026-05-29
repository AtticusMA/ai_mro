package com.mro.common.dubbo.common.response;

import java.io.Serializable;
import java.util.List;

/**
 * 用户完整信息 — auth-service Dubbo 返回值
 */
public record UserInfoDTO(
    Long userId,
    String username,
    String realName,
    String avatar,
    Long deptId,
    String deptName,
    List<String> roles,
    List<String> permissions
) implements Serializable {}

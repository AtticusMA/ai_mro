package com.mro.common.dubbo.common.request;

import java.io.Serializable;
import java.util.List;

/**
 * 用户上下文 — Gateway → manage-web → service Dubbo Attachment 透传
 */
public record UserContextDTO(
    Long userId,
    Long deptId,
    List<String> roles,
    List<String> permissions
) implements Serializable {}

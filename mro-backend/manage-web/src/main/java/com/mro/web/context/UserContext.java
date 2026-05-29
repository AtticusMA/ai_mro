package com.mro.web.context;

import com.mro.common.core.constant.HeaderConstants;
import com.mro.common.dubbo.common.request.UserContextDTO;

import java.util.Arrays;
import java.util.List;

/**
 * 从 Gateway 注入的 HTTP Header 构建用户上下文，
 * 通过 ThreadLocal 在请求生命周期内传递
 */
public final class UserContext {

    private static final ThreadLocal<UserContextDTO> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(UserContextDTO ctx) {
        HOLDER.set(ctx);
    }

    public static UserContextDTO get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        UserContextDTO ctx = HOLDER.get();
        return ctx != null ? ctx.userId() : null;
    }

    public static Long getDeptId() {
        UserContextDTO ctx = HOLDER.get();
        return ctx != null ? ctx.deptId() : null;
    }

    public static List<String> getRoles() {
        UserContextDTO ctx = HOLDER.get();
        return ctx != null && ctx.roles() != null ? ctx.roles() : List.of();
    }

    public static List<String> getPermissions() {
        UserContextDTO ctx = HOLDER.get();
        return ctx != null && ctx.permissions() != null ? ctx.permissions() : List.of();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 从 Header 字符串解析，逗号分隔转 List */
    public static List<String> parseList(String header) {
        if (header == null || header.isBlank()) return List.of();
        return Arrays.asList(header.split(","));
    }
}

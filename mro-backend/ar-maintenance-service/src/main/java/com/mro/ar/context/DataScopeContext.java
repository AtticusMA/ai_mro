package com.mro.ar.context;

import java.util.List;

/**
 * 数据权限上下文，由 DataScopeDubboFilter 在每次 RPC 调用入口填充，
 * Service 层查询时读取并注入到 QueryWrapper 过滤条件。
 *
 * deptIds = null  → 不限制（全部数据，dataScope=1）
 * deptIds = []    → 无可见部门（兜底返回空结果）
 * deptIds = [..] → IN 过滤（ar-maintenance 无 deptId 字段，暂不使用）
 * selfOnly = true → 按 inspectorId = 当前用户过滤（dataScope=4）
 */
public final class DataScopeContext {

    private static final ThreadLocal<List<Long>> DEPT_IDS  = new ThreadLocal<>();
    private static final ThreadLocal<Boolean>    SELF_ONLY = new ThreadLocal<>();
    private static final ThreadLocal<Long>       USER_ID   = new ThreadLocal<>();

    private DataScopeContext() {}

    public static void setDeptIds(List<Long> deptIds) { DEPT_IDS.set(deptIds); }
    public static void setSelfOnly(boolean selfOnly)   { SELF_ONLY.set(selfOnly); }
    public static void setUserId(Long userId)          { USER_ID.set(userId); }

    public static List<Long> getDeptIds()  { return DEPT_IDS.get(); }
    public static boolean isSelfOnly()     { return Boolean.TRUE.equals(SELF_ONLY.get()); }
    public static Long getUserId()         { return USER_ID.get(); }

    public static void clear() {
        DEPT_IDS.remove();
        SELF_ONLY.remove();
        USER_ID.remove();
    }
}

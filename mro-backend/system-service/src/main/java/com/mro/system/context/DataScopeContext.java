package com.mro.system.context;

import java.util.List;

/**
 * 数据权限上下文，由 DataScopeDubboFilter 在每次 RPC 调用入口填充，
 * Service 层查询时读取并注入到 QueryWrapper 过滤条件。
 *
 * 语义：
 *   deptIds  = null  → 不限制（全部数据，dataScope=1）
 *   deptIds  = []    → 无可见部门（通常不应出现，兜底返回空结果）
 *   deptIds  = [..] → IN 过滤
 *   selfOnly = true  → 按 createUserId = 当前用户过滤（dataScope=4）
 */
public final class DataScopeContext {

    private static final ThreadLocal<List<Long>> DEPT_IDS  = new ThreadLocal<>();
    private static final ThreadLocal<Boolean>    SELF_ONLY = new ThreadLocal<>();
    private static final ThreadLocal<Long>       USER_ID   = new ThreadLocal<>();

    private DataScopeContext() {}

    public static void setDeptIds(List<Long> deptIds) { DEPT_IDS.set(deptIds); }
    public static void setSelfOnly(boolean selfOnly)   { SELF_ONLY.set(selfOnly); }
    public static void setUserId(Long userId)          { USER_ID.set(userId); }

    /** null = 不限制；非 null = IN 过滤 */
    public static List<Long> getDeptIds()  { return DEPT_IDS.get(); }
    public static boolean isSelfOnly()     { return Boolean.TRUE.equals(SELF_ONLY.get()); }
    public static Long getUserId()         { return USER_ID.get(); }

    public static void clear() {
        DEPT_IDS.remove();
        SELF_ONLY.remove();
        USER_ID.remove();
    }
}

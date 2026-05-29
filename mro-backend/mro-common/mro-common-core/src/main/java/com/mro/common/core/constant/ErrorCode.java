package com.mro.common.core.constant;

/**
 * 错误码常量 — 按 PLAT-001 §10 分段
 */
public final class ErrorCode {

    private ErrorCode() {}

    // ── 成功 ─────────────────────────────────────────────────
    public static final int SUCCESS = 0;

    // ── 认证 + 权限 (auth-service) 4010–4039 ─────────────────
    public static final int AUTH_INVALID_CREDENTIALS = 4010;
    public static final int AUTH_TOKEN_INVALID       = 4011;
    public static final int AUTH_TOKEN_BLACKLISTED   = 4012;
    public static final int AUTH_REFRESH_INVALID     = 4013;
    public static final int AUTH_SAME_PASSWORD       = 4014;
    public static final int AUTH_OLD_PWD_WRONG       = 4015;
    public static final int AUTH_FORBIDDEN           = 4020;
    public static final int AUTH_USER_DISABLED       = 4021;
    public static final int AUTH_DEPT_DISABLED       = 4022;

    // ── system-service 4100–4199 ──────────────────────────────
    public static final int SYS_USER_NOT_FOUND       = 4100;
    public static final int SYS_USERNAME_EXISTS      = 4101;
    public static final int SYS_USER_PHONE_EXISTS    = 4102;
    public static final int SYS_USER_EMPNO_EXISTS    = 4103;
    public static final int SYS_USER_CANNOT_DEL_SELF = 4104;
    public static final int SYS_DEPT_NOT_FOUND       = 4110;
    public static final int SYS_DEPT_CODE_EXISTS     = 4111;
    public static final int SYS_DEPT_HAS_CHILDREN    = 4112;
    public static final int SYS_ROLE_NOT_FOUND       = 4120;
    public static final int SYS_ROLE_KEY_DUPLICATE   = 4121;
    public static final int SYS_ROLE_HAS_USER        = 4122;
    public static final int SYS_MENU_NOT_FOUND       = 4130;
    public static final int SYS_MENU_HAS_CHILDREN    = 4131;
    public static final int SYS_DICT_NOT_FOUND       = 4140;
    public static final int SYS_DICT_CODE_DUPLICATE  = 4141;
    public static final int SYS_DICT_TYPE_EXISTS     = 4140; // alias

    // ── 系统内部错误 5000–5899 ────────────────────────────────
    public static final int SERVER_ERROR             = 5000;
    public static final int SERVER_TIMEOUT           = 5001;
}

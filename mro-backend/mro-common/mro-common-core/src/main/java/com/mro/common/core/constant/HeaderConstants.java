package com.mro.common.core.constant;

/**
 * HTTP Header 名称常量 — 用户上下文透传链
 */
public final class HeaderConstants {

    private HeaderConstants() {}

    public static final String USER_ID          = "X-User-Id";
    public static final String USER_DEPT_ID     = "X-User-Dept-Id";
    public static final String USER_ROLES       = "X-User-Roles";
    public static final String USER_PERMISSIONS = "X-User-Permissions";

    // 国密传输加密相关 Headers
    public static final String ENCRYPTED_KEY  = "X-Encrypted-Key";   // SM2 加密后的 SM4 key (Base64)
    public static final String ENCRYPTED_IV   = "X-Encrypted-Iv";    // SM2 加密后的 SM4 IV  (Base64)
    public static final String SIGNATURE      = "X-Signature";       // HMAC-SM3 签名 (Base64)
    public static final String TIMESTAMP      = "X-Timestamp";       // Unix 秒时间戳
    public static final String REQUEST_ID     = "X-Request-Id";      // 防重放唯一请求 ID
}

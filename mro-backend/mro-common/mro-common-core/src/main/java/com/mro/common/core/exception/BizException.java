package com.mro.common.core.exception;

import com.mro.common.core.constant.ErrorCode;

/**
 * 业务异常 — 携带 errorCode，不触发 5xx 告警
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(ErrorCode.SERVER_ERROR, message);
    }

    public int getCode() {
        return code;
    }
}

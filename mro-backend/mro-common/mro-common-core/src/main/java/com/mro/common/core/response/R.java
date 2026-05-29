package com.mro.common.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 统一响应体，code=0 表示成功
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record R<T>(
    int code,
    String msg,
    T data,
    long timestamp
) implements Serializable {

    public static <T> R<T> ok() {
        return new R<>(0, "ok", null, System.currentTimeMillis());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(0, "ok", data, System.currentTimeMillis());
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(0, msg, data, System.currentTimeMillis());
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(5000, msg, null, System.currentTimeMillis());
    }

    public boolean isSuccess() {
        return code == 0;
    }
}

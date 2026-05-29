package com.mro.common.core.response;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应数据结构
 */
public record PageResult<T>(
    List<T> list,
    long total,
    int pageNum,
    int pageSize
) implements Serializable {

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}

package com.mro.common.dubbo.common.request;
import java.io.Serializable;
public record PageQueryParam(int pageNum, int pageSize) implements Serializable {
    public PageQueryParam { if (pageNum < 1) pageNum = 1; if (pageSize < 1 || pageSize > 100) pageSize = 20; }
    public static PageQueryParam of(int pageNum, int pageSize) { return new PageQueryParam(pageNum, pageSize); }
    public int offset() { return (pageNum - 1) * pageSize; }
}

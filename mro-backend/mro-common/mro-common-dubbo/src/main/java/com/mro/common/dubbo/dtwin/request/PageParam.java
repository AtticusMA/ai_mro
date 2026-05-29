package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;

public record PageParam(int pageNum, int pageSize) implements Serializable {
    public PageParam {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
    }
}

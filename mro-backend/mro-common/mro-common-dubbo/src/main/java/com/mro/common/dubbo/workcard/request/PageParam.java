package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record PageParam(
    int pageNum, int pageSize
) implements Serializable {}

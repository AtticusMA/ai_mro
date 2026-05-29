package com.mro.common.dubbo.ar.request;

import java.io.Serializable;

public record ArchiveQueryParam(
        int pageNum,
        int pageSize,
        Long taskId,
        Long sessionId
) implements Serializable {}

package com.mro.common.dubbo.training.request;

import java.io.Serializable;

public record SessionQueryParam(
    Long scenarioId, Long traineeId, String status,
    String mode, int pageNum, int pageSize
) implements Serializable {}

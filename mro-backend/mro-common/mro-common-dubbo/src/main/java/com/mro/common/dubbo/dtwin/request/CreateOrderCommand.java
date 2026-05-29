package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;

public record CreateOrderCommand(
        Long planId,
        Long workstationId,
        Long assigneeId,
        String description,
        Long operatorId
) implements Serializable {}

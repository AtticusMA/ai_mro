package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;

public record MaintenanceOrderDTO(
        Long id,
        Long planId,
        Long workstationId,
        String workstationName,
        Long assigneeId,
        String assigneeName,
        String description,
        int progress,
        String status
) implements Serializable {}

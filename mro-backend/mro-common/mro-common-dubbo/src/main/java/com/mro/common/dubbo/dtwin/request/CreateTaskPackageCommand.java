package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record CreateTaskPackageCommand(
        String title,
        Long hangarId,
        Long workstationId,
        String aircraftType,
        String registration,
        LocalDate planStart,
        LocalDate planEnd,
        String priority,
        List<Long> orderIds,
        Long createdBy
) implements Serializable {}

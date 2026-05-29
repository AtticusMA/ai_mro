package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskPackageDTO(
        Long id,
        String packageNo,
        String title,
        Long hangarId,
        Long workstationId,
        String aircraftType,
        String registration,
        LocalDate planStart,
        LocalDate planEnd,
        String status,
        String priority,
        LocalDateTime createTime
) implements Serializable {}

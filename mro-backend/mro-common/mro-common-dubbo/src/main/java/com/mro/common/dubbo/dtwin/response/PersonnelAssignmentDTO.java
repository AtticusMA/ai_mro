package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;
import java.time.LocalDate;

public record PersonnelAssignmentDTO(
        Long id,
        Long packageId,
        Long userId,
        String role,
        LocalDate workDate,
        String shift
) implements Serializable {}

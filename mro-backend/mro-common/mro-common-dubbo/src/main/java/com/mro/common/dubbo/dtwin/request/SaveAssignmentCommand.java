package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;
import java.time.LocalDate;

public record SaveAssignmentCommand(
        Long packageId,
        Long userId,
        String role,
        LocalDate workDate,
        String shift
) implements Serializable {}

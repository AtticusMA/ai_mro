package com.mro.common.dubbo.health.request;

import java.io.Serializable;
import java.time.LocalDate;

public record HealthStatQueryParam(
        String aircraftType,
        String component,
        LocalDate startDate,
        LocalDate endDate
) implements Serializable {}

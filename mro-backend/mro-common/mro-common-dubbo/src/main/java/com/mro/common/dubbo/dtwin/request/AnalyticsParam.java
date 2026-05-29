package com.mro.common.dubbo.dtwin.request;

import java.io.Serializable;
import java.time.LocalDate;

public record AnalyticsParam(
        Long hangarId,
        LocalDate startDate,
        LocalDate endDate
) implements Serializable {}

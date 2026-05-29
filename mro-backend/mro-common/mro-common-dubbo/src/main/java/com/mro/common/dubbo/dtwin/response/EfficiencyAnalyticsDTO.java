package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;

public record EfficiencyAnalyticsDTO(
        double avgCompletionDays,
        double completionRate,
        long completedOrders,
        long totalOrders
) implements Serializable {}

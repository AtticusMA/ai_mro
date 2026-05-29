package com.mro.common.dubbo.health.response;

import java.io.Serializable;
import java.util.Map;

public record HealthStatisticsDTO(
        long totalFaults,
        long openFaults,
        long resolvedFaults,
        long totalAlerts,
        long unacknowledgedAlerts,
        Map<String, Long> faultsByAircraftType,
        Map<String, Long> faultsByComponent,
        Map<String, Long> faultsBySeverity
) implements Serializable {}

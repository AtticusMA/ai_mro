package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OverviewReportDTO(
    int totalTrainees, int totalSessions, BigDecimal totalHours,
    List<Map<String, Object>> scenarioStats,
    List<Map<String, Object>> skillDistribution
) implements Serializable {}

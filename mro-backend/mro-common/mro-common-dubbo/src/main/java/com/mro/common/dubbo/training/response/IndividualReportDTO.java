package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record IndividualReportDTO(
    Long traineeId, String traineeName, LocalDate start, LocalDate end,
    int totalSessions, BigDecimal totalHours,
    List<Map<String, Object>> skillTrend, List<String> weakPoints,
    String recommendations
) implements Serializable {}

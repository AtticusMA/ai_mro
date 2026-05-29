package com.mro.common.dubbo.tshoot.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record FaultStatisticsDTO(
        int totalFaults,
        List<Map<String, Object>> byComponent,
        List<Map<String, Object>> byMonth,
        List<Map<String, Object>> topReplacedParts
) implements Serializable {}

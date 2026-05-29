package com.mro.common.dubbo.tshoot.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TshootReportDTO(
        Long id,
        String solution,
        List<Map<String, String>> references,
        String flowchartUrl,
        BigDecimal confidence,
        Instant generatedAt
) implements Serializable {}

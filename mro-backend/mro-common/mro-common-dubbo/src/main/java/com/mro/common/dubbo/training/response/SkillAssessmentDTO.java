package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record SkillAssessmentDTO(
    Long id, Long sessionId, String metricName, BigDecimal score,
    Map<String, Object> detail, Instant assessedAt
) implements Serializable {}

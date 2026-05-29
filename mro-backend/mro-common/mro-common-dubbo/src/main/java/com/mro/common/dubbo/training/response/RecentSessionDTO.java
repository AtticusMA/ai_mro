package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record RecentSessionDTO(
    Long sessionId, String scenarioName,
    BigDecimal score, Instant completedAt
) implements Serializable {}

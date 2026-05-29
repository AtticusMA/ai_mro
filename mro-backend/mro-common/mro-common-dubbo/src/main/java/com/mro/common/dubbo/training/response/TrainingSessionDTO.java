package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.time.Instant;

public record TrainingSessionDTO(
    Long id, Long scenarioId, String scenarioName,
    Long traineeId, String traineeName,
    String mode, Instant startedAt, Instant endedAt, String status
) implements Serializable {}

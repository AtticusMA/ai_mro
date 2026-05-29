package com.mro.common.dubbo.training.request;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record CreateTrainingSessionCommand(
    Long scenarioId, List<Long> traineeIds, String mode,
    Instant scheduledAt, Long assignedBy
) implements Serializable {}

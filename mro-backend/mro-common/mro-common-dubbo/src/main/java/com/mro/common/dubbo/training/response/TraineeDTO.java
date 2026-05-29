package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record TraineeDTO(
    Long id, Long userId, String userName, String skillLevel,
    BigDecimal totalTrainingHours
) implements Serializable {}

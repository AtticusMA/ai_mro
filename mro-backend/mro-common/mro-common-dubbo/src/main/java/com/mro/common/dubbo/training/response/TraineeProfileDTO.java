package com.mro.common.dubbo.training.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record TraineeProfileDTO(
    Long id, Long userId, String userName, String skillLevel,
    BigDecimal totalTrainingHours, LocalDate lastAssessmentDate,
    Map<String, Double> skillRadar, List<RecentSessionDTO> recentSessions
) implements Serializable {}

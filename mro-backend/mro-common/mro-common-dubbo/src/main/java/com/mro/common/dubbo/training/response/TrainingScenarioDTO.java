package com.mro.common.dubbo.training.response;

import java.io.Serializable;

public record TrainingScenarioDTO(
    Long id, String name, String category, String difficulty,
    String modelUrl, int durationMinutes, String status
) implements Serializable {}

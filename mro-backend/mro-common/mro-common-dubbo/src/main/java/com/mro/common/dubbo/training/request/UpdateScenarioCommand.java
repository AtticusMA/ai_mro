package com.mro.common.dubbo.training.request;

import java.io.Serializable;

public record UpdateScenarioCommand(
    Long id, String name, String category, String difficulty,
    String modelUrl, int durationMinutes, Long updatedBy
) implements Serializable {}

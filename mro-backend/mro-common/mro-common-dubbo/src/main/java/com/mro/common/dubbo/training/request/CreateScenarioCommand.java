package com.mro.common.dubbo.training.request;

import java.io.Serializable;

public record CreateScenarioCommand(
    String name, String category, String difficulty,
    String modelUrl, int durationMinutes, Long createdBy
) implements Serializable {}

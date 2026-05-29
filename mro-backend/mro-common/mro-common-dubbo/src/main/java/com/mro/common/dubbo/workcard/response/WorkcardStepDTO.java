package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WorkcardStepDTO(
    Long id, int stepNo, String description,
    List<Map<String, String>> requiredTools,
    List<Map<String, String>> requiredMaterials,
    String manualRef, String status,
    String completedByName, Instant completedAt
) implements Serializable {}

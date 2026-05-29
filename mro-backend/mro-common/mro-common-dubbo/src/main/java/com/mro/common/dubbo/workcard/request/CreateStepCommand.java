package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record CreateStepCommand(
    int stepNo, String description,
    List<Map<String, String>> requiredTools,
    List<Map<String, String>> requiredMaterials,
    String manualRef
) implements Serializable {}

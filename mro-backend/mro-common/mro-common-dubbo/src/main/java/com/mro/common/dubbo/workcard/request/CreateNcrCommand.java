package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record CreateNcrCommand(
    Long workcardId,
    Long qualitySignId,
    String title,
    String description,
    String severity,
    Long assignedTo
) implements Serializable {}

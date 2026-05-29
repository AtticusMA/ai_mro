package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record NcrDTO(
    Long id,
    Long workcardId,
    Long qualitySignId,
    String ncrNo,
    String title,
    String description,
    String severity,
    String status,
    Long assignedTo,
    String assignedToName,
    LocalDateTime closedAt,
    Long createdBy,
    LocalDateTime createTime
) implements Serializable {}

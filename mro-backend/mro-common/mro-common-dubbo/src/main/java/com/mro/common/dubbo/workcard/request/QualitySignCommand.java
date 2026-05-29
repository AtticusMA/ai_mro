package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;
import java.time.LocalDateTime;

public record QualitySignCommand(
    Long workcardId,
    Long stepId,
    String result,
    String comment,
    LocalDateTime signTime,
    String signatureHash
) implements Serializable {}

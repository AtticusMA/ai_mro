package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record QualitySignRecordDTO(
    Long id,
    Long workcardId,
    Long stepId,
    Long signerId,
    String signerName,
    String result,
    String comment,
    LocalDateTime signTime,
    String signatureHash
) implements Serializable {}

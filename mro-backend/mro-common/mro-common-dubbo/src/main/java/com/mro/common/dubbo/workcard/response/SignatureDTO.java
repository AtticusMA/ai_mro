package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.Instant;

public record SignatureDTO(
    Long id, Long workcardId, Long stepId, Long signerId,
    String signerName, String signatureType,
    String blockchainHash, Instant signedAt
) implements Serializable {}

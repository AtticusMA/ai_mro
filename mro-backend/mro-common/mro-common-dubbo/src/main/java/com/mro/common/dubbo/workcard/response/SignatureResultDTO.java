package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.Instant;

public record SignatureResultDTO(
    Long signatureId, String blockchainHash, Instant signedAt
) implements Serializable {}

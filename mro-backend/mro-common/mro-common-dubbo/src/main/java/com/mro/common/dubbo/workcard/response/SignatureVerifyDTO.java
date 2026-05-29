package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.time.Instant;

public record SignatureVerifyDTO(
    Long signatureId, String signerName, String signatureType,
    String blockchainHash, Instant onChainAt, boolean tampered
) implements Serializable {}

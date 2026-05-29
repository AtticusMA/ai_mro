package com.mro.common.dubbo.workcard.response;

import java.io.Serializable;
import java.util.List;

public record BlockchainVerifyDTO(
    Long workcardId, boolean verified, List<SignatureVerifyDTO> signatures
) implements Serializable {}

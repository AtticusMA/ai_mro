package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record SignWorkcardCommand(
    Long workcardId, Long stepId, String signatureType,
    String digitalSignature, Long signerId
) implements Serializable {}

package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record CloseNcrCommand(
    Long ncrId,
    String closeSignature
) implements Serializable {}

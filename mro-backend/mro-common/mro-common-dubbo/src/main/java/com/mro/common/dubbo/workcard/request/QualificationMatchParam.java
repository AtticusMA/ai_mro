package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record QualificationMatchParam(
    Long workcardId, String aircraftType, String cardType,
    int pageNum, int pageSize
) implements Serializable {}

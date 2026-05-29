package com.mro.common.dubbo.workcard.request;

import java.io.Serializable;

public record WorkcardQueryParam(
    String status, String cardType, String aircraftId,
    String priority, int pageNum, int pageSize
) implements Serializable {}

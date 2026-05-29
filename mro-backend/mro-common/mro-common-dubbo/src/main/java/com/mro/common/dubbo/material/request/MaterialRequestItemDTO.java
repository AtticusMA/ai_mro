package com.mro.common.dubbo.material.request;
import java.io.Serializable;
import java.math.BigDecimal;
public record MaterialRequestItemDTO(String partNo, String partName, int qty, String unit,
    BigDecimal estimatedCost) implements Serializable {}

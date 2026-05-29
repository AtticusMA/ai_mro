package com.mro.common.dubbo.material.response;
import java.io.Serializable;
import java.time.LocalDate;
import java.math.BigDecimal;
public record MaterialItemDTO(Long id, String partNo, String name, String category, int stockQty, int minStock, String location, LocalDate expiryDate, BigDecimal unitPrice, boolean belowMinStock) implements Serializable {}
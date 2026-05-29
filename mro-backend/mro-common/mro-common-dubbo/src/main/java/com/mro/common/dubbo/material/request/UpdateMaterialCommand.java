package com.mro.common.dubbo.material.request;
import java.io.Serializable;
import java.time.LocalDate;
import java.math.BigDecimal;
public record UpdateMaterialCommand(Long id, String partNo, String name, String category, int stockQty, int minStock, String location, LocalDate expiryDate, BigDecimal unitPrice, Long updatedBy) implements Serializable {}
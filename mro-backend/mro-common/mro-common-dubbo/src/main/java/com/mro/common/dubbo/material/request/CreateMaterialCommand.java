package com.mro.common.dubbo.material.request;
import java.io.Serializable;
import java.time.LocalDate;
import java.math.BigDecimal;
public record CreateMaterialCommand(String partNo, String name, String category, int stockQty, int minStock, String location, LocalDate expiryDate, BigDecimal unitPrice, Long createdBy) implements Serializable {}
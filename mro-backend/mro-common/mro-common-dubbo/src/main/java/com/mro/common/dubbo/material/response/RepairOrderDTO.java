package com.mro.common.dubbo.material.response;
import java.io.Serializable;
import java.time.Instant;
public record RepairOrderDTO(Long id, Long materialId, String materialName, String partNo, int quantity, String faultDescription, Long vendorId, String status, Instant createdAt) implements Serializable {}
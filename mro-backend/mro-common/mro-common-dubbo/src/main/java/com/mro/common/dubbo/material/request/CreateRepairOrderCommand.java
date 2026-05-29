package com.mro.common.dubbo.material.request;
import java.io.Serializable;
public record CreateRepairOrderCommand(Long materialId, int quantity, String faultDescription, Long vendorId, Long createdBy) implements Serializable {}
package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.time.LocalDate;
public record ToolDTO(Long id, String name, String toolCode, String rfidTag, String category, Long cabinetId, String cabinetName, int slotNo, String status, LocalDate calibrationDue, int useCount) implements Serializable {}
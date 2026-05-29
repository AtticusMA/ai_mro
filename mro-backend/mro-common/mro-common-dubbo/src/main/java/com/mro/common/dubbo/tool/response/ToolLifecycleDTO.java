package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.time.LocalDate;
public record ToolLifecycleDTO(Long toolId, String toolName, String toolCode, int useCount, LocalDate calibrationDue, String calibrationStatus, String repairHistory) implements Serializable {}
package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.time.Instant;
public record ToolAlertDTO(Long id, Long toolId, String toolName, String alertType, Long borrowerId, String borrowerName, Instant borrowTime, Instant expectedReturn, double overdueHours) implements Serializable {}
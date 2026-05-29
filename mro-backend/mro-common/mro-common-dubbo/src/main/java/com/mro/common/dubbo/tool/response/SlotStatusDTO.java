package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
public record SlotStatusDTO(int slotNo, Long toolId, String toolName, String toolCode, String rfidTag, String status) implements Serializable {}
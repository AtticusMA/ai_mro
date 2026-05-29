package com.mro.common.dubbo.tool.response;
import java.io.Serializable;
import java.math.BigDecimal;
public record ToolCabinetDTO(Long id, String name, String location, int slotCount, int availableSlots, BigDecimal temperature, BigDecimal humidity, String onlineStatus) implements Serializable {}
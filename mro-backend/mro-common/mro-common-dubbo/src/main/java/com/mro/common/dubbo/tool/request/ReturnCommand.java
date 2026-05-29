package com.mro.common.dubbo.tool.request;
import java.io.Serializable;
import java.util.List;
public record ReturnCommand(Long userId, Long cabinetId, List<String> rfidScanResult) implements Serializable {}
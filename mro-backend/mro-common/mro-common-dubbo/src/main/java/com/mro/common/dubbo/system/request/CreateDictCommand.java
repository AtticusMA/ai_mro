package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record CreateDictCommand(String dictGroup, String dictCode, String dictLabel, Integer status, String remark) implements Serializable {}

package com.mro.common.dubbo.system.request;
import java.io.Serializable;
public record UpdateDictCommand(Long id, String dictLabel, Integer status, String remark) implements Serializable {}

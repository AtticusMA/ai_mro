package com.mro.common.dubbo.system.response;
import java.io.Serializable;
public record DictItemDTO(String dictCode, String dictLabel) implements Serializable {}

package com.mro.common.dubbo.system.response;
import java.io.Serializable;
public record DictDTO(Long id, String dictGroup, String dictCode, String dictLabel, Integer status, String remark, String createTime) implements Serializable {}

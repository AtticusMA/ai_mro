package com.mro.common.dubbo.material.response;
import java.io.Serializable;
public record MaterialAlertDTO(Long id, String partNo, String name, int stockQty, int minStock, String location) implements Serializable {}
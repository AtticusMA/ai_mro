package com.mro.common.dubbo.material.request;
import java.io.Serializable;
public record MaterialQueryParam(int pageNum, int pageSize, String category, Boolean lowStock) implements Serializable {}
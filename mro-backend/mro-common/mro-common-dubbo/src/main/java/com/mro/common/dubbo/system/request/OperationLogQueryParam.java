package com.mro.common.dubbo.system.request;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OperationLogQueryParam implements Serializable {

    private int pageNum = 1;
    private int pageSize = 20;
    private String operatorName;
    private String requestUri;
    private Long deptId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

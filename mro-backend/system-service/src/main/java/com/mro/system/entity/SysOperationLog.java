package com.mro.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestId;
    private Long operatorId;
    private String operatorName;
    private Long deptId;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private String requestBody;
    private Integer responseStatus;
    private Long costMs;
    private String clientIp;
    private String userAgent;
    private LocalDateTime requestTime;
}

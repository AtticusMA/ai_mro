package com.mro.aircraft.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_alert")
public class HealthAlert {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String aircraftId;
    /** red / orange / yellow */
    private String alertLevel;
    private String message;
    private LocalDateTime predictedFaultTime;
    private Boolean acknowledged;
    private Long acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt;
}

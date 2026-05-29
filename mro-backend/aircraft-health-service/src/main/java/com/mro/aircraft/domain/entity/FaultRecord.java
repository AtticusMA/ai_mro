package com.mro.aircraft.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fault_record")
public class FaultRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String aircraftId;
    private String faultCode;
    /** critical / major / minor */
    private String severity;
    private String component;
    private LocalDateTime detectedAt;
    /** open / confirmed / resolved */
    private String status;
    private String rawData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

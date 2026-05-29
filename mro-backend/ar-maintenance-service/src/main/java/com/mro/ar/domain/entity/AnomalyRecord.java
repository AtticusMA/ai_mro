package com.mro.ar.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("anomaly_record")
public class AnomalyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String anomalyType;

    private BigDecimal confidence;

    private String snapshotUrl;

    private LocalDateTime detectedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

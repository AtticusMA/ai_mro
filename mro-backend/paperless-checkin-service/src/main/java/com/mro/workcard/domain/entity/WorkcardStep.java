package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@TableName("workcard_step")
public class WorkcardStep {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workcardId;

    private Integer stepNo;

    private String description;

    private String requiredTools;

    private String requiredMaterials;

    private String manualRef;

    private String status;

    private Long completedBy;

    private Instant completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

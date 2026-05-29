package com.mro.training.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@TableName("training_session")
public class TrainingSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenarioId;

    private Long traineeId;

    private String mode;

    private Instant startedAt;

    private Instant endedAt;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Long assignedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

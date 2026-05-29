package com.mro.training.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("training_scenario")
public class TrainingScenario {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private String difficulty;

    private String modelUrl;

    private Integer durationMinutes;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

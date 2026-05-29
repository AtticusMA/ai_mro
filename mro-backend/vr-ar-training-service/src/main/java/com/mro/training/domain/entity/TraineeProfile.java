package com.mro.training.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("trainee_profile")
public class TraineeProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String skillLevel;

    private BigDecimal totalTrainingHours;

    private LocalDate lastAssessmentDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

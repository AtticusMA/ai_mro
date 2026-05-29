package com.mro.training.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@TableName("skill_assessment")
public class SkillAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private String metricName;

    private BigDecimal score;

    private String detail;

    private Instant assessedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

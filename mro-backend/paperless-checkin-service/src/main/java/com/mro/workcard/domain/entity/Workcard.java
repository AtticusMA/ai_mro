package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@TableName("workcard")
public class Workcard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String cardNo;

    private String title;

    private String cardType;

    private String aircraftId;

    private String priority;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    private Long approvedBy;

    private Instant dueDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

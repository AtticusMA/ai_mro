package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ncr")
public class Ncr {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workcardId;

    private Long qualitySignId;

    private String ncrNo;

    private String title;

    private String description;

    private String severity;

    private String status; // "open", "in_progress", "closed"

    private Long assignedTo;

    private String closeSignature;

    private LocalDateTime closedAt;

    private Long createdBy;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

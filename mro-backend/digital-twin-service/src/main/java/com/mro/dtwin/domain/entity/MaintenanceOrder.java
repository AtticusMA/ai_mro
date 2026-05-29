package com.mro.dtwin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("maintenance_order")
public class MaintenanceOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long workstationId;

    private Long assigneeId;

    private String description;

    private Integer progress;

    private String status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

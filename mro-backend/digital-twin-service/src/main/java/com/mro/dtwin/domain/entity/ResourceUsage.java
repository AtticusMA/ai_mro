package com.mro.dtwin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_usage")
public class ResourceUsage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workstationId;

    private Long orderId;

    private String resourceType;

    private Long resourceId;

    private LocalDateTime allocatedAt;

    private LocalDateTime releasedAt;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

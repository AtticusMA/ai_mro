package com.mro.tool.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tool_cabinet")
public class ToolCabinet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String location;

    private Integer slotCount;

    private BigDecimal temperature;

    private BigDecimal humidity;

    private String onlineStatus;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

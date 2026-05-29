package com.mro.dtwin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workstation")
public class Workstation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hangarId;

    private String name;

    private BigDecimal positionX;

    private BigDecimal positionY;

    private BigDecimal positionZ;

    private String status;

    private String currentAircraftId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

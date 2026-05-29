package com.mro.tool.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("material_item")
public class MaterialItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partNo;

    private String name;

    private String category;

    private Integer stockQty;

    private Integer minStock;

    private String location;

    private LocalDate expiryDate;

    private BigDecimal unitPrice;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.mro.dtwin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_package_order")
public class TaskPackageOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long packageId;

    private Long orderId;

    private Integer seqNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

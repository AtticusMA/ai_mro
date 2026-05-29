package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workcard_checkin")
public class WorkcardCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workcardId;

    private Long userId;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String location;

    private String deviceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

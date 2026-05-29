package com.mro.dtwin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_package")
public class TaskPackage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String packageNo;

    private String title;

    private Long hangarId;

    private Long workstationId;

    private String aircraftType;

    private String registration;

    private LocalDate planStart;

    private LocalDate planEnd;

    private String status;

    private String priority;

    private Long createdBy;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

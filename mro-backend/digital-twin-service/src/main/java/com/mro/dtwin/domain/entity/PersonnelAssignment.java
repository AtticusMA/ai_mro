package com.mro.dtwin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("personnel_assignment")
public class PersonnelAssignment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long packageId;

    private Long userId;

    private String role;

    private LocalDate workDate;

    private String shift;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

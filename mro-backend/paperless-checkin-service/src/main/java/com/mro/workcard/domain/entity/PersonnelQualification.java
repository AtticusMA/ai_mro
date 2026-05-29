package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("personnel_qualification")
public class PersonnelQualification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String qualificationType;

    private String aircraftType;

    private String level;

    private LocalDate validFrom;

    private LocalDate validTo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

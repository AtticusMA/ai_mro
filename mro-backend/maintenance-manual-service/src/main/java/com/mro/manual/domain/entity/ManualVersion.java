package com.mro.manual.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("manual_version")
public class ManualVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private String versionNo;

    private String changeSummary;

    private LocalDate effectiveDate;

    private Long revisedBy;

    private String revisedByName;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

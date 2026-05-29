package com.mro.manual.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("manual_document")
public class ManualDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String manualNo;

    private String aircraftType;

    private String format;

    private String fileUrl;

    private String parsedStatus;

    private Boolean published;

    private Long uploaderId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quality_sign_record")
public class QualitySignRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workcardId;

    private Long stepId;

    private Long signerId;

    private String result; // "pass" or "fail"

    private String comment;

    private LocalDateTime signTime;

    private String signatureHash;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

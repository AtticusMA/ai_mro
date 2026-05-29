package com.mro.workcard.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@TableName("workcard_signature")
public class WorkcardSignature {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workcardId;

    private Long stepId;

    private Long signerId;

    private String signatureType;

    private String digitalSignature;

    private String blockchainHash;

    private Instant signedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

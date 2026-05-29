package com.mro.manual.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("translation_task")
public class TranslationTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private String sourceLang;

    private String targetLang;

    private String status;

    private BigDecimal accuracyScore;

    private String resultUrl;

    private Long operatorId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.mro.ar.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ar_session")
public class ArSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long callerId;

    private Long taskId;

    private Long expertId;

    /** waiting / active / ended */
    private String status;

    private String signalingToken;

    private String recordingUrl;

    private Integer durationSeconds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

package com.mro.ar.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_archive")
public class VideoArchive {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long sessionId;

    private String fileUrl;

    private Integer durationSeconds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

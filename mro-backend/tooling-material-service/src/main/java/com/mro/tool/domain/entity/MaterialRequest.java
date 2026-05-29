package com.mro.tool.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("material_request")
public class MaterialRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestNo;
    private Long workcardId;
    private Long requesterId;
    private Long deptId;
    private String urgency;
    private String status;
    private String rejectReason;
    private String itemsJson;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private Long receivedBy;
    private LocalDateTime receivedAt;
    private Long createdBy;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

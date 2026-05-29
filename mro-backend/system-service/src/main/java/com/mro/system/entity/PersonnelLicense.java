package com.mro.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("personnel_license")
public class PersonnelLicense {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String licenseNo;
    private String licenseType;
    private String aircraftType;
    private String category;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;
    private String fileUrl;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

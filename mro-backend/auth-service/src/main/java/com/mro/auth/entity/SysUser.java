package com.mro.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField(select = false)
    private String password;

    private String realName;
    private String employeeNo;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
    private String address;
    private Long deptId;

    /** 状态：1正常 0禁用 */
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private Long createUserId;
    private Long createDeptId;
    private LocalDateTime createTime;
    private Long updateUserId;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
}

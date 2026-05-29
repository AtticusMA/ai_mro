package com.mro.system.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO) private Long id;
    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private Integer dataScope;
    private Integer status;
    @TableLogic private Integer isDeleted;
    private Long createUserId;
    private Long createDeptId;
    private LocalDateTime createTime;
    private Long updateUserId;
    private LocalDateTime updateTime;
}

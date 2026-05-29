package com.mro.system.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("sys_dept")
public class SysDept {
    @TableId(type = IdType.AUTO) private Long id;
    private String deptName;
    private String deptCode;
    private Long parentId;
    private String ancestors;
    private Integer orderNum;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
    @TableLogic private Integer isDeleted;
    private Long createUserId;
    private Long createDeptId;
    private LocalDateTime createTime;
    private Long updateUserId;
    private LocalDateTime updateTime;
}

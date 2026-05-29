package com.mro.system.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO) private Long id;
    private String dictGroup;
    private String dictCode;
    private String dictLabel;
    private Integer status;
    private String remark;
    private Long createUserId;
    private LocalDateTime createTime;
    private Long updateUserId;
    private LocalDateTime updateTime;
}

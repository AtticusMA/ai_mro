package com.mro.aircraft.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "alert_rule", autoResultMap = true)
public class AlertRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String aircraftType;
    private String metricName;
    /** lt / gt / lte / gte / eq */
    private String operator;
    private Double threshold;
    /** red / orange / yellow */
    private String alertLevel;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> notifyUserIds;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

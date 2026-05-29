package com.mro.aircraft.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "prediction_report", autoResultMap = true)
public class PredictionReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String aircraftId;
    private String modelVersion;
    private LocalDateTime predictedAt;
    /** JSON: {faultProbability, predictedFaultTime, faultLocation, confidence} */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> result;
    private LocalDateTime createdAt;
}

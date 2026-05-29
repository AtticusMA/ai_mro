-- mro_aircraft database schema
-- Refs: MRO-001

CREATE DATABASE IF NOT EXISTS mro_aircraft DEFAULT CHARACTER SET utf8mb4;
USE mro_aircraft;

CREATE TABLE IF NOT EXISTS fault_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id VARCHAR(32)  NOT NULL COMMENT '飞机注册号',
    fault_code  VARCHAR(64)  NOT NULL COMMENT '故障代码',
    severity    VARCHAR(16)  NOT NULL COMMENT 'critical/major/minor',
    component   VARCHAR(128) NOT NULL COMMENT '故障部件',
    detected_at DATETIME     NOT NULL COMMENT '检测时间',
    status      VARCHAR(16)  NOT NULL DEFAULT 'open' COMMENT 'open/confirmed/resolved',
    raw_data    TEXT                  COMMENT '原始数据',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_aircraft_id (aircraft_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_detected_at (detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障记录';

CREATE TABLE IF NOT EXISTS health_alert (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id          VARCHAR(32)  NOT NULL COMMENT '飞机注册号',
    alert_level          VARCHAR(16)  NOT NULL COMMENT 'red/orange/yellow',
    message              TEXT         NOT NULL COMMENT '预警内容',
    predicted_fault_time DATETIME              COMMENT '预测故障时间',
    acknowledged         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已确认',
    acknowledged_by      BIGINT                COMMENT '确认人用户ID',
    acknowledged_at      DATETIME              COMMENT '确认时间',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_aircraft_id (aircraft_id),
    INDEX idx_alert_level (alert_level),
    INDEX idx_acknowledged (acknowledged),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康预警';

CREATE TABLE IF NOT EXISTS prediction_report (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id   VARCHAR(32)  NOT NULL COMMENT '飞机注册号',
    model_version VARCHAR(32)  NOT NULL COMMENT '预测模型版本',
    predicted_at  DATETIME     NOT NULL COMMENT '预测生成时间',
    result        JSON         NOT NULL COMMENT '预测结果',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_aircraft_id (aircraft_id),
    INDEX idx_predicted_at (predicted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='趋势预测报告';

CREATE TABLE IF NOT EXISTS alert_rule (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name        VARCHAR(128) NOT NULL COMMENT '规则名称',
    aircraft_type    VARCHAR(64)           COMMENT '适用机型，NULL表示全机型',
    metric_name      VARCHAR(128) NOT NULL COMMENT '监控指标名',
    operator         VARCHAR(8)   NOT NULL COMMENT 'lt/lte/gt/gte/eq',
    threshold        DOUBLE       NOT NULL COMMENT '阈值',
    alert_level      VARCHAR(16)  NOT NULL COMMENT 'red/orange/yellow',
    notify_user_ids  JSON                  COMMENT '通知用户ID列表',
    enabled          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_metric_name (metric_name),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则';

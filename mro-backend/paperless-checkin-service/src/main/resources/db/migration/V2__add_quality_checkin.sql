-- MRO-008 T-024: Quality check-in tables
-- Database: mro_workcard

CREATE TABLE IF NOT EXISTS `quality_sign_record` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `workcard_id`    BIGINT        NOT NULL COMMENT '工卡ID',
  `step_id`        BIGINT        NOT NULL COMMENT '工步ID',
  `signer_id`      BIGINT        NOT NULL COMMENT '签署人ID',
  `result`         ENUM('pass','fail') NOT NULL COMMENT '质检结果',
  `comment`        VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `sign_time`      DATETIME(3)   NOT NULL COMMENT '签署时间',
  `signature_hash` VARCHAR(128)  DEFAULT NULL COMMENT '签名哈希',
  `deleted`        TINYINT(1)    NOT NULL DEFAULT 0,
  `create_time`    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time`    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_workcard_id` (`workcard_id`),
  KEY `idx_step_id` (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检签署记录';

CREATE TABLE IF NOT EXISTS `ncr` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `workcard_id`     BIGINT        NOT NULL COMMENT '工卡ID',
  `quality_sign_id` BIGINT        NOT NULL COMMENT '关联质检记录ID',
  `ncr_no`          VARCHAR(32)   NOT NULL COMMENT 'NCR编号',
  `title`           VARCHAR(200)  NOT NULL COMMENT '标题',
  `description`     TEXT          DEFAULT NULL COMMENT '详细描述',
  `severity`        VARCHAR(20)   DEFAULT NULL COMMENT '严重程度',
  `status`          ENUM('open','in_progress','closed') NOT NULL DEFAULT 'open' COMMENT 'NCR状态',
  `assigned_to`     BIGINT        DEFAULT NULL COMMENT '指派人ID',
  `close_signature` VARCHAR(128)  DEFAULT NULL COMMENT '关闭签名',
  `closed_at`       DATETIME(3)   DEFAULT NULL COMMENT '关闭时间',
  `created_by`      BIGINT        NOT NULL COMMENT '创建人ID',
  `deleted`         TINYINT(1)    NOT NULL DEFAULT 0,
  `create_time`     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time`     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ncr_no` (`ncr_no`),
  KEY `idx_workcard_id` (`workcard_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不符合项报告';

CREATE TABLE IF NOT EXISTS `workcard_checkin` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `workcard_id`    BIGINT        NOT NULL COMMENT '工卡ID',
  `user_id`        BIGINT        NOT NULL COMMENT '用户ID',
  `check_in_time`  DATETIME(3)   DEFAULT NULL COMMENT '签到时间',
  `check_out_time` DATETIME(3)   DEFAULT NULL COMMENT '签退时间',
  `location`       VARCHAR(100)  DEFAULT NULL COMMENT '位置',
  `device_id`      VARCHAR(64)   DEFAULT NULL COMMENT '设备ID',
  `create_time`    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_workcard_id` (`workcard_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工卡签到记录';

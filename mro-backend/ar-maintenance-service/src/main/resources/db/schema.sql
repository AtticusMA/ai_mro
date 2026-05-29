CREATE DATABASE IF NOT EXISTS mro_ar DEFAULT CHARACTER SET utf8mb4;
USE mro_ar;

CREATE TABLE inspection_task (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id    VARCHAR(32)  NOT NULL COMMENT '飞机注册号',
    inspector_id   BIGINT       NOT NULL COMMENT '执行人ID',
    route_template VARCHAR(64)  NOT NULL COMMENT '巡检路线模板',
    status         VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/in_progress/completed',
    started_at     DATETIME     NULL,
    completed_at   DATETIME     NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_aircraft (aircraft_id),
    INDEX idx_inspector (inspector_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检任务';

CREATE TABLE anomaly_record (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id      BIGINT          NOT NULL COMMENT '关联巡检任务',
    anomaly_type VARCHAR(64)     NOT NULL COMMENT '异常类型',
    confidence   DECIMAL(5,4)    NOT NULL COMMENT 'AI置信度 0~1',
    snapshot_url VARCHAR(512)    NULL     COMMENT '异常截图MinIO地址',
    detected_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      TINYINT         NOT NULL DEFAULT 0,
    INDEX idx_task (task_id),
    INDEX idx_detected (detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常记录';

CREATE TABLE ar_session (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    caller_id        BIGINT       NOT NULL COMMENT '发起人ID',
    task_id          BIGINT       NULL     COMMENT '关联巡检任务',
    expert_id        BIGINT       NULL     COMMENT '专家ID',
    status           VARCHAR(20)  NOT NULL DEFAULT 'waiting' COMMENT 'waiting/active/ended',
    signaling_token  VARCHAR(64)  NOT NULL COMMENT 'WebRTC信令token',
    recording_url    VARCHAR(512) NULL     COMMENT '录像MinIO地址',
    duration_seconds INT          NULL     COMMENT '通话时长秒',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_caller (caller_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='远程协作会话';

CREATE TABLE video_archive (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id          BIGINT       NULL     COMMENT '关联巡检任务',
    session_id       BIGINT       NULL     COMMENT '关联协作会话',
    file_url         VARCHAR(512) NOT NULL COMMENT '视频文件地址',
    duration_seconds INT          NOT NULL DEFAULT 0 COMMENT '时长秒',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_task (task_id),
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影像档案';

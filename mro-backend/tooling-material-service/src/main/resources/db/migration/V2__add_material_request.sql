-- MRO-006 T-025 Material Request Table
-- Refs: MRO-006

CREATE TABLE IF NOT EXISTS material_request (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no    VARCHAR(32)  NOT NULL UNIQUE COMMENT '申请单号，格式 MR-YYYYMMDD-NNNN',
    workcard_id   BIGINT       NOT NULL COMMENT '关联工卡ID',
    requester_id  BIGINT       NOT NULL COMMENT '申请人ID',
    dept_id       BIGINT       COMMENT '申请部门ID',
    urgency       VARCHAR(16)  NOT NULL DEFAULT 'normal' COMMENT 'low/normal/high/urgent',
    status        VARCHAR(32)  NOT NULL DEFAULT 'pending_approval'
                  COMMENT 'pending_approval/approved/rejected/delivered/received',
    reject_reason VARCHAR(512) COMMENT '驳回原因',
    items_json    TEXT         NOT NULL COMMENT 'JSON array of {partNo,partName,qty,unit,estimatedCost}',
    approved_by   BIGINT       COMMENT '审批人ID',
    approved_at   DATETIME(3)  COMMENT '审批时间',
    received_by   BIGINT       COMMENT '领料人ID',
    received_at   DATETIME(3)  COMMENT '领料确认时间',
    created_by    BIGINT       NOT NULL,
    create_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted       TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_workcard (workcard_id),
    INDEX idx_requester (requester_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='航材领料申请单';

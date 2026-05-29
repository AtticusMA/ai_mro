-- MRO-005 Digital Twin Hangar Schema
-- Refs: MRO-005

CREATE TABLE IF NOT EXISTS hangar_model (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(128) NOT NULL COMMENT '机库名称',
    model_url   VARCHAR(512) NOT NULL COMMENT '3D模型文件地址',
    version     VARCHAR(32)  NOT NULL COMMENT '模型版本',
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机库模型';

CREATE TABLE IF NOT EXISTS workstation (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    hangar_id           BIGINT         NOT NULL COMMENT '所属机库ID',
    name                VARCHAR(64)    NOT NULL COMMENT '工位名称',
    position_x          DECIMAL(10,2)  NOT NULL COMMENT '3D坐标X',
    position_y          DECIMAL(10,2)  NOT NULL COMMENT '3D坐标Y',
    position_z          DECIMAL(10,2)  NOT NULL COMMENT '3D坐标Z',
    status              VARCHAR(16)    NOT NULL DEFAULT 'idle' COMMENT '状态: idle/occupied/maintenance',
    current_aircraft_id VARCHAR(32)    NULL COMMENT '当前停靠飞机注册号',
    deleted             TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_hangar_id (hangar_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工位';

CREATE TABLE IF NOT EXISTS production_plan (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    hangar_id        BIGINT       NOT NULL COMMENT '所属机库ID',
    aircraft_id      VARCHAR(32)  NOT NULL COMMENT '飞机注册号',
    plan_type        VARCHAR(16)  NOT NULL COMMENT '计划类型: line/heavy/component',
    scheduled_start  DATETIME     NOT NULL COMMENT '计划开始时间',
    scheduled_end    DATETIME     NOT NULL COMMENT '计划结束时间',
    status           VARCHAR(16)  NOT NULL DEFAULT 'draft' COMMENT '状态: draft/approved/in_progress/completed',
    created_by       BIGINT       NOT NULL COMMENT '创建人用户ID',
    deleted          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_hangar_id (hangar_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划';

CREATE TABLE IF NOT EXISTS maintenance_order (
    id             BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    plan_id        BIGINT  NOT NULL COMMENT '关联生产计划ID',
    workstation_id BIGINT  NOT NULL COMMENT '分配工位ID',
    assignee_id    BIGINT  NOT NULL COMMENT '负责人用户ID',
    description    TEXT    NOT NULL COMMENT '指令描述',
    progress       INT     NOT NULL DEFAULT 0 COMMENT '完成进度0~100',
    status         VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/executing/completed/blocked',
    deleted        TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_plan_id (plan_id),
    KEY idx_workstation_id (workstation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修指令';

CREATE TABLE IF NOT EXISTS resource_usage (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    workstation_id BIGINT      NOT NULL COMMENT '关联工位ID',
    order_id       BIGINT      NULL COMMENT '关联维修指令ID',
    resource_type  VARCHAR(16) NOT NULL COMMENT '资源类型: personnel/equipment/material',
    resource_id    BIGINT      NOT NULL COMMENT '资源ID',
    allocated_at   DATETIME    NOT NULL COMMENT '分配时间',
    released_at    DATETIME    NULL COMMENT '释放时间',
    deleted        TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_workstation_id (workstation_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源使用记录';

-- MRO-005 T-020: Task Package tables
-- Refs: MRO-005 / T-020

CREATE TABLE IF NOT EXISTS task_package (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_no      VARCHAR(32)  NOT NULL UNIQUE COMMENT '任务包编号，格式 TP-YYYYMMDD-NNNN',
    title           VARCHAR(256) NOT NULL COMMENT '任务包标题',
    hangar_id       BIGINT       NOT NULL COMMENT '所属机库ID',
    workstation_id  BIGINT       COMMENT '关联工位ID',
    aircraft_type   VARCHAR(32)  COMMENT '机型',
    registration    VARCHAR(32)  COMMENT '飞机注册号',
    plan_start      DATE         NOT NULL COMMENT '计划开始日期',
    plan_end        DATE         NOT NULL COMMENT '计划结束日期',
    status          VARCHAR(32)  NOT NULL DEFAULT 'draft'
                    COMMENT 'draft/submitted/in_progress/completed/cancelled',
    priority        VARCHAR(16)  NOT NULL DEFAULT 'normal' COMMENT 'low/normal/high/urgent',
    created_by      BIGINT       NOT NULL,
    create_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_hangar (hangar_id),
    INDEX idx_status (status),
    INDEX idx_plan_start (plan_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务包';

CREATE TABLE IF NOT EXISTS task_package_order (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id      BIGINT       NOT NULL COMMENT '任务包ID',
    order_id        BIGINT       NOT NULL COMMENT '关联维修指令ID',
    seq_no          INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_pkg_order (package_id, order_id),
    INDEX idx_package (package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务包-维修指令关联';

CREATE TABLE IF NOT EXISTS personnel_assignment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id      BIGINT       NOT NULL COMMENT '任务包ID',
    user_id         BIGINT       NOT NULL COMMENT '人员ID',
    role            VARCHAR(32)  NOT NULL COMMENT '角色: lead/member/inspector',
    work_date       DATE         NOT NULL COMMENT '排班日期',
    shift           VARCHAR(16)  NOT NULL DEFAULT 'day' COMMENT 'day/night/full',
    create_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_pkg_user_date (package_id, user_id, work_date),
    INDEX idx_package (package_id),
    INDEX idx_user_date (user_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员排班分配';
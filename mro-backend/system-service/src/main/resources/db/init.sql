-- ============================================================
-- mro_system schema initialisation
-- MySQL 8  |  utf8mb4  |  InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS mro_system DEFAULT CHARACTER SET utf8mb4;
USE mro_system;

-- ------------------------------------------------------------
-- 1. 部门表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_dept (
  id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
  dept_name      VARCHAR(64)  NOT NULL,
  dept_code      VARCHAR(64)  NOT NULL UNIQUE,
  parent_id      BIGINT       DEFAULT 0,
  ancestors      VARCHAR(500) DEFAULT '0',
  order_num      INT          DEFAULT 0,
  leader         VARCHAR(64),
  phone          VARCHAR(20),
  email          VARCHAR(100),
  status         TINYINT      DEFAULT 0,
  is_deleted     TINYINT      DEFAULT 0,
  create_user_id BIGINT,
  create_dept_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME,
  INDEX idx_parent_id (parent_id),
  INDEX idx_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
  id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
  username        VARCHAR(64)  NOT NULL UNIQUE,
  password        VARCHAR(255) NOT NULL,
  real_name       VARCHAR(64),
  employee_no     VARCHAR(64),
  gender          TINYINT,
  phone           VARCHAR(20),
  email           VARCHAR(100),
  avatar          VARCHAR(500),
  dept_id         BIGINT,
  status          TINYINT      DEFAULT 0,
  is_deleted      TINYINT      DEFAULT 0,
  create_user_id  BIGINT,
  create_dept_id  BIGINT,
  create_time     DATETIME,
  update_user_id  BIGINT,
  update_time     DATETIME,
  last_login_time DATETIME,
  INDEX idx_username (username),
  INDEX idx_dept_id  (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 3. 角色表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
  id             BIGINT      PRIMARY KEY AUTO_INCREMENT,
  role_name      VARCHAR(64) NOT NULL,
  role_key       VARCHAR(64) NOT NULL UNIQUE,
  role_sort      INT         DEFAULT 0,
  data_scope     TINYINT     DEFAULT 1,
  status         TINYINT     DEFAULT 0,
  is_deleted     TINYINT     DEFAULT 0,
  create_user_id BIGINT,
  create_dept_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 4. 菜单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
  id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
  menu_name      VARCHAR(64)  NOT NULL,
  parent_id      BIGINT       DEFAULT 0,
  order_num      INT          DEFAULT 0,
  path           VARCHAR(255),
  component      VARCHAR(255),
  menu_type      CHAR(1)      COMMENT 'M=目录 C=菜单 F=按钮',
  perms          VARCHAR(255),
  icon           VARCHAR(64),
  visible        TINYINT      DEFAULT 1,
  status         TINYINT      DEFAULT 0,
  create_user_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 5. 关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
  id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_dept (
  id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_dept (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 6. 字典表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_dict (
  id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
  dict_group     VARCHAR(64)  NOT NULL COMMENT '字典分组，如gender/status',
  dict_code      VARCHAR(64)  NOT NULL COMMENT '字典编码（键）',
  dict_label     VARCHAR(128) NOT NULL COMMENT '显示标签',
  status         TINYINT      DEFAULT 0,
  remark         VARCHAR(255),
  create_user_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME,
  UNIQUE KEY uk_group_code  (dict_group, dict_code),
  INDEX  idx_dict_group     (dict_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 初始数据
-- ============================================================

-- 部门
INSERT IGNORE INTO sys_dept (id, dept_name, dept_code, parent_id, ancestors, order_num, status, create_time) VALUES
(1, 'A集团', 'ROOT', 0, '0',     1, 0, NOW()),
(2, '技术部', 'TECH', 1, '0,1',  1, 0, NOW()),
(3, '运维部', 'OPS',  1, '0,1',  2, 0, NOW()),
(4, '质检部', 'QA',   1, '0,1',  3, 0, NOW());

-- 用户（admin，密码 Admin@123 的 BCrypt 散列）
INSERT IGNORE INTO sys_user (id, username, password, real_name, dept_id, status, create_time) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.IU4TQLH5MbZc/yR4lK.5nHFaOZkJ9lu', '管理员', 1, 0, NOW());

-- 角色
INSERT IGNORE INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_time) VALUES
(1, '超级管理员', 'admin',       1, 1, 0, NOW()),
(2, 'MRO管理员',  'mro_manager', 2, 3, 0, NOW()),
(3, '普通用户',   'user',        3, 4, 0, NOW());

-- 用户-角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 菜单：顶级目录
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, icon, visible, status, create_time) VALUES
(1, '系统管理',     0, 1, '/system',  NULL, 'M', 'Setting',      1, 0, NOW()),
(2, '飞机健康监测', 0, 2, '/aircraft',NULL, 'M', 'Airplane',     1, 0, NOW()),
(3, '智能排故助手', 0, 3, '/fault',   NULL, 'M', 'Tools',        1, 0, NOW()),
(4, '维修手册管理', 0, 4, '/manual',  NULL, 'M', 'Document',     1, 0, NOW()),
(5, 'AR智慧维修',   0, 5, '/ar',      NULL, 'M', 'View',         1, 0, NOW()),
(6, '数字孪生机库', 0, 6, '/twin',    NULL, 'M', 'Grid',         1, 0, NOW()),
(7, '工具与航材',   0, 7, '/tooling', NULL, 'M', 'Box',          1, 0, NOW()),
(8, 'VR培训系统',   0, 8, '/vr',      NULL, 'M', 'VideoCamera',  1, 0, NOW()),
(9, '无纸化工卡',   0, 9, '/checkin', NULL, 'M', 'Memo',         1, 0, NOW());

-- 菜单：系统管理子菜单
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, visible, status, create_time) VALUES
(10, '用户管理', 1, 1, '/system/user', 'system/user/index', 'C', 'system:user:list', 'User',            1, 0, NOW()),
(11, '角色管理', 1, 2, '/system/role', 'system/role/index', 'C', 'system:role:list', 'UserFilled',      1, 0, NOW()),
(12, '菜单管理', 1, 3, '/system/menu', 'system/menu/index', 'C', 'system:menu:list', 'Menu',            1, 0, NOW()),
(13, '部门管理', 1, 4, '/system/dept', 'system/dept/index', 'C', 'system:dept:list', 'OfficeBuilding',  1, 0, NOW()),
(14, '字典管理', 1, 5, '/system/dict', 'system/dict/index', 'C', 'system:dict:list', 'Collection',      1, 0, NOW());

-- 菜单：用户管理按钮
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, visible, status, create_time) VALUES
(100, '用户新增', 10, 1, 'F', 'system:user:add',    0, 0, NOW()),
(101, '用户编辑', 10, 2, 'F', 'system:user:edit',   0, 0, NOW()),
(102, '用户删除', 10, 3, 'F', 'system:user:delete', 0, 0, NOW()),
(103, '密码重置', 10, 4, 'F', 'system:user:reset',  0, 0, NOW());

-- 菜单：角色管理按钮
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, visible, status, create_time) VALUES
(110, '角色新增', 11, 1, 'F', 'system:role:add',    0, 0, NOW()),
(111, '角色编辑', 11, 2, 'F', 'system:role:edit',   0, 0, NOW()),
(112, '角色删除', 11, 3, 'F', 'system:role:delete', 0, 0, NOW());

-- 角色-菜单：admin 拥有全部菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 角色-菜单：mro_manager 拥有系统只读 + 全部业务模块
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),(2, 10),(2, 13),(2, 14),
(2, 2),(2, 3),(2, 4),(2, 5),(2, 6),(2, 7),(2, 8),(2, 9);

-- 字典数据
INSERT IGNORE INTO sys_dict (dict_group, dict_code, dict_label, status, create_time) VALUES
-- 性别
('gender', '0', '未知', 0, NOW()),
('gender', '1', '男',   0, NOW()),
('gender', '2', '女',   0, NOW()),
-- 状态
('status', '0', '正常', 0, NOW()),
('status', '1', '禁用', 0, NOW()),
-- 菜单类型
('menu_type', 'M', '目录', 0, NOW()),
('menu_type', 'C', '菜单', 0, NOW()),
('menu_type', 'F', '按钮', 0, NOW()),
-- 数据权限
('data_scope', '1', '全部数据',     0, NOW()),
('data_scope', '2', '本部门',       0, NOW()),
('data_scope', '3', '本部门及子部门', 0, NOW()),
('data_scope', '4', '仅本人',       0, NOW()),
('data_scope', '5', '自定义部门',   0, NOW()),
-- 维修工单状态
('work_order_status', '0', '待处理', 0, NOW()),
('work_order_status', '1', '进行中', 0, NOW()),
('work_order_status', '2', '已完成', 0, NOW()),
('work_order_status', '3', '已关闭', 0, NOW()),
-- 故障等级
('fault_level', '1', '一级（严重）', 0, NOW()),
('fault_level', '2', '二级（重要）', 0, NOW()),
('fault_level', '3', '三级（一般）', 0, NOW()),
('fault_level', '4', '四级（轻微）', 0, NOW());

-- ------------------------------------------------------------
-- 操作日志表 (SYS-007)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_operation_log (
  id               BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键',
  request_id       VARCHAR(64)                               COMMENT '前端请求ID (X-Request-Id)',
  operator_id      BIGINT                                    COMMENT '操作人ID',
  operator_name    VARCHAR(64)                               COMMENT '操作人姓名',
  dept_id          BIGINT                                    COMMENT '操作人部门ID',
  request_uri      VARCHAR(512)  NOT NULL                    COMMENT '请求URI',
  request_method   VARCHAR(10)   NOT NULL                    COMMENT 'HTTP方法',
  request_params   TEXT                                      COMMENT '请求参数(query string)',
  request_body     TEXT                                      COMMENT '请求体(JSON)',
  response_status  INT                                       COMMENT 'HTTP响应状态码',
  cost_ms          BIGINT                                    COMMENT '耗时(毫秒)',
  client_ip        VARCHAR(64)                               COMMENT '客户端IP',
  user_agent       VARCHAR(512)                              COMMENT 'User-Agent',
  request_time     DATETIME      NOT NULL                    COMMENT '请求时间',
  INDEX idx_request_time (request_time),
  INDEX idx_dept_id      (dept_id),
  INDEX idx_operator_id  (operator_id),
  INDEX idx_request_id   (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ------------------------------------------------------------
-- 人员证照表 (MRO-009)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `personnel_license` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT        NOT NULL COMMENT '人员ID',
  `license_no`      VARCHAR(64)   NOT NULL COMMENT '证照编号',
  `license_type`    VARCHAR(50)   NOT NULL COMMENT '证照类型',
  `aircraft_type`   VARCHAR(50)   DEFAULT NULL COMMENT '适用机型',
  `category`        VARCHAR(50)   DEFAULT NULL COMMENT '类别/专业',
  `issuer`          VARCHAR(100)  DEFAULT NULL COMMENT '发证机构',
  `issue_date`      DATE          NOT NULL COMMENT '签发日期',
  `expiry_date`     DATE          NOT NULL COMMENT '到期日期',
  `status`          VARCHAR(20)   NOT NULL DEFAULT 'valid' COMMENT '状态(valid/expiring/expired) — 查询时动态计算',
  `file_url`        VARCHAR(500)  DEFAULT NULL COMMENT '证照文件URL',
  `remark`          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `deleted`         TINYINT(1)    NOT NULL DEFAULT 0,
  `create_time`     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time`     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_license_type` (`license_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员证照';

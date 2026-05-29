-- Refs: AUTH-001 / PLAT-002
-- 初始化认证服务所需数据库表（mro_auth schema）
-- 可幂等执行

CREATE DATABASE IF NOT EXISTS mro_auth DEFAULT CHARACTER SET utf8mb4;
USE mro_auth;

-- ── sys_user ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_user (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  username        VARCHAR(64)  NOT NULL UNIQUE,
  password        VARCHAR(255) NOT NULL,
  real_name       VARCHAR(64),
  employee_no     VARCHAR(64)  UNIQUE,
  gender          TINYINT,
  phone           VARCHAR(20)  UNIQUE,
  email           VARCHAR(100) UNIQUE,
  avatar          VARCHAR(255),
  address         VARCHAR(255),
  dept_id         BIGINT,
  status          TINYINT      DEFAULT 1  COMMENT '1正常 0禁用',
  is_deleted      TINYINT      DEFAULT 0,
  create_user_id  BIGINT,
  create_dept_id  BIGINT,
  create_time     DATETIME,
  update_user_id  BIGINT,
  update_time     DATETIME,
  last_login_time DATETIME,
  INDEX idx_dept_id (dept_id),
  INDEX idx_status  (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── sys_dept ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_dept (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  dept_name      VARCHAR(64)  NOT NULL,
  dept_code      VARCHAR(64)  NOT NULL UNIQUE,
  parent_id      BIGINT,
  ancestors      VARCHAR(255),
  order_num      INT,
  leader         VARCHAR(64),
  phone          VARCHAR(20),
  email          VARCHAR(100),
  status         TINYINT      DEFAULT 1,
  is_deleted     TINYINT      DEFAULT 0,
  create_user_id BIGINT,
  create_dept_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME,
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── sys_role ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_role (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name      VARCHAR(64) NOT NULL,
  role_key       VARCHAR(64) NOT NULL UNIQUE,
  role_sort      INT,
  data_scope     TINYINT     DEFAULT 1 COMMENT '1全部 2本部门及以下 3本部门 4仅本人 5自定义',
  status         TINYINT     DEFAULT 1,
  is_deleted     TINYINT     DEFAULT 0,
  create_user_id BIGINT,
  create_dept_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME,
  INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── sys_menu ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_menu (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  menu_name      VARCHAR(64)  NOT NULL,
  parent_id      BIGINT,
  order_num      INT,
  path           VARCHAR(255),
  component      VARCHAR(255),
  menu_type      CHAR(1) COMMENT 'M目录 C菜单 F按钮',
  perms          VARCHAR(255),
  icon           VARCHAR(64),
  visible        TINYINT DEFAULT 1,
  status         TINYINT DEFAULT 1,
  create_user_id BIGINT,
  create_time    DATETIME,
  update_user_id BIGINT,
  update_time    DATETIME,
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── sys_user_role ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_user_role (
  id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_user_id (user_id),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── sys_role_menu ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_role_menu (
  id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  perms   VARCHAR(255),
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 初始化数据 ────────────────────────────────────────────
-- 根部门
INSERT IGNORE INTO sys_dept (id, dept_name, dept_code, parent_id, ancestors, order_num, status, create_time)
VALUES (1, 'A集团', 'ROOT', 0, '0', 0, 1, NOW());

-- 超管角色
INSERT IGNORE INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_time)
VALUES (1, '超级管理员', 'admin', 0, 1, 1, NOW());

-- 超管用户（密码 Admin@123，BCrypt 散列）
INSERT IGNORE INTO sys_user (id, username, password, real_name, dept_id, status, create_time)
VALUES (1, 'admin',
  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  '超级管理员', 1, 1, NOW());

-- 绑定超管用户 ↔ 角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

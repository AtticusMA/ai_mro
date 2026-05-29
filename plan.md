# 项目需求文档完善计划

## Context（背景）
用户提供了一个初步的AI工作辅助系统需求文档，但存在以下问题：
1. **拼写错误**：多处出现"微服务矿建"（应为"框架"）、"depet"（应为"dept"）等错误
2. **安全问题**：使用MD5加密密码（已不安全）
3. **需求不完整**：缺少角色管理模块、详细的功能说明、完整的用户字段
4. **设计不明确**：数据权限过滤逻辑需要更详细的说明、缺少完整的数据库表设计
5. **认证方式混淆**：文档中同时提到SSO和用户名密码登录，但用户明确只需要用户名/密码登录

本计划旨在：
- 修正所有拼写和术语错误
- 补充完整的功能模块说明
- 设计详细的数据库表结构（包括字段类型、长度、索引、约束）
- 详细说明数据权限过滤的实现逻辑（包括SQL示例）
- 提供清晰的系统架构说明

## 计划步骤

### 1. 创建完善的需求文档（项目需求文档-完善版.md）

#### 1.1 修正基础信息
- 修正"微服务矿建" → "微服务框架"
- 统一所有"depet" → "dept"
- 明确认证方式：仅使用用户名/密码登录（不使用SSO）
- 密码加密方式：MD5 → BCrypt

#### 1.2 补充完整的功能模块
**系统管理模块**：
- 1.1 部门管理
  - 树形展示部门结构
  - 增加、修改、删除部门
  - Excel导入部门
  - 启用/禁用部门
  
- 1.2 用户管理
  - 用户列表（分页、搜索、筛选）
  - 新增用户（表单验证）
  - 编辑用户信息
  - 删除用户（软删除）
  - Excel批量导入用户
  - 重置密码
  - 启用/禁用用户
  - 分配角色
  - 配置数据权限
  
- 1.3 角色管理（新增模块）
  - 角色列表
  - 创建角色
  - 编辑角色
  - 删除角色
  - 分配菜单权限
  - 启用/禁用角色
  
- 1.4 菜单/权限管理
  - 树形展示菜单结构
  - 新增菜单/按钮权限
  - 编辑菜单
  - 删除菜单
  - 排序调整
  - 启用/禁用
  
- 1.5 字典管理
  - 字典类型管理（增删改查）
  - 字典数据管理（增删改查）
  - 启用/禁用字典项

#### 1.3 详细说明数据权限机制

**五种数据权限类型**：
1. 全部数据（超管）
2. 本部门数据
3. 本部门及子部门数据
4. 本人数据
5. 自定义部门数据

**数据权限实现逻辑**：
- 所有业务数据表必须包含：`create_user_id`、`create_dept_id`、`use_for_dept_id`（可为空）
- 通过MyBatis拦截器在SQL执行前动态添加WHERE条件
- 根据用户的数据权限类型生成不同的过滤条件

**SQL过滤示例**（将在文档中详细说明）：
- 本部门：`WHERE create_dept_id = ? OR use_for_dept_id = ?`
- 本部门及子部门：`WHERE create_dept_id IN (?, ?, ...) OR use_for_dept_id IN (?, ?, ...)`
- 自定义部门：查询用户关联的所有部门及其子部门，然后用IN条件过滤

### 2. 创建详细的数据库设计文档（数据库设计文档.md）

#### 2.1 核心表设计

**sys_user（用户表）**
- 基础字段：id, username, password, real_name, employee_no, gender, phone, email, avatar, address
- 状态字段：status（0禁用/1启用）, is_deleted（0未删除/1已删除）
- 关联字段：dept_id（部门ID）
- 审计字段：create_user_id, create_dept_id, create_time, update_user_id, update_time, last_login_time
- 索引：username（唯一）, phone（唯一）, dept_id, status

**sys_dept（部门表）**
- 基础字段：id, dept_name, dept_code, parent_id, ancestors（祖级列表）, order_num, leader, phone, email
- 状态字段：status, is_deleted
- 审计字段：create_user_id, create_dept_id, create_time, update_user_id, update_time
- 索引：parent_id, dept_code（唯一）

**sys_role（角色表）**
- 基础字段：id, role_name, role_key, role_sort, data_scope（数据权限类型：1全部/2本部门/3本部门及子部门/4本人/5自定义）
- 状态字段：status, is_deleted
- 审计字段：create_user_id, create_dept_id, create_time, update_user_id, update_time
- 索引：role_key（唯一）

**sys_user_role（用户角色关联表）**
- 字段：id, user_id, role_id, create_time
- 索引：user_id, role_id, 联合唯一索引(user_id, role_id)

**sys_role_dept（角色部门关联表 - 用于自定义数据权限）**
- 字段：id, role_id, dept_id, create_time
- 说明：当角色的data_scope=5（自定义部门）时，在此表中存储该角色可访问的部门ID
- 索引：role_id, dept_id, 联合索引(role_id, dept_id)

**sys_menu（菜单表）**
- 基础字段：id, menu_name, parent_id, order_num, path, component, menu_type（M目录/C菜单/F按钮）, perms（权限标识）, icon
- 状态字段：visible（是否显示）, status
- 审计字段：create_user_id, create_time, update_user_id, update_time
- 索引：parent_id

**sys_role_menu（角色菜单关联表）**
- 字段：id, role_id, menu_id, create_time
- 索引：role_id, menu_id, 联合唯一索引(role_id, menu_id)

**sys_dict_type（字典类型表）**
- 字段：id, dict_name, dict_type（唯一标识）, status, remark, create_user_id, create_time, update_user_id, update_time
- 索引：dict_type（唯一）

**sys_dict_data（字典数据表）**
- 字段：id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, remark, create_user_id, create_time, update_user_id, update_time
- 索引：dict_type

**sys_user_contact（用户紧急联系人表）**
- 字段：id, user_id, contact_name, contact_phone, relationship, address, create_time, update_time
- 索引：user_id

#### 2.2 数据权限过滤详细设计

**MyBatis拦截器实现逻辑**：
1. 拦截所有SELECT语句
2. 获取当前登录用户信息
3. 查询用户的角色及数据权限类型
4. 根据数据权限类型构建WHERE条件：
   - 超管：不添加任何过滤条件
   - 本人：`AND (create_user_id = #{userId})`
   - 本部门：`AND (create_dept_id = #{deptId} OR use_for_dept_id = #{deptId})`
   - 本部门及子部门：先查询部门树获取所有子部门ID，然后 `AND (create_dept_id IN (...) OR use_for_dept_id IN (...))`
   - 自定义部门：从sys_role_dept查询角色关联的部门，获取这些部门及其子部门，然后 `AND (create_dept_id IN (...) OR use_for_dept_id IN (...))`
5. 动态修改SQL，添加过滤条件

**部门树查询算法**：
- 使用递归CTE（Common Table Expression）或递归查询
- 根据parent_id和ancestors字段快速定位子部门

### 3. 创建技术架构说明文档（技术架构说明.md）

#### 3.1 前端技术栈
- Vue.js 3.x
- Tailwind CSS
- Vue Router
- Pinia（状态管理）
- Axios（HTTP客户端）
- Element Plus / Ant Design Vue（UI组件库）

#### 3.2 后端技术栈
- Spring Boot 2.7.x / 3.x
- Spring Security + JWT
- MyBatis Plus
- Redis（缓存、Session）
- Nacos（服务注册与配置中心）
- Dubbo（RPC框架）
- MySQL 8.0

#### 3.3 微服务模块划分
- gateway-service（网关服务）
- auth-service（认证服务）
- system-service（系统管理服务）
- 其他业务服务...

#### 3.4 安全设计
- JWT Token认证
- BCrypt密码加密
- RBAC权限模型
- 动态SQL数据权限过滤
- 接口防重放
- XSS防护
- SQL注入防护

### 4. 创建API接口设计文档（API接口设计.md）

列出所有模块的RESTful API接口：
- 登录/登出接口
- 用户管理接口
- 部门管理接口
- 角色管理接口
- 菜单管理接口
- 字典管理接口

每个接口包含：
- 请求方法
- 请求路径
- 请求参数
- 响应格式
- 权限要求

## 关键文件

将创建以下文件：
- `D:\ai_code\ui\项目需求文档-完善版.md` - 完善后的需求文档
- `D:\ai_code\ui\数据库设计文档.md` - 详细的数据库表设计
- `D:\ai_code\ui\技术架构说明.md` - 技术选型和架构设计
- `D:\ai_code\ui\API接口设计.md` - RESTful API接口文档
- `D:\ai_code\ui\数据权限实现方案.md` - 数据权限过滤的详细实现方案

## 验证方式

文档完成后，将进行以下验证：
1. 检查所有拼写错误是否已修正
2. 确认所有功能模块是否完整（CRUD操作齐全）
3. 验证数据库表设计是否包含所有必要字段
4. 确认数据权限逻辑是否清晰可实现
5. 检查是否有遗漏的常见功能（如日志记录、操作审计等）

## 预期成果

完成后将得到：
- ✅ 一套完整、规范、可直接用于开发的需求文档
- ✅ 详细的数据库设计（包含建表SQL）
- ✅ 清晰的技术架构说明
- ✅ 完整的API接口文档
- ✅ 可落地的数据权限实现方案

这些文档将作为后续开发的标准依据，确保开发团队能够准确理解需求并高效实施。

---
id: SYS-004
title: 菜单/权限管理
domain: system
status: draft
owner: '@product'
version: 1.0.0
created: 2026-05-23
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 菜单/权限管理

## 1. 背景与目标

菜单 + 按钮权限是前端动态渲染与后端鉴权的统一来源。`perms` 字段是权限码。
前端侧边栏菜单必须通过接口动态加载，不允许硬编码，以支持不发版调整菜单结构与权限。

## 2. 范围

### In Scope
- 树形展示、增删改、排序、启停、隐藏。
- 菜单类型：M 目录 / C 菜单 / F 按钮。
- 前端侧边栏菜单动态加载（基于 `/api/system/menu/user` 接口）。
- 前端权限码（perms）动态注入权限系统。

### Out of Scope
- 字段级权限（首期不做）。

## 3. 用户故事

- 作为管理员，我希望在不发版的情况下调整菜单结构与按钮权限码。
- 作为系统，我希望前端侧边栏在用户登录后自动从服务端加载菜单，以确保菜单与权限配置实时一致。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 类型约束 | F 必须挂在 C 下；C 必须有 path/component；M 仅作分组 |
| FR-2 | perms 唯一 | `module:action` 格式，跨菜单不重复 |
| FR-3 | 隐藏 | visible=0 不渲染但权限仍生效 |
| FR-4 | 前端动态菜单加载 | Given 用户登录成功，When 进入主界面，Then 前端调用 `GET /api/system/menu/user` 获取当前用户菜单树，侧边栏按返回数据渲染，禁止任何硬编码菜单数组 |
| FR-5 | 前端权限码注入 | Given `GET /api/system/menu/user` 返回用户 perms 列表，When 数据加载完成，Then 将 perms 写入前端权限系统（`src/utils/permission.js` 的 `hasPermission` 可正确判断），`v-permission` 指令生效 |
| FR-6 | 菜单默认排序 | 侧边栏菜单显示顺序：仪表板 → MRO 业务模块（健康监控、AR协作、排故助手、手册管理、数字机库、工具物料、VR训练、电子工卡）→ 系统管理（置底），`order_num` 字段控制，系统管理目录 order_num 值最大 |

## 5. 非功能需求

- 用户登录后菜单接口走缓存；变更后失效。
- 菜单接口 P95 < 500ms（复用 Charter 性能约束）。

## 6. 数据契约

| 字段 | 类型 | 说明 |
|------|------|------|
| menu_name | varchar(64) | |
| parent_id | bigint | |
| order_num | int | |
| path | varchar(255) | C 必填 |
| component | varchar(255) | C 必填 |
| menu_type | char(1) | M/C/F |
| perms | varchar(100) | 权限码 |
| icon | varchar(100) | |
| visible | tinyint | |
| status | tinyint | |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/system/menu/tree | menu:list | 完整菜单树（管理页用） |
| POST | /api/system/menu | menu:add | 新增菜单 |
| PUT | /api/system/menu/{id} | menu:edit | 更新菜单 |
| DELETE | /api/system/menu/{id} | menu:delete | 删除菜单 |
| GET | /api/system/menu/user | 已登录 | 当前用户的菜单树 + perms 列表（侧边栏专用） |

### GET /api/system/menu/user 响应结构

```json
{
  "code": 200,
  "data": {
    "menus": [ /* 树形菜单，仅含 type=M/C 且 visible=1 的节点 */ ],
    "permissions": [ "system:user:list", "log:list", "..." ]
  }
}
```

## 8. 权限边界

- 菜单权限：`menu:*`。
- 超管 `*:*:*` 视同拥有所有 perms。

## 9. 验收标准

- [ ] 删除有子菜单的目录被禁止。
- [ ] 侧边栏 `Sidebar.vue` 中不存在任何硬编码菜单数组，所有菜单数据来自 `GET /api/system/menu/user`。
- [ ] 切换不同权限的账号登录，侧边栏显示的菜单项与该账号的角色权限配置一致。
- [ ] `v-permission` 指令在菜单加载完成后能正确控制按钮的显示/隐藏。

## 10. 未决问题

- [ ] 是否支持外链菜单？
- [ ] 菜单数据是否需要前端缓存到 Pinia store（避免页面刷新重复请求）？

---

## B. 后端实现约束

### B.1 接口 JSON Schema

#### GET /api/system/menu/tree（完整菜单树，管理页用）

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "parentId": 0,
      "orderNum": 100,
      "path": "/system",
      "component": null,
      "menuType": "M",
      "perms": null,
      "icon": "setting",
      "visible": 1,
      "status": 1,
      "children": [
        {
          "id": 10,
          "menuName": "用户管理",
          "parentId": 1,
          "orderNum": 1,
          "path": "/system/user",
          "component": "system/user/index",
          "menuType": "C",
          "perms": null,
          "icon": "user",
          "visible": 1,
          "status": 1,
          "children": [
            {
              "id": 100,
              "menuName": "新增用户",
              "parentId": 10,
              "orderNum": 1,
              "path": null,
              "component": null,
              "menuType": "F",
              "perms": "user:add",
              "icon": null,
              "visible": 1,
              "status": 1,
              "children": []
            }
          ]
        }
      ]
    }
  ],
  "timestamp": 1716700800000
}
```

#### GET /api/system/menu/user（当前用户菜单，侧边栏用）

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "menus": [
      {
        "id": 1,
        "menuName": "系统管理",
        "path": "/system",
        "icon": "setting",
        "children": [
          {
            "id": 10,
            "menuName": "用户管理",
            "path": "/system/user",
            "component": "system/user/index",
            "icon": "user",
            "children": []
          }
        ]
      }
    ],
    "permissions": ["user:list", "user:add", "dept:list"]
  },
  "timestamp": 1716700800000
}
```

注意：`menus` 仅含 `menuType = M/C` 且 `visible = 1`、`status = 1` 的节点；`permissions` 为该用户拥有的所有按钮权限码（`menuType = F`）。

#### POST /api/system/menu（新增）

请求：
```json
{
  "menuName": "新增用户",
  "parentId": 10,
  "orderNum": 1,
  "path": null,
  "component": null,
  "menuType": "F",
  "perms": "user:add",
  "icon": null,
  "visible": 1,
  "status": 1
}
```

### B.2 Dubbo 接口签名（system-service）

```java
public interface MenuDubboService {

    /** 查询完整菜单树（管理页） */
    List<MenuTreeDTO> getMenuTree();

    /** 查询当前用户可见菜单树 + 权限码（带缓存） */
    UserMenuResult getUserMenuAndPermissions(Long userId);

    /** 新增菜单 */
    Long createMenu(CreateMenuCommand cmd);

    /** 编辑菜单 */
    void updateMenu(UpdateMenuCommand cmd);

    /** 删除菜单（有子菜单时拒绝） */
    void deleteMenu(Long menuId);

    /** 查询角色已分配的菜单 ID 列表（角色编辑回显用） */
    List<Long> getMenuIdsByRoleId(Long roleId);
}

public record MenuTreeDTO(
    Long id, String menuName, Long parentId, Integer orderNum,
    String path, String component, String menuType, String perms,
    String icon, Integer visible, Integer status,
    List<MenuTreeDTO> children
) implements Serializable {}

public record UserMenuResult(
    List<MenuTreeDTO> menus,
    List<String> permissions
) implements Serializable {}

public record CreateMenuCommand(
    String menuName, Long parentId, Integer orderNum,
    String path, String component, String menuType,
    String perms, String icon, Integer visible, Integer status
) implements Serializable {}

public record UpdateMenuCommand(
    Long id, String menuName, Long parentId, Integer orderNum,
    String path, String component, String menuType,
    String perms, String icon, Integer visible, Integer status
) implements Serializable {}
```

### B.3 错误码（system-service 菜单，4160–4179）

| 错误码 | 含义 |
|--------|------|
| 4160 | perms 权限码已存在 |
| 4161 | 菜单不存在 |
| 4162 | F 类型菜单必须挂在 C 类型下 |
| 4163 | 该菜单下存在子菜单，无法删除 |
| 4164 | C 类型菜单 path 和 component 不能为空 |

### B.4 Redis 缓存策略

- Key：`sys:user:menu:{userId}`，TTL：30min
- 菜单变更（增/删/改）、角色菜单变更后，批量失效受影响用户缓存

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，源自 §3.2.4 |
| 0.2.0 | 2026-05-23 | 新增 FR-4（前端动态菜单加载）、FR-5（perms 权限码注入），补充验收标准，禁止硬编码菜单 |
| 0.2.1 | 2026-05-25 | 新增 FR-6（菜单默认排序），系统管理置底，MRO 业务模块优先显示 |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：完整 JSON Schema、Dubbo 接口签名、错误码 4160–4179、Redis 缓存策略 |

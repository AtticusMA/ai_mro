let _roles = [
  { id: 1, name: '超级管理员', code: 'admin', sort: 1, status: 1, remark: '拥有所有权限', menuIds: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20], createTime: '2024-01-01 08:00:00' },
  { id: 2, name: '部门经理', code: 'manager', sort: 2, status: 1, remark: '部门管理权限', menuIds: [1,2,3,5,6,7,8,9,10], createTime: '2024-01-15 09:00:00' },
  { id: 3, name: '普通员工', code: 'user', sort: 3, status: 1, remark: '基本查看权限', menuIds: [1,2], createTime: '2024-02-01 10:00:00' },
  { id: 4, name: '审计员', code: 'auditor', sort: 4, status: 1, remark: '审计查看权限', menuIds: [1,2,3,4,7,8], createTime: '2024-03-01 08:00:00' },
  { id: 5, name: '已停用角色', code: 'deprecated', sort: 5, status: 0, remark: '已停用', menuIds: [], createTime: '2024-04-01 08:00:00' },
]
let _nextId = 100

export default [
  {
    url: '/api/system/role/page',
    method: 'get',
    response: ({ query }) => {
      let list = [..._roles]
      if (query.name) list = list.filter(r => r.name.includes(query.name))
      if (query.code) list = list.filter(r => r.code.includes(query.code))
      if (query.status !== undefined && query.status !== '') list = list.filter(r => r.status === Number(query.status))
      const page = Number(query.page) || 1
      const pageSize = Number(query.pageSize) || 10
      return { code: 200, message: '获取成功', data: { list: list.slice((page-1)*pageSize, page*pageSize), total: list.length, page, pageSize } }
    },
  },
  {
    url: '/api/system/role/list',
    method: 'get',
    response: () => ({ code: 200, message: '获取成功', data: _roles.filter(r => r.status === 1) }),
  },
  {
    url: '/api/system/role/detail',
    method: 'get',
    response: ({ query }) => {
      const role = _roles.find(r => r.id === Number(query.id))
      return role ? { code: 200, message: '获取成功', data: role } : { code: 404, message: '角色不存在', data: null }
    },
  },
  {
    url: '/api/system/role/create',
    method: 'post',
    response: ({ body }) => {
      const newRole = { ...body, id: ++_nextId, createTime: new Date().toLocaleString() }
      _roles.push(newRole)
      return { code: 200, message: '创建成功', data: newRole }
    },
  },
  {
    url: '/api/system/role/update',
    method: 'put',
    response: ({ body }) => {
      const idx = _roles.findIndex(r => r.id === body.id)
      if (idx === -1) return { code: 404, message: '角色不存在', data: null }
      _roles[idx] = { ..._roles[idx], ...body }
      return { code: 200, message: '更新成功', data: _roles[idx] }
    },
  },
  {
    url: '/api/system/role/remove',
    method: 'delete',
    response: ({ query }) => {
      const idx = _roles.findIndex(r => r.id === Number(query.id))
      if (idx === -1) return { code: 404, message: '角色不存在', data: null }
      _roles.splice(idx, 1)
      return { code: 200, message: '删除成功', data: null }
    },
  },
]

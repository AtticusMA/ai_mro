const buildTree = (list, parentId = 0) =>
  list.filter(d => d.parentId === parentId).map(d => ({ ...d, children: buildTree(list, d.id) }))

let _depts = [
  { id: 1, parentId: 0, name: 'A集团', sort: 1, leader: '张三', phone: '13800138001', email: 'admin@example.com', status: 1 },
  { id: 2, parentId: 1, name: '技术部', sort: 1, leader: '李四', phone: '13800138002', email: 'tech@example.com', status: 1 },
  { id: 3, parentId: 1, name: '市场部', sort: 2, leader: '王五', phone: '13800138003', email: 'market@example.com', status: 1 },
  { id: 4, parentId: 2, name: '前端组', sort: 1, leader: '赵六', phone: '13800138004', email: 'frontend@example.com', status: 1 },
  { id: 5, parentId: 2, name: '后端组', sort: 2, leader: '钱七', phone: '13800138005', email: 'backend@example.com', status: 1 },
  { id: 6, parentId: 1, name: '财务部', sort: 3, leader: '孙八', phone: '13800138006', email: 'finance@example.com', status: 1 },
  { id: 7, parentId: 1, name: '人事部', sort: 4, leader: '周九', phone: '13800138007', email: 'hr@example.com', status: 1 },
  { id: 8, parentId: 3, name: '品牌组', sort: 1, leader: '吴十', phone: '13800138008', email: 'brand@example.com', status: 1 },
  { id: 9, parentId: 3, name: '推广组', sort: 2, leader: '郑一', phone: '13800138009', email: 'promo@example.com', status: 0 },
  { id: 10, parentId: 1, name: '运营部', sort: 5, leader: '黄二', phone: '13800138010', email: 'ops@example.com', status: 1 },
]
let _nextId = 100

export default [
  {
    url: '/api/system/dept/list',
    method: 'get',
    response: () => ({ code: 200, message: '获取成功', data: buildTree(_depts) }),
  },
  {
    url: '/api/system/dept/detail',
    method: 'get',
    response: ({ query }) => {
      const dept = _depts.find(d => d.id === Number(query.id))
      return dept ? { code: 200, message: '获取成功', data: dept } : { code: 404, message: '部门不存在', data: null }
    },
  },
  {
    url: '/api/system/dept/create',
    method: 'post',
    response: ({ body }) => {
      const newDept = { ...body, id: ++_nextId }
      _depts.push(newDept)
      return { code: 200, message: '创建成功', data: newDept }
    },
  },
  {
    url: '/api/system/dept/update',
    method: 'put',
    response: ({ body }) => {
      const idx = _depts.findIndex(d => d.id === body.id)
      if (idx === -1) return { code: 404, message: '部门不存在', data: null }
      _depts[idx] = { ..._depts[idx], ...body }
      return { code: 200, message: '更新成功', data: _depts[idx] }
    },
  },
  {
    url: '/api/system/dept/remove',
    method: 'delete',
    response: ({ query }) => {
      const id = Number(query.id)
      if (_depts.some(d => d.parentId === id)) return { code: 400, message: '存在子部门，不允许删除', data: null }
      const idx = _depts.findIndex(d => d.id === id)
      if (idx === -1) return { code: 404, message: '部门不存在', data: null }
      _depts.splice(idx, 1)
      return { code: 200, message: '删除成功', data: null }
    },
  },
]

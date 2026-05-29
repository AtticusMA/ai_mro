let _dictTypes = [
  { id: 1, dictName: '用户性别', dictType: 'sys_user_sex', status: 1, remark: '用户性别列表', createTime: '2024-01-01 08:00:00' },
  { id: 2, dictName: '菜单状态', dictType: 'sys_show_hide', status: 1, remark: '菜单状态列表', createTime: '2024-01-01 08:00:00' },
  { id: 3, dictName: '系统开关', dictType: 'sys_normal_disable', status: 1, remark: '系统开关列表', createTime: '2024-01-01 08:00:00' },
  { id: 4, dictName: '任务状态', dictType: 'sys_job_status', status: 1, remark: '任务状态列表', createTime: '2024-01-01 08:00:00' },
  { id: 5, dictName: '通知类型', dictType: 'sys_notice_type', status: 1, remark: '通知类型列表', createTime: '2024-01-01 08:00:00' },
]

let _dictItems = [
  { id: 1, dictTypeId: 1, dictType: 'sys_user_sex', label: '男', value: '1', sort: 1, status: 1, remark: '' },
  { id: 2, dictTypeId: 1, dictType: 'sys_user_sex', label: '女', value: '2', sort: 2, status: 1, remark: '' },
  { id: 3, dictTypeId: 1, dictType: 'sys_user_sex', label: '未知', value: '0', sort: 3, status: 1, remark: '' },
  { id: 4, dictTypeId: 2, dictType: 'sys_show_hide', label: '显示', value: '0', sort: 1, status: 1, remark: '' },
  { id: 5, dictTypeId: 2, dictType: 'sys_show_hide', label: '隐藏', value: '1', sort: 2, status: 1, remark: '' },
  { id: 6, dictTypeId: 3, dictType: 'sys_normal_disable', label: '正常', value: '0', sort: 1, status: 1, remark: '' },
  { id: 7, dictTypeId: 3, dictType: 'sys_normal_disable', label: '停用', value: '1', sort: 2, status: 1, remark: '' },
  { id: 8, dictTypeId: 4, dictType: 'sys_job_status', label: '正常', value: '0', sort: 1, status: 1, remark: '' },
  { id: 9, dictTypeId: 4, dictType: 'sys_job_status', label: '暂停', value: '1', sort: 2, status: 1, remark: '' },
  { id: 10, dictTypeId: 5, dictType: 'sys_notice_type', label: '通知', value: '1', sort: 1, status: 1, remark: '' },
  { id: 11, dictTypeId: 5, dictType: 'sys_notice_type', label: '公告', value: '2', sort: 2, status: 1, remark: '' },
]

let _typeNextId = 100
let _itemNextId = 100

export default [
  {
    url: '/api/system/dict-type/page',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, dictName = '', dictType = '', status } = query
      let list = _dictTypes.filter(t => {
        if (dictName && !t.dictName.includes(dictName)) return false
        if (dictType && !t.dictType.includes(dictType)) return false
        if (status !== undefined && status !== '' && t.status !== Number(status)) return false
        return true
      })
      const total = list.length
      const records = list.slice((page - 1) * pageSize, page * pageSize)
      return { code: 200, message: '获取成功', data: { records, total, page: Number(page), pageSize: Number(pageSize) } }
    },
  },
  {
    url: '/api/system/dict-type/detail',
    method: 'get',
    response: ({ query }) => {
      const t = _dictTypes.find(t => t.id === Number(query.id))
      return t ? { code: 200, message: '获取成功', data: t } : { code: 404, message: '字典类型不存在', data: null }
    },
  },
  {
    url: '/api/system/dict-type/create',
    method: 'post',
    response: ({ body }) => {
      const newType = { ...body, id: ++_typeNextId, createTime: new Date().toLocaleString() }
      _dictTypes.push(newType)
      return { code: 200, message: '创建成功', data: newType }
    },
  },
  {
    url: '/api/system/dict-type/update',
    method: 'put',
    response: ({ body }) => {
      const idx = _dictTypes.findIndex(t => t.id === body.id)
      if (idx === -1) return { code: 404, message: '字典类型不存在', data: null }
      _dictTypes[idx] = { ..._dictTypes[idx], ...body }
      return { code: 200, message: '更新成功', data: _dictTypes[idx] }
    },
  },
  {
    url: '/api/system/dict-type/remove',
    method: 'delete',
    response: ({ query }) => {
      const id = Number(query.id)
      const idx = _dictTypes.findIndex(t => t.id === id)
      if (idx === -1) return { code: 404, message: '字典类型不存在', data: null }
      _dictTypes.splice(idx, 1)
      _dictItems = _dictItems.filter(i => i.dictTypeId !== id)
      return { code: 200, message: '删除成功', data: null }
    },
  },
  {
    url: '/api/system/dict-item/list',
    method: 'get',
    response: ({ query }) => {
      const { dictTypeId, dictType } = query
      const list = _dictItems.filter(i => {
        if (dictTypeId) return i.dictTypeId === Number(dictTypeId)
        if (dictType) return i.dictType === dictType
        return true
      })
      return { code: 200, message: '获取成功', data: list }
    },
  },
  {
    url: '/api/system/dict-item/create',
    method: 'post',
    response: ({ body }) => {
      const newItem = { ...body, id: ++_itemNextId }
      _dictItems.push(newItem)
      return { code: 200, message: '创建成功', data: newItem }
    },
  },
  {
    url: '/api/system/dict-item/update',
    method: 'put',
    response: ({ body }) => {
      const idx = _dictItems.findIndex(i => i.id === body.id)
      if (idx === -1) return { code: 404, message: '字典数据不存在', data: null }
      _dictItems[idx] = { ..._dictItems[idx], ...body }
      return { code: 200, message: '更新成功', data: _dictItems[idx] }
    },
  },
  {
    url: '/api/system/dict-item/remove',
    method: 'delete',
    response: ({ query }) => {
      const id = Number(query.id)
      const idx = _dictItems.findIndex(i => i.id === id)
      if (idx === -1) return { code: 404, message: '字典数据不存在', data: null }
      _dictItems.splice(idx, 1)
      return { code: 200, message: '删除成功', data: null }
    },
  },
]

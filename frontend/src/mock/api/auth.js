export default [
  {
    url: '/api/auth/login',
    method: 'post',
    response: ({ body }) => {
      const { username, password } = body
      const users = {
        admin:   { password: 'admin123',   realName: '管理员',   roles: ['admin'],   permissions: ['*:*:*'] },
        manager: { password: 'manager123', realName: '部门经理', roles: ['manager'], permissions: ['system:user:list','system:dept:list','system:role:list','system:menu:list','system:dict:list'] },
        user:    { password: 'user123',    realName: '普通员工', roles: ['user'],     permissions: ['system:user:list','system:dept:list'] },
      }
      const u = users[username]
      if (u && u.password === password) {
        return {
          code: 200,
          message: '登录成功',
          data: {
            token: 'mock-token-' + Date.now(),
            refreshToken: 'mock-refresh-' + Date.now(),
            user: {
              id: 1,
              username,
              realName: u.realName,
              avatar: '',
              dept: { id: 1, name: 'A集团' },
              roles: u.roles,
              permissions: u.permissions,
            },
          },
        }
      }
      return { code: 401, message: '用户名或密码错误', data: null }
    },
  },
  {
    url: '/api/auth/user-info',
    method: 'get',
    response: () => ({
      code: 200,
      message: '获取成功',
      data: {
        id: 1,
        username: 'admin',
        realName: '管理员',
        avatar: '',
        dept: { id: 1, name: 'A集团' },
        roles: ['admin'],
        permissions: ['*:*:*'],
      },
    }),
  },
  {
    url: '/api/auth/logout',
    method: 'post',
    response: () => ({ code: 200, message: '登出成功', data: null }),
  },
  {
    url: '/api/auth/refresh-token',
    method: 'post',
    response: () => ({
      code: 200,
      message: '刷新成功',
      data: { token: 'mock-token-' + Date.now(), refreshToken: 'mock-refresh-' + Date.now() },
    }),
  },
]

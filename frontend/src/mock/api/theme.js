const THEMES = {
  internet:      { themeCode: 'internet',      primaryColor: '#667eea', sidebarBg: '#1d1f2b', sidebarTextColor: '#c8c9cc' },
  finance:       { themeCode: 'finance',       primaryColor: '#1a3a6b', sidebarBg: '#0d1f3c', sidebarTextColor: '#a0aec0' },
  medical:       { themeCode: 'medical',       primaryColor: '#00a896', sidebarBg: '#f0fafa', sidebarTextColor: '#2c3e50' },
  education:     { themeCode: 'education',     primaryColor: '#f6851b', sidebarBg: '#1a2744', sidebarTextColor: '#c8c9cc' },
  manufacturing: { themeCode: 'manufacturing', primaryColor: '#e87722', sidebarBg: '#2b2d30', sidebarTextColor: '#c8c9cc' },
  power:         { themeCode: 'power',         primaryColor: '#f5a623', sidebarBg: '#1a2332', sidebarTextColor: '#c8c9cc' },
  aerospace:     { themeCode: 'aerospace',     primaryColor: '#0066cc', sidebarBg: '#0a0e1a', sidebarTextColor: '#8899aa' },
}

let _activeCode = 'internet'

export default [
  {
    url: '/api/system/theme',
    method: 'get',
    response: () => ({
      code: 200,
      message: '获取成功',
      data: THEMES[_activeCode] || THEMES.internet,
    }),
  },
  {
    url: '/api/system/theme',
    method: 'put',
    response: ({ body }) => {
      const code = body?.theme_code
      if (!THEMES[code]) return { code: 400, message: '无效的主题标识', data: null }
      _activeCode = code
      return { code: 200, message: '更新成功', data: THEMES[_activeCode] }
    },
  },
]

import request from '@/utils/request'

export const getTheme = () => request.get('/api/system/theme')

export const updateTheme = (themeCode) => request.put('/api/system/theme', { theme_code: themeCode })

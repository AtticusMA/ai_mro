// Element Plus primary 色衍生变量名列表
const EP_LIGHT_VARS = [1, 2, 3, 4, 5, 6, 7, 8, 9]

/**
 * 将十六进制颜色与白色混合，生成 light 衍生色
 * level: 1~9，level=1 最深（90% 原色），level=9 最浅（10% 原色）
 */
function mixWithWhite(hex, level) {
  const ratio = level / 10
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  const mr = Math.round(r + (255 - r) * ratio)
  const mg = Math.round(g + (255 - g) * ratio)
  const mb = Math.round(b + (255 - b) * ratio)
  return `rgb(${mr}, ${mg}, ${mb})`
}

/**
 * 将十六进制颜色与黑色混合，生成 dark 衍生色
 * level: 2 → 80% 原色 + 20% 黑
 */
function mixWithBlack(hex, level) {
  const ratio = level / 10
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  const mr = Math.round(r * (1 - ratio))
  const mg = Math.round(g * (1 - ratio))
  const mb = Math.round(b * (1 - ratio))
  return `rgb(${mr}, ${mg}, ${mb})`
}

/**
 * 将主题数据应用到 :root CSS 变量
 * @param {{ primaryColor: string, sidebarBg: string, sidebarTextColor: string }} themeData
 */
export function applyTheme(themeData) {
  const { primaryColor, sidebarBg, sidebarTextColor } = themeData
  const root = document.documentElement

  root.style.setProperty('--el-color-primary', primaryColor)
  EP_LIGHT_VARS.forEach(level => {
    root.style.setProperty(`--el-color-primary-light-${level}`, mixWithWhite(primaryColor, level))
  })
  root.style.setProperty('--el-color-primary-dark-2', mixWithBlack(primaryColor, 2))

  root.style.setProperty('--sidebar-bg', sidebarBg)
  root.style.setProperty('--sidebar-text-color', sidebarTextColor)
}

/**
 * 默认互联网主题（兜底用）
 */
export const DEFAULT_THEME = {
  themeCode: 'internet',
  primaryColor: '#667eea',
  sidebarBg: '#1d1f2b',
  sidebarTextColor: '#c8c9cc',
}

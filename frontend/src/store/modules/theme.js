import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getTheme, updateTheme } from '@/api/theme'
import { applyTheme, DEFAULT_THEME } from '@/utils/theme'

export const useThemeStore = defineStore('theme', () => {
  const themeCode = ref(DEFAULT_THEME.themeCode)
  const primaryColor = ref(DEFAULT_THEME.primaryColor)
  const sidebarBg = ref(DEFAULT_THEME.sidebarBg)
  const sidebarTextColor = ref(DEFAULT_THEME.sidebarTextColor)

  const _apply = (data) => {
    themeCode.value = data.themeCode
    primaryColor.value = data.primaryColor
    sidebarBg.value = data.sidebarBg
    sidebarTextColor.value = data.sidebarTextColor
    applyTheme(data)
  }

  const loadTheme = async () => {
    try {
      const res = await getTheme()
      _apply(res.data)
    } catch {
      applyTheme(DEFAULT_THEME)
    }
  }

  const setTheme = async (code) => {
    await updateTheme(code)
    const res = await getTheme()
    _apply(res.data)
  }

  return { themeCode, primaryColor, sidebarBg, sidebarTextColor, loadTheme, setTheme }
})

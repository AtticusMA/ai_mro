<template>
  <div class="theme-config">
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span class="title">前端主题配置</span>
          <span class="subtitle">选择符合行业特色的界面风格，保存后即时生效</span>
        </div>
      </template>

      <div class="theme-grid">
        <div
          v-for="theme in themes"
          :key="theme.code"
          class="theme-card"
          :class="{ active: selectedCode === theme.code }"
          @click="selectedCode = theme.code"
        >
          <!-- 色块预览 -->
          <div class="theme-preview">
            <div class="preview-sidebar" :style="{ background: theme.sidebarBg }">
              <div class="preview-menu-item" :style="{ background: theme.primaryColor, opacity: 0.9 }"></div>
              <div class="preview-menu-item" :style="{ background: theme.sidebarTextColor, opacity: 0.3 }"></div>
              <div class="preview-menu-item" :style="{ background: theme.sidebarTextColor, opacity: 0.3 }"></div>
            </div>
            <div class="preview-content">
              <div class="preview-topbar" :style="{ background: theme.primaryColor }"></div>
              <div class="preview-body">
                <div class="preview-block" :style="{ background: theme.primaryColor, opacity: 0.15 }"></div>
                <div class="preview-btn" :style="{ background: theme.primaryColor }"></div>
              </div>
            </div>
          </div>

          <!-- 主题名称 -->
          <div class="theme-info">
            <div class="theme-name">{{ theme.name }}</div>
            <div class="theme-color">
              <span class="color-dot" :style="{ background: theme.primaryColor }"></span>
              <span class="color-value">{{ theme.primaryColor }}</span>
            </div>
          </div>

          <!-- 选中标记 -->
          <div v-if="selectedCode === theme.code" class="active-badge">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button
          type="primary"
          :loading="saving"
          v-permission="['system:theme:edit']"
          @click="handleSave"
        >
          保存配置
        </el-button>
        <span class="current-tip">当前主题：{{ currentThemeName }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { useThemeStore } from '@/store/modules/theme'

const themeStore = useThemeStore()

const themes = [
  { code: 'internet',      name: '互联网',  primaryColor: '#667eea', sidebarBg: '#1d1f2b', sidebarTextColor: '#c8c9cc' },
  { code: 'finance',       name: '金融',    primaryColor: '#1a3a6b', sidebarBg: '#0d1f3c', sidebarTextColor: '#a0aec0' },
  { code: 'medical',       name: '医疗',    primaryColor: '#00a896', sidebarBg: '#f0fafa', sidebarTextColor: '#2c3e50' },
  { code: 'education',     name: '教育',    primaryColor: '#f6851b', sidebarBg: '#1a2744', sidebarTextColor: '#c8c9cc' },
  { code: 'manufacturing', name: '制造业',  primaryColor: '#e87722', sidebarBg: '#2b2d30', sidebarTextColor: '#c8c9cc' },
  { code: 'power',         name: '电力',    primaryColor: '#f5a623', sidebarBg: '#1a2332', sidebarTextColor: '#c8c9cc' },
  { code: 'aerospace',     name: '航天',    primaryColor: '#0066cc', sidebarBg: '#0a0e1a', sidebarTextColor: '#8899aa' },
]

const selectedCode = ref('internet')
const saving = ref(false)

const currentThemeName = computed(
  () => themes.find(t => t.code === themeStore.themeCode)?.name || '互联网'
)

const handleSave = async () => {
  saving.value = true
  try {
    await themeStore.setTheme(selectedCode.value)
    ElMessage.success('主题配置已保存并生效')
  } catch {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  selectedCode.value = themeStore.themeCode
})
</script>

<style scoped>
.theme-config {
  padding: 0;
}

.config-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.config-card :deep(.el-card__header) {
  border-bottom: 1px solid #e4e7eb;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 4px 0 24px;
}

.theme-card {
  position: relative;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
}

.theme-card:hover {
  border-color: var(--el-color-primary, #667eea);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.theme-card.active {
  border-color: var(--el-color-primary, #667eea);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25);
}

/* 主题预览区域 */
.theme-preview {
  display: flex;
  height: 100px;
}

.preview-sidebar {
  width: 36px;
  padding: 6px 5px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-menu-item {
  height: 8px;
  border-radius: 2px;
}

.preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.preview-topbar {
  height: 16px;
  opacity: 0.9;
}

.preview-body {
  flex: 1;
  background: #f5f7fa;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-block {
  height: 28px;
  border-radius: 3px;
}

.preview-btn {
  height: 14px;
  width: 48px;
  border-radius: 3px;
  opacity: 0.85;
}

/* 主题信息 */
.theme-info {
  padding: 10px 12px;
  background: #fff;
}

.theme-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.theme-color {
  display: flex;
  align-items: center;
  gap: 6px;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.color-value {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

/* 选中角标 */
.active-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--el-color-primary, #667eea);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
}

/* 操作区 */
.actions {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.current-tip {
  font-size: 13px;
  color: #909399;
}
</style>

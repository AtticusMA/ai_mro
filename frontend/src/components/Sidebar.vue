<template>
  <div class="sidebar" :class="{ collapsed: isCollapsed }" :style="{ background: themeStore.sidebarBg }">
    <!-- 菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapsed"
      class="sidebar-menu"
      :background-color="themeStore.sidebarBg"
      :text-color="themeStore.sidebarTextColor"
      :active-text-color="themeStore.primaryColor"
      router
      @select="handleMenuSelect"
    >
      <!-- 菜单项 -->
      <template v-for="menu in menus" :key="menu.id">
        <!-- 目录菜单 -->
        <el-sub-menu v-if="menu.type === 'M'" :index="menu.id.toString()">
          <template #title>
            <el-icon v-if="menu.icon" :class="`icon-${menu.icon}`">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </template>

          <!-- 子菜单 -->
          <template v-for="child in menu.children" :key="child.id">
            <!-- 子菜单项 -->
            <el-menu-item
              v-if="child.type === 'C'"
              :index="child.path"
              :route="{ path: child.path }"
            >
              <el-icon v-if="child.icon" :class="`icon-${child.icon}`">
                <component :is="child.icon" />
              </el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>

            <!-- 子目录 -->
            <el-sub-menu v-else-if="child.type === 'M'" :index="child.id.toString()">
              <template #title>
                <el-icon v-if="child.icon" :class="`icon-${child.icon}`">
                  <component :is="child.icon" />
                </el-icon>
                <span>{{ child.name }}</span>
              </template>

              <!-- 三级菜单 -->
              <el-menu-item
                v-for="grandchild in child.children"
                :key="grandchild.id"
                :index="grandchild.path"
                :route="{ path: grandchild.path }"
              >
                <el-icon v-if="grandchild.icon" :class="`icon-${grandchild.icon}`">
                  <component :is="grandchild.icon" />
                </el-icon>
                <span>{{ grandchild.name }}</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-sub-menu>

        <!-- 菜单项 -->
        <el-menu-item
          v-else-if="menu.type === 'C'"
          :index="menu.path"
          :route="{ path: menu.path }"
        >
          <el-icon v-if="menu.icon" :class="`icon-${menu.icon}`">
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.name }}</span>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- 收缩按钮 - 右侧边缘 -->
    <div class="collapse-toggle" @click="toggleCollapse">
      <el-icon>
        <component :is="isCollapsed ? 'Expand' : 'Fold'" />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import { useThemeStore } from '@/store/modules/theme'
import { getUserMenus } from '@/api/menu'

const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const isCollapsed = ref(false)
const menus = ref([])

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleMenuSelect = () => {}

const loadMenus = async () => {
  try {
    const res = await getUserMenus()
    menus.value = res.data.menus
    // 将服务端返回的权限码写入 store，使 v-permission 指令生效
    if (authStore.user && res.data.permissions) {
      authStore.user.permissions = res.data.permissions
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.sidebar {
  width: 250px;
  height: calc(100vh - 60px);
  background: var(--sidebar-bg, #f5f7fa);
  border-right: 1px solid #e4e7eb;
  overflow: visible;
  transition: width 0.3s;
  position: relative;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-menu {
  border: none;
  background: transparent;
  height: 100%;
  overflow-y: auto;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: var(--sidebar-text-color, #606266);
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--el-color-primary-light-9, #e6f7ff) !important;
  color: var(--el-color-primary, #667eea) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-9, #e6f7ff) !important;
  color: var(--el-color-primary, #667eea) !important;
  border-right: 3px solid var(--el-color-primary, #667eea);
}

.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--el-color-primary, #667eea) !important;
}

.sidebar-menu :deep(.el-icon) {
  margin-right: 8px;
}

.collapse-toggle {
  position: absolute;
  top: 50%;
  right: -12px;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--sidebar-text-color, #909399);
  background: #fff;
  border: 1px solid #e4e7eb;
  border-radius: 0 6px 6px 0;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
  transition: all 0.2s;
}

.collapse-toggle:hover {
  color: var(--el-color-primary, #667eea);
  border-color: var(--el-color-primary, #667eea);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 60px;
    z-index: 999;
    height: calc(100vh - 60px);
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
  }

  .sidebar.collapsed {
    width: 0;
    overflow: hidden;
  }
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>

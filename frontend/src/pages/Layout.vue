<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <Navbar />

    <div class="layout-container">
      <!-- 左侧菜单栏 -->
      <Sidebar />

      <!-- 主内容区域 -->
      <div class="layout-content">
        <!-- 面包屑导航 -->
        <div class="breadcrumb-container">
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="{ path: item.path }"
            >
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 页面内容 -->
        <div class="page-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" :key="$route.path" />
            </transition>
          </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Sidebar from '@/components/Sidebar.vue'
import { useThemeStore } from '@/store/modules/theme'

const route = useRoute()
const themeStore = useThemeStore()

onMounted(() => {
  themeStore.loadTheme()
})

// 计算面包屑
const breadcrumbs = computed(() => {
  const breadcrumbs = []
  const pathArray = route.path.split('/').filter(p => p)

  let currentPath = ''
  for (const path of pathArray) {
    currentPath += '/' + path
    breadcrumbs.push({
      path: currentPath,
      name: route.meta?.breadcrumb || path
    })
  }

  return breadcrumbs
})
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.layout-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.layout-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.breadcrumb-container {
  padding: 15px 20px;
  background: white;
  border-bottom: 1px solid #e4e7eb;
}

.page-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 滚动条样式 */
.page-content::-webkit-scrollbar {
  width: 8px;
}

.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 4px;
}

.page-content::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .breadcrumb-container {
    padding: 10px 15px;
  }

  .page-content {
    padding: 15px;
  }
}
</style>

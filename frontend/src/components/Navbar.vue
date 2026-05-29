<template>
  <div class="navbar">
    <!-- 左侧：Logo 和标题 -->
    <div class="navbar-left">
      <div class="navbar-logo">
        <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
        <span class="logo-text">智慧机务</span>
      </div>
    </div>

    <!-- 右侧：用户菜单 -->
    <div class="navbar-right">
      <!-- 用户信息 -->
      <div class="user-info">
        <el-avatar :src="userAvatar" :size="40" />
        <div class="user-details">
          <div class="user-name">{{ userName }}</div>
          <div class="user-role">{{ userRole }}</div>
        </div>
      </div>

      <!-- 用户菜单 -->
      <el-dropdown @command="handleCommand">
        <el-icon class="dropdown-icon">
          <ArrowDown />
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人资料
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item command="change-password">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-dropdown-item>
            <el-divider />
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              登出
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const userAvatar = computed(() => authStore.user?.avatar || 'https://via.placeholder.com/40')
const userName = computed(() => authStore.user?.realName || '用户')
const userRole = computed(() => {
  const roles = authStore.user?.roles || []
  const roleMap = {
    admin: '超级管理员',
    manager: '部门经理',
    user: '普通员工'
  }
  return roles.map(role => roleMap[role] || role).join('、')
})

/**
 * 处理菜单命令
 */
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      router.push({ name: 'Profile' })
      break
    case 'settings':
      // 跳转到系统设置页面
      router.push({ name: 'Settings' })
      break
    case 'change-password':
      // 打开修改密码对话框
      ElMessage.info('修改密码功能开发中...')
      break
    case 'logout':
      // 确认登出
      try {
        await ElMessageBox.confirm('确定要登出吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await authStore.logout()
        ElMessage.success('登出成功')
        router.push({ name: 'Login' })
      } catch (error) {
        // 用户取消登出
      }
      break
  }
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background: white;
  border-bottom: 1px solid #e4e7eb;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

.dropdown-icon {
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  transition: color 0.3s;
}

.dropdown-icon:hover {
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 10px;
  }

  .logo-text {
    display: none;
  }

  .user-details {
    display: none;
  }

  .navbar-right {
    gap: 10px;
  }
}
</style>

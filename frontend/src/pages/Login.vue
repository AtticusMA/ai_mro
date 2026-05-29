<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo 和标题 -->
      <div class="login-header">
        <h1 class="login-title">智慧机务</h1>
        <p class="login-subtitle">智能办公助手</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            prefix-icon="Lock"
            clearable
            @keyup.enter="handleLogin"
          >
            <template #suffix>
              <el-icon
                class="password-toggle"
                @click="showPassword = !showPassword"
              >
                <component :is="showPassword ? 'Hide' : 'View'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 记住密码 -->
        <div class="login-options">
          <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
          <el-link type="primary" href="#">忘记密码？</el-link>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 测试账号提示 -->
      <div class="test-accounts">
        <p class="test-title">测试账号：</p>
        <div class="account-list">
          <div class="account-item">
            <span class="account-label">超级管理员：</span>
            <span class="account-value">admin / admin123</span>
          </div>
          <div class="account-item">
            <span class="account-label">部门经理：</span>
            <span class="account-value">manager / manager123</span>
          </div>
          <div class="account-item">
            <span class="account-label">普通员工：</span>
            <span class="account-value">user / user123</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="login-background">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { useThemeStore } from '@/store/modules/theme'
import { validateUsername, validatePassword } from '@/utils/validate'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单引用
const formRef = ref(null)

// 状态
const loading = ref(false)
const showPassword = ref(false)
const rememberPassword = ref(false)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  // 表单验证
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  loading.value = true

  try {
    // 调用登录接口
    await authStore.login({
      username: loginForm.username,
      password: loginForm.password
    })

    // 保存记住密码
    if (rememberPassword.value) {
      localStorage.setItem('rememberPassword', JSON.stringify({
        username: loginForm.username,
        password: loginForm.password
      }))
    } else {
      localStorage.removeItem('rememberPassword')
    }

    ElMessage.success('登录成功')

    // 跳转到首页（replace 替换历史记录，避免回退到登录页）
    await router.replace({ name: 'Dashboard' })
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时恢复记住的密码
 */
onMounted(() => {
  themeStore.loadTheme()
  const saved = localStorage.getItem('rememberPassword')
  if (saved) {
    try {
      const { username, password } = JSON.parse(saved)
      loginForm.username = username
      loginForm.password = password
      rememberPassword.value = true
    } catch (error) {
      console.error('恢复记住的密码失败:', error)
    }
  }
})
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.login-background {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.bg-circle-1 {
  width: 300px;
  height: 300px;
  background: white;
  top: -100px;
  right: -100px;
}

.bg-circle-2 {
  width: 200px;
  height: 200px;
  background: white;
  bottom: -50px;
  left: -50px;
}

.bg-circle-3 {
  width: 150px;
  height: 150px;
  background: white;
  top: 50%;
  left: 10%;
}

.login-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
}

.password-toggle {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
}

.password-toggle:hover {
  color: #667eea;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.login-button:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

.test-accounts {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.test-title {
  margin: 0 0 10px 0;
  color: #333;
  font-weight: bold;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  color: #666;
}

.account-label {
  font-weight: 500;
}

.account-value {
  color: #909399;
  font-family: monospace;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    max-width: 90%;
    padding: 30px 20px;
  }

  .login-title {
    font-size: 24px;
  }

  .bg-circle-1 {
    width: 200px;
    height: 200px;
  }

  .bg-circle-2 {
    width: 150px;
    height: 150px;
  }
}
</style>

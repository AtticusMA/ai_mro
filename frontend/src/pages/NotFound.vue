<template>
  <div class="not-found">
    <div class="not-found-container">
      <!-- 404 图标 -->
      <div class="not-found-icon">
        <span class="icon-404">404</span>
      </div>

      <!-- 错误信息 -->
      <div class="not-found-content">
        <h1 class="not-found-title">页面不存在</h1>
        <p class="not-found-description">
          抱歉，您访问的页面不存在或已被删除
        </p>

        <!-- 建议链接 -->
        <div class="not-found-suggestions">
          <p class="suggestions-title">您可以尝试：</p>
          <ul class="suggestions-list">
            <li>
              <el-link type="primary" @click="goHome">返回首页</el-link>
            </li>
            <li>
              <el-link type="primary" @click="goBack">返回上一页</el-link>
            </li>
            <li>
              <el-link type="primary" @click="goLogin">返回登录</el-link>
            </li>
          </ul>
        </div>

        <!-- 操作按钮 -->
        <div class="not-found-actions">
          <el-button type="primary" @click="goHome">返回首页</el-button>
          <el-button @click="goBack">返回上一页</el-button>
        </div>
      </div>

      <!-- 背景装饰 -->
      <div class="not-found-background">
        <div class="bg-circle bg-circle-1"></div>
        <div class="bg-circle bg-circle-2"></div>
        <div class="bg-circle bg-circle-3"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * 返回首页
 */
const goHome = () => {
  router.push({ path: '/' })
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 返回登录页
 */
const goLogin = () => {
  router.push({ path: '/login' })
}
</script>

<style scoped>
.not-found {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.not-found-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.not-found-background {
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

.not-found-icon {
  position: relative;
  z-index: 1;
  margin-right: 60px;
}

.icon-404 {
  font-size: 200px;
  font-weight: bold;
  color: white;
  opacity: 0.8;
  display: block;
  line-height: 1;
  text-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.not-found-content {
  position: relative;
  z-index: 1;
  max-width: 400px;
  color: white;
}

.not-found-title {
  font-size: 32px;
  font-weight: bold;
  margin: 0 0 15px 0;
}

.not-found-description {
  font-size: 16px;
  margin: 0 0 30px 0;
  opacity: 0.9;
}

.not-found-suggestions {
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.suggestions-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 500;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestions-list li {
  font-size: 14px;
}

.not-found-actions {
  display: flex;
  gap: 10px;
}

.not-found-actions :deep(.el-button) {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-container {
    flex-direction: column;
    padding: 20px;
  }

  .not-found-icon {
    margin-right: 0;
    margin-bottom: 30px;
  }

  .icon-404 {
    font-size: 120px;
  }

  .not-found-content {
    max-width: 100%;
    text-align: center;
  }

  .not-found-title {
    font-size: 24px;
  }

  .not-found-description {
    font-size: 14px;
  }

  .not-found-actions {
    flex-direction: column;
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

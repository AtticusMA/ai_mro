<template>
  <transition-group name="message" tag="div" class="message-container">
    <div
      v-for="message in messages"
      :key="message.id"
      :class="['message', `message-${message.type}`]"
    >
      <div class="message-content">
        <el-icon class="message-icon">
          <component :is="getIcon(message.type)" />
        </el-icon>
        <span class="message-text">{{ message.text }}</span>
      </div>
      <el-icon class="message-close" @click="removeMessage(message.id)">
        <Close />
      </el-icon>
    </div>
  </transition-group>
</template>

<script setup>
import { ref } from 'vue'
import { Close, SuccessFilled, InfoFilled, WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'

const messages = ref([])
let messageId = 0

/**
 * 获取消息图标
 */
const getIcon = (type) => {
  const iconMap = {
    success: 'SuccessFilled',
    info: 'InfoFilled',
    warning: 'WarningFilled',
    error: 'CircleCloseFilled'
  }
  return iconMap[type] || 'InfoFilled'
}

/**
 * 显示消息
 */
const showMessage = (text, type = 'info', duration = 3000) => {
  const id = messageId++
  const message = { id, text, type }

  messages.value.push(message)

  // 自动移除消息
  if (duration > 0) {
    setTimeout(() => {
      removeMessage(id)
    }, duration)
  }

  return id
}

/**
 * 移除消息
 */
const removeMessage = (id) => {
  const index = messages.value.findIndex(m => m.id === id)
  if (index > -1) {
    messages.value.splice(index, 1)
  }
}

/**
 * 清除所有消息
 */
const clearMessages = () => {
  messages.value = []
}

/**
 * 快捷方法
 */
const success = (text, duration) => showMessage(text, 'success', duration)
const info = (text, duration) => showMessage(text, 'info', duration)
const warning = (text, duration) => showMessage(text, 'warning', duration)
const error = (text, duration) => showMessage(text, 'error', duration)

defineExpose({
  showMessage,
  removeMessage,
  clearMessages,
  success,
  info,
  warning,
  error
})
</script>

<style scoped>
.message-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9998;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  min-width: 300px;
  max-width: 400px;
  animation: slideIn 0.3s ease;
}

.message-content {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.message-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.message-text {
  font-size: 14px;
  color: #333;
}

.message-close {
  font-size: 16px;
  color: #909399;
  cursor: pointer;
  margin-left: 10px;
  flex-shrink: 0;
  transition: color 0.3s;
}

.message-close:hover {
  color: #333;
}

/* 消息类型样式 */
.message-success {
  border-left: 4px solid #67c23a;
}

.message-success .message-icon {
  color: #67c23a;
}

.message-info {
  border-left: 4px solid #909399;
}

.message-info .message-icon {
  color: #909399;
}

.message-warning {
  border-left: 4px solid #e6a23c;
}

.message-warning .message-icon {
  color: #e6a23c;
}

.message-error {
  border-left: 4px solid #f56c6c;
}

.message-error .message-icon {
  color: #f56c6c;
}

/* 动画 */
@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.message-enter-active,
.message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from {
  transform: translateX(400px);
  opacity: 0;
}

.message-leave-to {
  transform: translateX(400px);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-container {
    left: 10px;
    right: 10px;
  }

  .message {
    min-width: auto;
    max-width: none;
  }
}
</style>

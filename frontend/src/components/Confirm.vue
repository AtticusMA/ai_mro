<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <!-- 对话框内容 -->
    <div class="confirm-content">
      <el-icon class="confirm-icon" :class="`icon-${type}`">
        <component :is="getIcon(type)" />
      </el-icon>
      <div class="confirm-message">{{ message }}</div>
    </div>

    <!-- 对话框底部按钮 -->
    <template #footer>
      <div class="confirm-footer">
        <el-button @click="handleCancel">{{ cancelText }}</el-button>
        <el-button
          :type="type === 'danger' ? 'danger' : 'primary'"
          :loading="loading"
          @click="handleConfirm"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { WarningFilled, InfoFilled, SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'

const visible = ref(false)
const loading = ref(false)

const props = defineProps({
  title: {
    type: String,
    default: '提示'
  },
  message: {
    type: String,
    default: '确定要执行此操作吗？'
  },
  type: {
    type: String,
    default: 'warning',
    validator: (value) => ['warning', 'info', 'success', 'danger'].includes(value)
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  width: {
    type: String,
    default: '400px'
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

/**
 * 获取图标
 */
const getIcon = (type) => {
  const iconMap = {
    warning: 'WarningFilled',
    info: 'InfoFilled',
    success: 'SuccessFilled',
    danger: 'CircleCloseFilled'
  }
  return iconMap[type] || 'InfoFilled'
}

/**
 * 打开对话框
 */
const open = () => {
  visible.value = true
}

/**
 * 关闭对话框
 */
const close = () => {
  visible.value = false
}

/**
 * 处理确认
 */
const handleConfirm = async () => {
  loading.value = true
  try {
    await emit('confirm')
    close()
  } finally {
    loading.value = false
  }
}

/**
 * 处理取消
 */
const handleCancel = () => {
  emit('cancel')
  close()
}

/**
 * 处理关闭
 */
const handleClose = () => {
  emit('close')
}

defineExpose({
  open,
  close,
  visible
})
</script>

<style scoped>
.confirm-content {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 10px 0;
}

.confirm-icon {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 2px;
}

.icon-warning {
  color: #e6a23c;
}

.icon-info {
  color: #909399;
}

.icon-success {
  color: #67c23a;
}

.icon-danger {
  color: #f56c6c;
}

.confirm-message {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  word-break: break-word;
}

.confirm-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

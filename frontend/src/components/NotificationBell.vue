<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" @show="handleShow">
    <template #reference>
      <div class="notification-bell">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
      </div>
    </template>
    <div class="notification-panel">
      <div class="panel-header">
        <span class="panel-title">通知</span>
        <el-button type="primary" link size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">全部已读</el-button>
      </div>
      <div class="panel-list">
        <div v-if="recentList.length === 0" class="empty-tip">暂无通知</div>
        <div
          v-for="item in recentList"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.is_read }"
          @click="handleItemClick(item)"
        >
          <div class="item-dot" :class="typeClass(item.type)"></div>
          <div class="item-content">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-time">{{ item.created_at }}</div>
          </div>
        </div>
      </div>
      <div class="panel-footer">
        <el-button type="primary" link @click="goToCenter">查看全部</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/store/modules/notification'

const router = useRouter()
const store = useNotificationStore()

const unreadCount = computed(() => store.unreadCount)
const recentList = computed(() => store.recentList)

const typeClass = (type) => ({
  'dot-health': type === 'health_alert',
  'dot-workcard': type === 'workcard_overdue',
  'dot-license': type === 'license_expiring',
  'dot-training': type === 'training_assignment'
})

const handleShow = () => {
  store.fetchRecentList()
}

const handleItemClick = (item) => {
  if (!item.is_read) {
    store.markRead(item.id)
  }
}

const handleMarkAllRead = () => {
  store.markAllRead()
}

const goToCenter = () => {
  router.push('/notifications')
}
</script>

<style scoped>
.notification-bell {
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.notification-bell:hover {
  color: var(--el-color-primary);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
}

.panel-list {
  max-height: 320px;
  overflow-y: auto;
  padding: 8px 0;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px 0;
  font-size: 13px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #ecf5ff;
}

.item-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  margin-right: 10px;
  flex-shrink: 0;
}

.dot-health { background: #f56c6c; }
.dot-workcard { background: #e6a23c; }
.dot-license { background: #409eff; }
.dot-training { background: #67c23a; }

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.panel-footer {
  text-align: center;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}
</style>

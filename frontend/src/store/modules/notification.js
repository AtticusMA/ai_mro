import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount, getNotifications, markNotificationRead, markAllNotificationsRead } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const recentList = ref([])
  let timer = null

  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCount()
      if (res.code === 200) {
        unreadCount.value = res.data.count
      }
    } catch (e) {
      console.error('获取未读数失败:', e)
    }
  }

  const fetchRecentList = async () => {
    try {
      const res = await getNotifications({ page: 1, pageSize: 5 })
      if (res.code === 200) {
        recentList.value = res.data.list
      }
    } catch (e) {
      console.error('获取最近通知失败:', e)
    }
  }

  const markRead = async (id) => {
    try {
      const res = await markNotificationRead(id)
      if (res.code === 200) {
        const item = recentList.value.find(n => n.id === id)
        if (item) item.is_read = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    } catch (e) {
      console.error('标记已读失败:', e)
    }
  }

  const markAllRead = async () => {
    try {
      const res = await markAllNotificationsRead()
      if (res.code === 200) {
        recentList.value.forEach(n => { n.is_read = true })
        unreadCount.value = 0
      }
    } catch (e) {
      console.error('全部标记已读失败:', e)
    }
  }

  const startPolling = () => {
    fetchUnreadCount()
    fetchRecentList()
    timer = setInterval(() => {
      fetchUnreadCount()
    }, 60000)
  }

  const stopPolling = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  return {
    unreadCount,
    recentList,
    fetchUnreadCount,
    fetchRecentList,
    markRead,
    markAllRead,
    startPolling,
    stopPolling
  }
})

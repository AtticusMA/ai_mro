import request from '@/utils/request'

export const getNotifications = (params) => {
  return request.get('/api/notifications', { params })
}

export const getUnreadCount = () => {
  return request.get('/api/notifications/unread-count')
}

export const markNotificationRead = (id) => {
  return request.put(`/api/notifications/${id}/read`)
}

export const markAllNotificationsRead = () => {
  return request.put('/api/notifications/read-all')
}

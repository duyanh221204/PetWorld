import apiClient from './axios'

export const notificationApi = {
    getUnreadCount() {
        return apiClient.get('/notifications/unread-count')
    },

    getNotifications(page = 0, size = 50) {
        return apiClient.get('/notifications', {
            params: { page, size }
        })
    },

    markAllAsRead() {
        return apiClient.put('/notifications/mark-all-as-read')
    },

    markAsRead(notificationId) {
        return apiClient.put(`/notifications/${notificationId}/mark-as-read`)
    }
}

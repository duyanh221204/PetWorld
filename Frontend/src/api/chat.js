import apiClient from './axios'

export const chatApi = {
    getUnreadCount() {
        return apiClient.get('/chats/unread-count')
    },

    getAllChats(page = 0, size = 50) {
        return apiClient.get('/chats', {
            params: { page, size }
        })
    },

    markAsRead(chatId) {
        return apiClient.put(`/chats/${chatId}/read`)
    }
}

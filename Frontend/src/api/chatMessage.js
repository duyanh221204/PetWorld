import apiClient from './axios'

export const chatMessageApi = {
    getMessages(chatId, page = 0, size = 50) {
        return apiClient.get('/messages', {
            params: { chatId, page, size }
        })
    }
}

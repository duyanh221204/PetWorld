import apiClient from './axios'

export const userApi = {
    register(data) {
        return apiClient.post('/users/register', data)
    },

    getUserProfile(userId) {
        return apiClient.get(`/users/${userId}`)
    },

    getFriendsList(userId, page = 0, size = 100) {
        return apiClient.get(`/users/${userId}/friends-list`, {
            params: { page, size }
        })
    },

    getFriendshipStatus(userId) {
        return apiClient.get(`/users/${userId}/friendship-status`)
    }
}

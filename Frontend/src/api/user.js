import apiClient from './axios'

export const userApi = {
    register(data) {
        return apiClient.post('/users/register', data, {
            'headers': { 'Content-Type': 'application/json' },
        })
    },
    
    getCurrentUser() {
        return apiClient.get('/users/me')
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

    // TODO: Add friendship action APIs when backend is ready
    // sendFriendRequest(userId)
    // cancelFriendRequest(userId)
    // acceptFriendRequest(userId)
    // declineFriendRequest(userId)
    // removeFriend(userId)
}

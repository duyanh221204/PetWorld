import apiClient from './axios'

export const friendshipApi = {
    getFriendshipRequests(page = 0, size = 100) {
        return apiClient.get('/friendships/friendship-requests', {
            params: { page, size }
        })
    },

    sendFriendRequest(recipientId) {
        return apiClient.post(`/friendships/requests/${recipientId}`)
    },

    cancelFriendRequest(friendshipId) {
        return apiClient.delete(`/friendships/${friendshipId}/cancel`)
    },

    acceptFriendRequest(friendshipId) {
        return apiClient.put(`/friendships/${friendshipId}/accept`)
    },

    rejectFriendRequest(friendshipId) {
        return apiClient.delete(`/friendships/${friendshipId}/reject`)
    },

    removeFriend(friendshipId) {
        return apiClient.delete(`/friendships/${friendshipId}`)
    }
}

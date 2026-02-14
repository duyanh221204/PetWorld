import apiClient from './axios'

export const reactionApi = {
    createReaction(postId) {
        return apiClient.post(`/posts/${postId}/reactions`)
    },

    deleteReaction(postId) {
        return apiClient.delete(`/posts/${postId}/reactions`)
    },

    getReactionsByPostId(postId, page = 0, size = 100) {
        return apiClient.get(`/posts/${postId}/reactions`, {
            params: { page, size }
        })
    }
}

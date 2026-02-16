import apiClient from './axios'

export const commentApi = {
    getCommentsByPostId(postId, page = 0, size = 50) {
        return apiClient.get(`/posts/${postId}/comments`, {
            params: { page, size }
        })
    },

    getRepliesByRootCommentId(postId, rootCommentId) {
        return apiClient.get(`/posts/${postId}/comments/${rootCommentId}/replies`)
    },

    createComment(postId, content, parentCommentId = null) {
        const requestBody = { content }
        if (parentCommentId) {
            requestBody.parentCommentId = parentCommentId
        }
        return apiClient.post(`/posts/${postId}/comments`, requestBody)
    },

    updateComment(postId, commentId, content) {
        return apiClient.put(`/posts/${postId}/comments/${commentId}`, { content })
    },

    deleteComment(postId, commentId) {
        return apiClient.delete(`/posts/${postId}/comments/${commentId}`)
    },

    getCommentPage(postId, commentId, size = 50) {
        return apiClient.get(`/posts/${postId}/comments/page-of/${commentId}`, {
            params: { size }
        })
    }
}

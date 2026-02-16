import apiClient from './axios'

export const commentApi = {
    // Lấy danh sách comments của post (root comments only)
    getCommentsByPostId(postId, page = 0, size = 50) {
        return apiClient.get(`/posts/${postId}/comments`, {
            params: { page, size }
        })
    },

    // Lấy danh sách replies của một root comment
    getRepliesByRootCommentId(postId, rootCommentId) {
        return apiClient.get(`/posts/${postId}/comments/${rootCommentId}/replies`)
    },

    // Tạo comment (root hoặc reply)
    createComment(postId, content, parentCommentId = null) {
        const requestBody = { content }
        if (parentCommentId) {
            requestBody.parentCommentId = parentCommentId
        }
        return apiClient.post(`/posts/${postId}/comments`, requestBody)
    },

    // Update comment
    updateComment(postId, commentId, content) {
        return apiClient.put(`/posts/${postId}/comments/${commentId}`, { content })
    },

    // Xóa comment
    deleteComment(postId, commentId) {
        return apiClient.delete(`/posts/${postId}/comments/${commentId}`)
    },

    // Lấy page number của một comment
    getCommentPage(postId, commentId, size = 50) {
        return apiClient.get(`/posts/${postId}/comments/page-of/${commentId}`, {
            params: { size }
        })
    }
}

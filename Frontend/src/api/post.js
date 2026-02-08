import apiClient from './axios'

export const postApi = {
    getPostsForNewsFeed(page = 0, size = 10) {
        return apiClient.get('/posts', {
            params: { page, size }
        })
    },

    getGroupPosts(page = 0, size = 10) {
        return apiClient.get('/posts/groups', {
            params: { page, size }
        })
    },

    getPostById(postId) {
        return apiClient.get(`/posts/${postId}`)
    }
}

import apiClient from './axios'

export const postApi = {
    getPostsForNewsFeed(page = 0, size = 10) {
        return apiClient.get('/posts', {
            params: { page, size }
        })
    },

    getGroupsPosts(page = 0, size = 10) {
        return apiClient.get('/posts/groups', {
            params: { page, size }
        })
    },

    getFriendsPostsForNewsFeed(page = 0, size = 10) {
        return apiClient.get('/posts/friends', {
            params: { page, size }
        })
    },

    getUserPosts(userId, page = 0, size = 10) {
        return apiClient.get(`/posts/users/${userId}`, {
            params: { page, size }
        })
    },

    getPostById(postId) {
        return apiClient.get(`/posts/${postId}`)
    }
}

import apiClient from './axios'

export const authApi = {
    login(data) {
        return apiClient.post('/auth/login', data)
    },

    activateUser(data) {
        return apiClient.post('/auth/activate-user', data)
    },

    refresh() {
        return apiClient.post('/auth/refresh')
    },

    logout() {
        return apiClient.post('/auth/logout')
    }
}

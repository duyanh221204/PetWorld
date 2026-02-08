import apiClient from './axios'

export const authApi = {
    login(data) {
        return apiClient.post('/auth/login', data, {
            'headers': { 'Content-Type': 'application/json' }
        })
    },

    activateUser(data) {
        return apiClient.post('/auth/activate-user', data, {
            'headers': { 'Content-Type': 'application/json' }
        })
    },

    refresh() {
        return apiClient.post('/auth/refresh')
    },

    logout() {
        return apiClient.post('/auth/logout')
    }
}

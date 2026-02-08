import apiClient from './axios'

export const userApi = {
    register(data) {
        return apiClient.post('/users/register', data, {
            'headers': { 'Content-Type': 'application/json' },
        })
    },
    
    getCurrentUser() {
        return apiClient.get('/users/me')
    }
}

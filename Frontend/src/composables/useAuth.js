import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

const accessToken = ref(null)
const user = ref(null)

export function useAuth() {
    const isAuthenticated = computed(() => !!accessToken.value && !!user.value)

    const login = async (credentials) => {
        try {
            const response = await authApi.login(credentials)
            if (response.data.status === 200) {
                accessToken.value = response.data.data.accessToken
                user.value = response.data.data.user
            } else {
                accessToken.value = null
                user.value = null
            }
            return response
        } catch (error) {
            accessToken.value = null
            user.value = null
            throw error
        }
    }

    const refreshAccessToken = async () => {
        try {
            const response = await authApi.refresh()
            if (response.data.status === 200) {
                accessToken.value = response.data.data.accessToken
                user.value = response.data.data.user
            }
            return response
        } catch (error) {
            accessToken.value = null
            user.value = null
            throw error
        }
    }

    const logout = async () => {
        try {
            await authApi.logout()
        } finally {
            accessToken.value = null
            user.value = null
        }
    }

    return {
        accessToken,
        user,
        isAuthenticated,
        login,
        refreshAccessToken,
        logout
    }
}

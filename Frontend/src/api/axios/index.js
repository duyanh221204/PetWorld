import axios from 'axios'
import { useAuth } from '@/composables/useAuth'
import router from '@/router'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true
})

apiClient.interceptors.request.use(
    (config) => {
        const url = config.url || ''
        if (url.includes('/auth/login') || url.includes('/auth/refresh'))
            return config

        const auth = useAuth()
        if (auth.accessToken.value) {
            config.headers = config.headers || {}
            config.headers.Authorization = `Bearer ${auth.accessToken.value}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

let isRefreshing = false
let refreshQueue = []

const processQueue = (error, token = null) => {
    refreshQueue.forEach(({ resolve, reject }) => {
        if (error)
            reject(error)
        else
            resolve(token)
    })
    refreshQueue = []
}

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config
        const status = error.response?.status
        const url = originalRequest?.url || ''

        if (status !== 401 || url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/logout'))
            return Promise.reject(error)

        if (originalRequest._retry)
            return Promise.reject(error)
        originalRequest._retry = true

        const auth = useAuth()

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                refreshQueue.push({
                    resolve: (token) => {
                        originalRequest.headers = originalRequest.headers || {}
                        originalRequest.headers.Authorization = `Bearer ${token}`
                        resolve(apiClient(originalRequest))
                    },
                    reject
                })
            })
        }

        isRefreshing = true
        try {
            await auth.refreshAccessToken()
            const newToken = auth.accessToken.value

            processQueue(null, newToken)

            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return apiClient(originalRequest)
        } catch (refreshError) {
            processQueue(refreshError, null)

            await auth.logout()
            if (router.currentRoute.value.name !== 'Login')
                await router.replace({
                    name: 'Login',
                    query: { redirect: router.currentRoute.value.fullPath }
                })

            return Promise.reject(refreshError)
        } finally {
            isRefreshing = false
        }
    }
)

export default apiClient

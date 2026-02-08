import apiClient from './axios'

export const emailApi = {
    sendVerificationCode(email) {
        return apiClient.post('/email/send-verification-code', { email })
    }
}

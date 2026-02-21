import apiClient from './axios'

export const groupJoinRequestAnswerApi = {
    getAnswers(groupId, requestId) {
        return apiClient.get(`/groups/${groupId}/join-requests/${requestId}/answers`)
    }
}

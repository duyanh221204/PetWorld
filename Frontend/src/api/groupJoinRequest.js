import apiClient from './axios'

export const groupJoinRequestApi = {
    createGroupJoinRequest(groupId, answers = null) {
        return apiClient.post(`/groups/${groupId}/join-requests`, answers)
    },

    countGroupJoinRequests(groupId) {
        return apiClient.get(`/groups/${groupId}/join-requests/count`)
    },

    getGroupJoinRequests(groupId, page = 0, size = 100) {
        return apiClient.get(`/groups/${groupId}/join-requests`, {
            params: { page, size }
        })
    },

    getRequestPage(groupId, requestId, size = 100) {
        return apiClient.get(`/groups/${groupId}/join-requests/page-of/${requestId}`, {
            params: { size }
        })
    },

    approveGroupJoinRequest(groupId, requestId) {
        return apiClient.post(`/groups/${groupId}/join-requests/${requestId}/approve`)
    },

    rejectGroupJoinRequest(groupId, requestId) {
        return apiClient.delete(`/groups/${groupId}/join-requests/${requestId}/reject`)
    },

    cancelGroupJoinRequest(groupId) {
        return apiClient.delete(`/groups/${groupId}/join-requests/cancel`)
    }
}

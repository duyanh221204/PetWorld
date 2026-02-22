import apiClient from './axios'

export const groupMembershipApi = {
    getGroupMembers(groupId, page = 0, size = 100) {
        return apiClient.get(`/groups/${groupId}/memberships`, {
            params: { page, size }
        })
    },

    updateGroupMembership(groupId, userId, data) {
        return apiClient.put(`/groups/${groupId}/memberships/${userId}/role`, data)
    },

    deleteGroupMembership(groupId, userId) {
        return apiClient.delete(`/groups/${groupId}/memberships/${userId}`)
    },

    leaveGroup(groupId) {
        return apiClient.delete(`/groups/${groupId}/memberships/leave`)
    },

    transferOwnershipAndLeave(groupId, userId) {
        return apiClient.post(`/groups/${groupId}/memberships/transfer-ownership/${userId}`)
    }
}

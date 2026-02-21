import apiClient from './axios'

export const groupJoinFormApi = {
    getGroupJoinForms(groupId) {
        return apiClient.get(`/groups/${groupId}/join-forms`)
    },

    getActiveGroupJoinForm(groupId) {
        return apiClient.get(`/groups/${groupId}/join-forms/active`)
    },

    createGroupJoinForm(groupId, data) {
        return apiClient.post(`/groups/${groupId}/join-forms`, data)
    },

    updateGroupJoinForm(groupId, formId, data) {
        return apiClient.put(`/groups/${groupId}/join-forms/${formId}`, data)
    },

    activateGroupJoinForm(groupId, formId) {
        return apiClient.put(`/groups/${groupId}/join-forms/${formId}/activate`)
    },

    deleteGroupJoinForm(groupId, formId) {
        return apiClient.delete(`/groups/${groupId}/join-forms/${formId}`)
    }
}

import apiClient from './axios'

export const groupJoinFormQuestionApi = {
    getGroupJoinFormQuestions(groupId, formId) {
        return apiClient.get(`/groups/${groupId}/join-forms/${formId}/questions`)
    },

    createGroupJoinFormQuestion(groupId, formId, data) {
        return apiClient.post(`/groups/${groupId}/join-forms/${formId}/questions`, data)
    },

    updateGroupJoinFormQuestion(groupId, formId, questionId, data) {
        return apiClient.put(`/groups/${groupId}/join-forms/${formId}/questions/${questionId}`, data)
    },

    deleteGroupJoinFormQuestion(groupId, formId, questionId) {
        return apiClient.delete(`/groups/${groupId}/join-forms/${formId}/questions/${questionId}`)
    },

    updateGroupJoinFormQuestionOrders(groupId, formId, data) {
        return apiClient.put(`/groups/${groupId}/join-forms/${formId}/questions/reorder`, data)
    }
}

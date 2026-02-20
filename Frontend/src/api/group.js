import apiClient from './axios'

export const groupApi = {
    getOwnedGroups(page = 0, size = 100) {
        return apiClient.get('/groups/me/owned', {
            params: { page, size }
        })
    },

    getJoinedGroups(page = 0, size = 100) {
        return apiClient.get('/groups/me/joined', {
            params: { page, size }
        })
    },

    getJoinRequestedGroups(page = 0, size = 100) {
        return apiClient.get('/groups/me/requests', {
            params: { page, size }
        })
    },

    getDiscoverGroups(page = 0, size = 100) {
        return apiClient.get('/groups/discover', {
            params: { page, size }
        })
    },

    getGroupById(groupId) {
        return apiClient.get(`/groups/${groupId}`)
    },

    createGroup(data) {
        return apiClient.post('/groups', data)
    }
}

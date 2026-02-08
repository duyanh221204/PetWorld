import apiClient from "@/api/axios.js";

export const uploadApi = {
    async uploadFile(file) {
        const formData = new FormData()
        formData.append('file', file)

        return apiClient.post('/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    }
}

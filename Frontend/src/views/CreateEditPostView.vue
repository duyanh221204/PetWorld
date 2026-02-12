<template>
  <div class="create-edit-post-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />
      
      <div class="form-container">
        <div class="form-card">
          <div class="form-header">
            <h1 class="form-title">{{ isEditMode ? 'Edit Post' : 'Create Post' }}</h1>
            <!-- <button @click="handleCancel" class="close-button">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button> -->
          </div>

          <form @submit.prevent="handleSubmit" class="form-content">
            <div class="form-group">
              <label for="content" class="form-label">Content</label>
              <textarea
                id="content"
                v-model="formData.content"
                placeholder="What's on your mind?"
                rows="6"
                class="form-textarea"
              ></textarea>
            </div>

            <div class="form-group">
              <label class="form-label">Visibility *</label>
              <div class="visibility-options">
                <button
                  v-for="option in visibilityOptions"
                  :key="option.value"
                  type="button"
                  @click="formData.visibility = option.value"
                  :class="['visibility-option', { active: formData.visibility === option.value }]"
                >
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path :d="option.iconPath" :fill-rule="option.fillRule" :clip-rule="option.clipRule"/>
                  </svg>
                  <span>{{ option.label }}</span>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Images</label>
              <div class="media-upload-area">
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  multiple
                  @change="handleFileSelect"
                  class="hidden"
                />
                <button
                  type="button"
                  @click="$refs.fileInput.click()"
                  class="upload-button"
                >
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                  </svg>
                  Add Images
                </button>
              </div>

              <div v-if="mediaItems.length > 0" class="media-preview-grid">
                <draggable
                  v-model="mediaItems"
                  item-key="tempId"
                  class="media-grid"
                  :animation="200"
                >
                  <template #item="{ element, index }">
                    <div class="media-item">
                      <img :src="element.preview || element.mediaUrl" :alt="`Image ${index + 1}`" class="media-image"/>
                      <button
                        type="button"
                        @click="removeMedia(index)"
                        class="remove-button"
                      >
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                      </button>
                      <div class="drag-handle">
                        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                          <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z"/>
                        </svg>
                      </div>
                    </div>
                  </template>
                </draggable>
                <p class="media-hint">Drag to reorder images</p>
              </div>
            </div>

            <div v-if="errorMessage" class="error-message">
              {{ errorMessage }}
            </div>

            <div class="form-actions">
              <button type="button" @click="handleCancel" class="btn-cancel">
                Cancel
              </button>
              <button type="submit" :disabled="isSubmitting" class="btn-submit">
                <LoadingSpinner v-if="isSubmitting" size="sm" />
                <span v-else>{{ isEditMode ? 'Update' : 'Create' }} Post</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>

    <div v-if="showConfirmModal" class="modal-overlay" @click="isSubmitting ? null : (showConfirmModal = false)">
      <div class="modal-content" @click.stop>
        <h2 class="modal-title">{{ isSubmitting ? 'Uploading...' : `Confirm ${isEditMode ? 'Update' : 'Create'}` }}</h2>
        <div v-if="isSubmitting" class="uploading-container">
          <LoadingSpinner size="lg" />
        </div>
        <p v-else class="modal-message">
          Are you sure you want to {{ isEditMode ? 'update' : 'create' }} this post?
        </p>
        <div v-if="!isSubmitting" class="modal-actions">
          <button @click="showConfirmModal = false" class="btn-modal-cancel">
            Cancel
          </button>
          <button @click="confirmSubmit" class="btn-modal-confirm">
            Confirm
          </button>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import draggable from 'vuedraggable'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/LeftSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { postApi } from '@/api/post'
import { uploadApi } from '@/api/upload'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { user } = useAuth()

const fileInput = ref(null)
const isEditMode = ref(false)
const postId = ref(null)
const isSubmitting = ref(false)
const showConfirmModal = ref(false)
const errorMessage = ref('')

const formData = ref({
  content: '',
  visibility: 'PUBLIC',
  postMediaResources: []
})

const mediaItems = ref([])

const visibilityOptions = [
  {
    value: 'PUBLIC',
    label: 'Public',
    iconPath: 'M10 18a8 8 0 100-16 8 8 0 000 16zM4.332 8.027a6.012 6.012 0 011.912-2.706C6.512 5.73 6.974 6 7.5 6A1.5 1.5 0 019 7.5V8a2 2 0 004 0 2 2 0 011.523-1.943A5.977 5.977 0 0116 10c0 .34-.028.675-.083 1H15a2 2 0 00-2 2v2.197A5.973 5.973 0 0110 16v-2a2 2 0 00-2-2 2 2 0 01-2-2 2 2 0 00-1.668-1.973z',
    fillRule: 'evenodd',
    clipRule: 'evenodd'
  },
  {
    value: 'FRIENDS_ONLY',
    label: 'Friends Only',
    iconPath: 'M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z'
  },
  {
    value: 'PRIVATE',
    label: 'Private',
    iconPath: 'M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z',
    fillRule: 'evenodd',
    clipRule: 'evenodd'
  }
]

const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  
  files.forEach((file) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      mediaItems.value.push({
        tempId: Date.now() + Math.random(),
        file: file,
        preview: e.target.result,
        mediaUrl: null,
        id: null
      })
    }
    reader.readAsDataURL(file)
  })

  event.target.value = ''
}

const removeMedia = (index) => mediaItems.value.splice(index, 1)

const handleCancel = () => {
  if (isEditMode.value)
    router.back()
  else
    router.push({ name: 'Profile', params: { userId: user.value.id } })
}

const handleSubmit = () => {
  errorMessage.value = ''

  const hasContent = formData.value.content && formData.value.content.trim().length > 0
  const hasMedia = mediaItems.value.length > 0

  if (!hasContent && !hasMedia) {
    errorMessage.value = 'Please add some content or images to your post'
    return
  }

  if (!formData.value.visibility) {
    errorMessage.value = 'Please select a visibility option'
    return
  }

  showConfirmModal.value = true
}

const confirmSubmit = async () => {
  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const uploadedMediaItems = []
    
    for (let i = 0; i < mediaItems.value.length; ++i) {
      const item = mediaItems.value[i]

      if (item.file && !item.mediaUrl) {
        try {
          const uploadResponse = await uploadApi.uploadFile(item.file)
          if (uploadResponse.data.status === 200) {
            uploadedMediaItems.push({
              mediaUrl: uploadResponse.data.data,
              displayOrder: i + 1,
              id: item.id || null
            })
          }
        } catch (uploadError) {
          console.error('Failed to upload file:', uploadError)
          throw uploadError
        }
      } else if (item.mediaUrl) {
        uploadedMediaItems.push({
          id: item.id,
          mediaUrl: item.mediaUrl,
          displayOrder: i + 1
        })
      }
    }

    const requestData = {
      content: formData.value.content || null,
      visibility: formData.value.visibility,
      postMediaResources: uploadedMediaItems.length > 0 ? uploadedMediaItems : null,
      groupId: null
    }

    let response
    if (isEditMode.value)
      response = await postApi.updatePost(postId.value, requestData)
    else
      response = await postApi.createPost(requestData)

    if (response.data.status === 200) {
      showConfirmModal.value = false
      
      if (isEditMode.value)
        router.back()
      else
        await router.push({name: 'Profile', params: {userId: user.value.id}})
    }
  } catch (error) {
    errorMessage.value = error.message || error.response?.data?.message || 'Failed to save post. Please try again.'
    showConfirmModal.value = false
    throw error
  } finally {
    isSubmitting.value = false
  }
}

const loadPost = async () => {
  if (!postId.value)
    return

  try {
    const response = await postApi.getPostById(postId.value)
    if (response.data.status === 200) {
      const post = response.data.data
      
      formData.value.content = post.content || ''
      formData.value.visibility = post.visibility || 'PUBLIC'
      
      if (post.postMediaResources && post.postMediaResources.length > 0) {
        mediaItems.value = post.postMediaResources
          .sort((a, b) => a.displayOrder - b.displayOrder)
          .map((media) => ({
            tempId: media.id,
            id: media.id,
            mediaUrl: media.mediaUrl,
            preview: null,
            file: null
          }))
      }
    }
  } catch (error) {
    errorMessage.value = 'Failed to load post data'
    throw error
  }
}

onMounted(() => {
  const editPostId = route.query.editPostId
  if (editPostId) {
    isEditMode.value = true
    postId.value = parseInt(editPostId)
    loadPost()
  }
})
</script>

<style scoped>
.create-edit-post-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-8;
}

.form-container {
  @apply max-w-3xl mx-auto px-4;
}

.form-card {
  @apply bg-white rounded-xl shadow-md overflow-hidden;
}

.form-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.form-title {
  @apply text-2xl font-bold text-gray-900;
}

/* .close-button {
  @apply text-gray-500 hover:text-gray-700 transition-colors;
} */

.form-content {
  @apply p-6 space-y-6;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.form-textarea {
  @apply w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none;
}

.visibility-options {
  @apply grid grid-cols-3 gap-3;
}

.visibility-option {
  @apply flex flex-col items-center justify-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-300 transition-colors;
}

.visibility-option.active {
  @apply border-primary-600 bg-primary-50;
}

.visibility-option svg {
  @apply mb-2;
}

.visibility-option span {
  @apply text-sm font-medium text-gray-700;
}

.media-upload-area {
  @apply space-y-3;
}

.upload-button {
  @apply flex items-center justify-center space-x-2 w-full px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-400 hover:bg-primary-50 transition-colors text-gray-600 hover:text-primary-600;
}

.media-preview-grid {
  @apply mt-4 space-y-2;
}

.media-grid {
  @apply grid grid-cols-3 gap-3;
}

.media-item {
  @apply relative aspect-square rounded-lg overflow-hidden border-2 border-gray-200;
}

.media-item:hover .remove-button,
.media-item:hover .drag-handle {
  opacity: 1;
}

.media-image {
  @apply w-full h-full object-cover;
}

.remove-button {
  @apply absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors;
  opacity: 0;
  transition: opacity 0.2s;
}

.drag-handle {
  @apply absolute top-2 left-2 p-1 bg-white text-gray-600 rounded-full cursor-move;
  opacity: 0;
  transition: opacity 0.2s;
}

.media-hint {
  @apply text-xs text-gray-500 text-center;
}

.error-message {
  @apply p-4 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm;
}

.form-actions {
  @apply flex justify-end space-x-3 pt-4 border-t border-gray-200;
}

.btn-cancel {
  @apply px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-medium;
}

.btn-submit {
  @apply flex items-center justify-center px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed min-w-[120px];
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
}

.modal-content {
  @apply bg-white rounded-xl shadow-2xl p-6 max-w-md w-full mx-4;
}

.modal-title {
  @apply text-xl font-bold text-gray-900 mb-2;
}

.uploading-container {
  @apply flex items-center justify-center py-8;
}

.modal-message {
  @apply text-gray-700 mb-6;
}

.modal-actions {
  @apply flex space-x-3 justify-end;
}

.btn-modal-cancel {
  @apply px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors;
}

.btn-modal-confirm {
  @apply flex items-center justify-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 min-w-[100px];
}

.hidden {
  @apply sr-only;
}
</style>

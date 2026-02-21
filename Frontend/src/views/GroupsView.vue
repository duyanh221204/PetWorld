<template>
  <div class="groups-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="groups-container">
        <div class="tabs-wrapper">
          <div class="tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              @click="activeTab = tab.value"
              :class="['tab', { active: activeTab === tab.value }]"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>

        <div class="groups-content-wrapper">
          <div class="groups-feed">
            <LoadingSpinner v-if="isLoading && groups.length === 0" size="lg" class="my-8" />

            <div v-else-if="groups.length === 0" class="empty-state">
              <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
              </svg>
              <p class="empty-text">{{ getEmptyMessage() }}</p>
            </div>

            <div v-else class="groups-grid">
              <div
                v-for="group in groups"
                :key="group.id"
                @click="goToGroup(group.id)"
                class="group-card"
              >
                <div class="group-cover-circle">
                  <img
                    :src="group.coverImageUrl || defaultGroupCover"
                    :alt="group.name"
                    class="cover-image-circle"
                    @error="(e) => e.target.src = defaultGroupCover"
                  />
                </div>

                <div class="group-content">
                  <div class="group-info">
                    <h3 class="group-name">{{ group.name }}</h3>
                    <div class="group-meta">
                      <span class="meta-text">{{ group.memberCount }} {{ group.memberCount === 1 ? 'member' : 'members' }}</span>
                      <span class="meta-separator">•</span>
                      <span class="meta-text">{{ formatTime(group.createdAt) }}</span>
                    </div>
                    <p v-if="group.description" class="group-description">{{ group.description }}</p>
                  </div>

                  <div class="group-status">
                    <span :class="['status-badge', getStatusClass()]">
                      {{ getStatusText(group) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="isLoading && groups.length > 0" class="load-more">
              <LoadingSpinner size="md" />
            </div>

            <div v-if="!hasMore && groups.length > 0" class="no-more">
              <p>No more groups</p>
            </div>
          </div>

          <Transition name="fab">
            <button
              v-if="activeTab === 'owned'"
              @click="showCreateModal = true"
              class="floating-action-btn"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
              </svg>
            </button>
          </Transition>
        </div>
      </div>

      <RightSidebar />
    </main>

    <AppFooter />

    <Transition name="modal">
      <div v-if="showCreateModal" class="modal-overlay" @click="isCreating ? null : (showCreateModal = false)">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">{{ isCreating ? 'Creating Group...' : 'Create New Group' }}</h3>
            <button
              v-if="!isCreating"
              @click="showCreateModal = false"
              class="modal-close"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <div v-if="isCreating" class="creating-container">
              <LoadingSpinner size="lg" />
            </div>

            <form v-else @submit.prevent="handleCreateGroup" class="group-form">
              <div class="form-group">
                <label for="groupName" class="form-label">
                  Group Name <span class="required">*</span>
                </label>
                <input
                  id="groupName"
                  v-model="groupForm.name"
                  type="text"
                  placeholder="Enter group name (5-100 characters)"
                  class="form-input"
                  :class="{ 'input-error': groupFormError.name }"
                  maxlength="100"
                />
                <p v-if="groupFormError.name" class="error-text">{{ groupFormError.name }}</p>
              </div>

              <div class="form-group">
                <label for="groupDescription" class="form-label">Description</label>
                <textarea
                  id="groupDescription"
                  v-model="groupForm.description"
                  placeholder="Enter group description (optional)"
                  rows="4"
                  class="form-textarea"
                ></textarea>
              </div>

              <div class="form-group">
                <label class="form-label">Cover Image</label>
                <div class="image-upload-section">
                  <div v-if="coverImagePreview" class="image-preview">
                    <img :src="coverImagePreview" alt="Cover preview" class="preview-img" />
                    <button
                      @click="removeCoverImage"
                      type="button"
                      class="remove-image-btn"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </button>
                  </div>
                  <div v-else class="upload-placeholder">
                    <input
                      ref="coverImageInput"
                      type="file"
                      accept="image/*"
                      @change="handleCoverImageChange"
                      class="hidden-file-input"
                    />
                    <button
                      @click="$refs.coverImageInput.click()"
                      type="button"
                      class="upload-btn"
                    >
                      <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                      </svg>
                      <span class="upload-text">Click to upload cover image</span>
                    </button>
                  </div>
                  <p v-if="uploadError" class="error-text">{{ uploadError }}</p>
                </div>
              </div>

              <div v-if="createError" class="error-message">
                {{ createError }}
              </div>
            </form>
          </div>

          <div v-if="!isCreating" class="modal-footer">
            <button
              @click="showCreateModal = false"
              class="btn-secondary"
            >
              Cancel
            </button>
            <button
              @click="handleCreateGroup"
              class="btn-primary"
              :disabled="!canCreate || isUploading"
            >
              {{ isUploading ? 'Uploading...' : 'Create Group' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import defaultGroupCover from '@/assets/images/group-default-cover-image.png'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import RightSidebar from '@/components/layout/sidebar/RightSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { groupApi } from '@/api/group'
import { uploadApi } from '@/api/upload'

const router = useRouter()

const tabs = [
  { value: 'owned', label: 'Owned Groups' },
  { value: 'joined', label: 'Joined' },
  { value: 'requests', label: 'Requests' },
  { value: 'discover', label: 'Discover' }
]

const activeTab = ref('owned')
const groups = ref([])
const isLoading = ref(false)
const currentPage = ref(0)
const hasMore = ref(true)

const showCreateModal = ref(false)
const isCreating = ref(false)
const isUploading = ref(false)
const createError = ref('')
const uploadError = ref('')
const coverImageInput = ref(null)
const coverImagePreview = ref('')
const coverImageFile = ref(null)
const groupForm = ref({
  name: '',
  description: '',
  coverImageUrl: ''
})
const groupFormError = ref({ name: '' })

const canCreate = computed(() => groupForm.value.name.trim().length >= 5 && groupForm.value.name.trim().length <= 100)

const handleCoverImageChange = (event) => {
  const file = event.target.files[0]
  if (!file)
    return

  uploadError.value = ''

  if (!file.type.startsWith('image/')) {
    uploadError.value = 'Please select an image file'
    return
  }

  if (file.size > 50 * 1024 * 1024) {
    uploadError.value = 'Image size must be less than 50MB'
    return
  }

  coverImageFile.value = file

  const reader = new FileReader()
  reader.onload = (e) => coverImagePreview.value = e.target.result
  reader.readAsDataURL(file)
}

const removeCoverImage = () => {
  coverImageFile.value = null
  coverImagePreview.value = ''
  uploadError.value = ''
  if (coverImageInput.value)
    coverImageInput.value.value = ''
}

const resetForm = () => {
  groupForm.value = {
    name: '',
    description: '',
    coverImageUrl: ''
  }
  groupFormError.value = {
    name: ''
  }
  createError.value = ''
  uploadError.value = ''
  coverImageFile.value = null
  coverImagePreview.value = ''
  if (coverImageInput.value)
    coverImageInput.value.value = ''
}

const handleCreateGroup = async () => {
  groupFormError.value.name = ''
  createError.value = ''
  uploadError.value = ''

  const name = groupForm.value.name.trim()
  if (name.length < 5 || name.length > 100) {
    groupFormError.value.name = 'Group name must be between 5 and 100 characters'
    return
  }

  isCreating.value = true

  try {
    let coverImageUrl = null

    if (coverImageFile.value) {
      isUploading.value = true
      try {
        const uploadResponse = await uploadApi.uploadFile(coverImageFile.value)
        if (uploadResponse.data.status === 200)
          coverImageUrl = uploadResponse.data.data
        else
          throw new Error('Failed to upload image')
      } catch (uploadErr) {
        console.error('Error uploading image:', uploadErr)
        uploadError.value = 'Failed to upload cover image. Please try again.'
        isCreating.value = false
        isUploading.value = false
        return
      } finally {
        isUploading.value = false
      }
    }

    const payload = {
      name: groupForm.value.name.trim(),
      description: groupForm.value.description.trim() || null,
      coverImageUrl: coverImageUrl
    }

    const response = await groupApi.createGroup(payload)

    if (response.data.status === 200) {
      const newGroup = response.data.data
      showCreateModal.value = false
      resetForm()
      await router.push({name: 'GroupDetail', params: {groupId: newGroup.id}})
    } else
      createError.value = response.data.message || 'Failed to create group'
  } catch (error) {
    console.error('Error creating group:', error)
    createError.value = error.response?.data?.message || 'Failed to create group. Please try again.'
  } finally {
    isCreating.value = false
  }
}

const fetchGroups = async (reset = false) => {
  if (isLoading.value || (!hasMore.value && !reset))
    return

  if (reset) {
    currentPage.value = 0
    hasMore.value = true
    groups.value = []
  }

  isLoading.value = true

  try {
    let response
    const page = currentPage.value

    switch (activeTab.value) {
      case 'owned':
        response = await groupApi.getOwnedGroups(page, 100)
        break
      case 'joined':
        response = await groupApi.getJoinedGroups(page, 100)
        break
      case 'requests':
        response = await groupApi.getJoinRequestedGroups(page, 100)
        break
      case 'discover':
        response = await groupApi.getDiscoverGroups(page, 100)
        break
    }

    if (response.data.status === 200) {
      const data = response.data.data
      const newGroups = data.content || []
      
      if (reset)
        groups.value = newGroups
      else
        groups.value.push(...newGroups)

      hasMore.value = !data.last
      ++currentPage.value
    }
  } catch (error) {
    console.error('Error fetching groups:', error)
  } finally {
    isLoading.value = false
  }
}

const goToGroup = (groupId) => router.push({ name: 'GroupDetail', params: { groupId } })

const getEmptyMessage = () => {
  switch (activeTab.value) {
    case 'owned':
      return 'You don\'t own any groups yet'
    case 'joined':
      return 'You haven\'t joined any groups yet'
    case 'requests':
      return 'You haven\'t requested to join any groups'
    case 'discover':
      return 'No groups to discover'
    default:
      return 'No groups found'
  }
}

const getStatusText = (group) => {
  switch (activeTab.value) {
    case 'owned':
      return 'OWNER'
    case 'joined':
      return group.currentUserRole || 'MEMBER'
    case 'requests':
      return 'REQUESTED'
    case 'discover':
      return 'NOT JOINED'
    default:
      return ''
  }
}

const getStatusClass = () => {
  switch (activeTab.value) {
    case 'owned':
      return 'status-owner'
    case 'joined':
      return 'status-member'
    case 'requests':
      return 'status-requested'
    case 'discover':
      return 'status-not-joined'
    default:
      return ''
  }
}

const formatTime = (timestamp) => {
  if (!timestamp)
    return ''

  try {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (days > 365) {
      const years = Math.floor(days / 365)
      return `${years} ${years === 1 ? 'year' : 'years'} ago`
    }
    if (days > 30) {
      const months = Math.floor(days / 30)
      return `${months} ${months === 1 ? 'month' : 'months'} ago`
    }
    if (days > 0)
      return `${days} ${days === 1 ? 'day' : 'days'} ago`
    if (hours > 0)
      return `${hours} ${hours === 1 ? 'hour' : 'hours'} ago`
    if (minutes > 0)
      return `${minutes} ${minutes === 1 ? 'minute' : 'minutes'} ago`
    return 'Just now'
  } catch (e) {
    return ''
  }
}

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 500)
    fetchGroups()
}

watch(activeTab, () => fetchGroups(true))

onMounted(() => {
  fetchGroups(true)
  window.addEventListener('scroll', handleScroll)
})
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.groups-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.groups-container {
  @apply max-w-3xl mx-auto px-4;
  width: 100%;
}

.tabs-wrapper {
  @apply bg-white rounded-xl shadow-sm mb-4 sticky top-0 z-10;
}

.tabs {
  @apply flex border-b border-gray-200;
}

.tab {
  @apply flex-1 flex items-center justify-center py-4 text-gray-600 font-medium transition-colors border-b-2 border-transparent hover:text-primary-600 hover:border-primary-200;
}

.tab.active {
  @apply text-primary-600 border-primary-600;
}

.groups-content-wrapper {
  position: relative;
  min-height: 400px;
}

.groups-feed {
  @apply space-y-4;
  padding-bottom: 20px;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16 bg-white rounded-xl shadow-sm;
}

.empty-icon {
  @apply w-20 h-20 text-gray-300 mb-4;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

.groups-grid {
  @apply grid grid-cols-1 gap-4;
}

.group-card {
  @apply bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow cursor-pointer overflow-hidden p-4 flex items-center gap-4;
}

.group-cover-circle {
  @apply w-16 h-16 rounded-full bg-gray-200 flex-shrink-0 overflow-hidden;
}

.cover-image-circle {
  @apply w-full h-full object-cover;
}

.group-content {
  @apply flex-1 flex items-center gap-4 min-w-0;
}

.group-info {
  @apply flex-1 min-w-0;
}

.group-name {
  @apply text-lg font-bold text-gray-900 mb-1 truncate;
}

.group-meta {
  @apply flex items-center gap-2 text-xs text-gray-500 mb-1;
}

.meta-text {
  @apply text-gray-600;
}

.meta-separator {
  @apply text-gray-400;
}

.group-description {
  @apply text-gray-700 text-xs line-clamp-1;
}

.group-status {
  @apply flex-shrink-0;
}

.status-badge {
  @apply px-2.5 py-1 rounded-full text-xs font-semibold uppercase;
}

.status-owner {
  @apply bg-purple-100 text-purple-700;
}

.status-member {
  @apply bg-blue-100 text-blue-700;
}

.status-requested {
  @apply bg-yellow-100 text-yellow-700;
}

.status-not-joined {
  @apply bg-gray-100 text-gray-600;
}

.load-more {
  @apply flex justify-center py-8;
}

.no-more {
  @apply text-center py-8 text-gray-500;
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl max-w-md w-full max-h-[90vh] overflow-hidden flex flex-col;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-xl font-bold text-gray-900;
}

.modal-close {
  @apply text-gray-400 hover:text-gray-600 transition-colors;
}

.modal-body {
  @apply flex-1 overflow-y-auto p-6;
}

.creating-container {
  @apply flex items-center justify-center py-12;
}

.group-form {
  @apply space-y-4;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.required {
  @apply text-red-500;
}

.form-input,
.form-textarea {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-shadow;
}

.form-input.input-error {
  @apply border-red-500 focus:ring-red-500;
}

.form-textarea {
  @apply resize-none;
}

.help-text {
  @apply text-xs text-gray-500;
}

.error-text {
  @apply text-xs text-red-600 mt-1;
}

.error-message {
  @apply bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm;
}

.modal-footer {
  @apply flex items-center justify-end gap-3 p-6 border-t border-gray-200;
}

.btn-secondary {
  @apply px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors;
}

.btn-primary {
  @apply px-6 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors;
}

.image-upload-section {
  @apply space-y-2;
}

.image-preview {
  @apply relative rounded-lg overflow-hidden border-2 border-gray-300;
}

.preview-img {
  @apply w-full h-48 object-cover;
}

.remove-image-btn {
  @apply absolute top-2 right-2 bg-red-500 hover:bg-red-600 text-white p-2 rounded-full transition-colors shadow-lg;
}

.upload-placeholder {
  @apply border-2 border-dashed border-gray-300 rounded-lg p-8 hover:border-primary-400 transition-colors;
}

.hidden-file-input {
  @apply hidden;
}

.upload-btn {
  @apply w-full flex flex-col items-center justify-center gap-2 text-gray-600 hover:text-primary-600 transition-colors;
}

.upload-text {
  @apply text-sm font-medium text-center;
}

.floating-action-btn {
  @apply absolute bg-primary-600 hover:bg-primary-700 text-white p-4 rounded-full shadow-2xl transition-all;
  right: 0;
  bottom: 16px;
  z-index: 10;
}

.floating-action-btn:hover {
  transform: scale(1.1);
}

.floating-action-btn:active {
  transform: scale(0.95);
}

.fab-enter-active,
.fab-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fab-enter-from,
.fab-leave-to {
  opacity: 0;
  transform: scale(0) rotate(-180deg);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-dialog,
.modal-leave-active .modal-dialog {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.modal-enter-from .modal-dialog,
.modal-leave-to .modal-dialog {
  transform: scale(0.95);
  opacity: 0;
}
</style>

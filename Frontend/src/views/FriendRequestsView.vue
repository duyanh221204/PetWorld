<template>
  <div class="friend-requests-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="requests-container">
        <div class="requests-header">
          <h1 class="requests-title">Friend Requests</h1>
        </div>

        <LoadingSpinner v-if="isLoading && requests.length === 0" size="lg" class="my-8" />

        <template v-else>
          <div v-if="requests.length > 0" class="requests-list">
            <div
              v-for="request in requests"
              :key="request.id"
              class="request-item"
            >
              <div class="request-info" @click="goToProfile(request.senderId)">
                <img
                  :src="request.senderAvatar || defaultAvatar"
                  :alt="request.senderUsername"
                  class="request-avatar"
                />
                <div class="request-details">
                  <span class="request-username">{{ request.senderUsername }}</span>
                  <span v-if="request.status === 'accepted'" class="request-status">
                    You are now friends
                  </span>
                </div>
              </div>

              <div v-if="request.status !== 'accepted' && request.status !== 'rejected'" class="request-actions">
                <button
                  @click="handleAccept(request)"
                  class="btn-friendship btn-accept"
                  :disabled="isProcessing"
                >
                  <LoadingSpinner v-if="isProcessing" size="sm" />
                  <template v-else>
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                    </svg>
                    Accept
                  </template>
                </button>
                <button
                  @click="handleReject(request)"
                  class="btn-friendship btn-cancel"
                  :disabled="isProcessing"
                >
                  <LoadingSpinner v-if="isProcessing" size="sm" />
                  <template v-else>
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                    </svg>
                    Reject
                  </template>
                </button>
              </div>
            </div>
          </div>

          <div v-if="isLoading && requests.length > 0" class="load-more">
            <LoadingSpinner size="md" />
          </div>

          <div v-if="!isLoading && requests.length === 0" class="empty-state">
            <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
            </svg>
            <p class="empty-text">No friend requests</p>
          </div>
        </template>
      </div>

      <RightSidebar />
    </main>

    <Transition name="modal">
      <div v-if="showAcceptModal" class="modal-overlay" @click="showAcceptModal = false">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">Confirm Action</h3>
            <button @click="showAcceptModal = false" class="modal-close" :disabled="isProcessing">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
          
          <div class="modal-body">
            <p class="modal-text">
              Accept friend request from {{ selectedRequest?.senderUsername }}?
            </p>
          </div>
          
          <div class="modal-footer">
            <button 
              @click="showAcceptModal = false" 
              class="btn-secondary"
              :disabled="isProcessing"
            >
              Cancel
            </button>
            <button 
              @click="confirmAccept" 
              class="btn-primary btn-accept"
              :disabled="isProcessing"
            >
              <LoadingSpinner v-if="isProcessing" size="sm" class="mr-2" />
              Accept
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="modal">
      <div v-if="showRejectModal" class="modal-overlay" @click="showRejectModal = false">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">Confirm Action</h3>
            <button @click="showRejectModal = false" class="modal-close" :disabled="isProcessing">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
          
          <div class="modal-body">
            <p class="modal-text">
              Reject friend request from {{ selectedRequest?.senderUsername }}?
            </p>
          </div>
          
          <div class="modal-footer">
            <button 
              @click="showRejectModal = false" 
              class="btn-secondary"
              :disabled="isProcessing"
            >
              Cancel
            </button>
            <button 
              @click="confirmReject" 
              class="btn-primary"
              :disabled="isProcessing"
            >
              <LoadingSpinner v-if="isProcessing" size="sm" class="mr-2" />
              Reject
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/images/default-avatar.png'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import RightSidebar from '@/components/layout/sidebar/RightSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { friendshipApi } from '@/api/friendship'

const router = useRouter()

const requests = ref([])
const isLoading = ref(false)
const isProcessing = ref(false)
const currentPage = ref(0)
const hasMore = ref(true)

const showAcceptModal = ref(false)
const showRejectModal = ref(false)
const selectedRequest = ref(null)

const loadRequests = async () => {
  if (isLoading.value || !hasMore.value)
    return

  isLoading.value = true
  try {
    const response = await friendshipApi.getFriendshipRequests(currentPage.value, 100)
    if (response.data.status === 200) {
      const newRequests = response.data.data.content
      requests.value.push(...newRequests)
      hasMore.value = !response.data.data.last
      ++currentPage.value
    }
  } catch (error) {
    console.error('Failed to load friend requests:', error)
  } finally {
    isLoading.value = false
  }
}

const handleAccept = (request) => {
  selectedRequest.value = request
  showAcceptModal.value = true
}

const confirmAccept = async () => {
  if (!selectedRequest.value)
    return

  isProcessing.value = true
  try {
    const response = await friendshipApi.acceptFriendRequest(selectedRequest.value.id)
    if (response.data.status === 200) {
      const index = requests.value.findIndex(r => r.id === selectedRequest.value.id)
      if (index !== -1)
        requests.value[index].status = 'accepted'

      showAcceptModal.value = false
      selectedRequest.value = null
    }
  } catch (error) {
    console.error('Failed to accept friend request:', error)
    alert('Failed to accept friend request. Please try again.')
  } finally {
    isProcessing.value = false
  }
}

const handleReject = (request) => {
  selectedRequest.value = request
  showRejectModal.value = true
}

const confirmReject = async () => {
  if (!selectedRequest.value)
    return

  isProcessing.value = true
  try {
    const response = await friendshipApi.rejectFriendRequest(selectedRequest.value.id)
    if (response.data.status === 200) {
      const index = requests.value.findIndex(r => r.id === selectedRequest.value.id)
      if (index !== -1)
        requests.value[index].status = 'rejected'

      showRejectModal.value = false
      selectedRequest.value = null
    }
  } catch (error) {
    console.error('Failed to reject friend request:', error)
    alert('Failed to reject friend request. Please try again.')
  } finally {
    isProcessing.value = false
  }
}

const goToProfile = (userId) => {
  router.push({ name: 'Profile', params: { userId } })
}

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 500)
    loadRequests()
}

onMounted(() => {
  loadRequests()
  window.addEventListener('scroll', handleScroll)
})
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.friend-requests-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.requests-container {
  @apply max-w-3xl mx-auto px-4;
}

.requests-header {
  @apply bg-white rounded-xl shadow-md p-6 mb-6;
}

.requests-title {
  @apply text-3xl font-bold text-gray-900;
}

.requests-list {
  @apply space-y-4;
}

.request-item {
  @apply bg-white rounded-xl shadow-md p-4 flex items-center justify-between;
}

.request-info {
  @apply flex items-center space-x-4 cursor-pointer hover:opacity-80 transition-opacity flex-1;
}

.request-avatar {
  @apply w-16 h-16 rounded-full object-cover border-2 border-gray-200;
}

.request-details {
  @apply flex flex-col;
}

.request-username {
  @apply text-lg font-semibold text-gray-900;
}

.request-status {
  @apply text-sm text-green-600 mt-1;
}

.request-actions {
  @apply flex space-x-3;
}

.btn-friendship {
  @apply flex items-center justify-center space-x-2 px-6 py-2 rounded-lg transition-colors font-medium disabled:opacity-50;
}

.btn-accept {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}

.btn-cancel {
  @apply bg-gray-200 text-gray-700 hover:bg-gray-300;
}

.load-more {
  @apply flex justify-center py-8;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16 bg-white rounded-xl shadow-md;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-4;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-md;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-xl font-bold text-gray-900;
}

.modal-close {
  @apply text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-lg hover:bg-gray-100;
}

.modal-body {
  @apply p-6;
}

.modal-text {
  @apply text-gray-700 text-base leading-relaxed;
}

.modal-footer {
  @apply flex items-center justify-end space-x-3 p-6 border-t border-gray-200 bg-gray-50 rounded-b-2xl;
}

.btn-secondary {
  @apply px-6 py-2.5 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-all font-medium disabled:opacity-50 disabled:cursor-not-allowed;
}

.btn-primary {
  @apply px-6 py-2.5 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-all font-medium disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center min-w-[120px];
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
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-dialog,
.modal-leave-to .modal-dialog {
  transform: translateY(-20px);
}
</style>

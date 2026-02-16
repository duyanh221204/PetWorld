<template>
  <transition name="modal">
    <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">Reactions ({{ totalElements }})</h2>
          <div class="header-actions">
            <button @click="handleRefresh" class="icon-btn" title="Refresh">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
              </svg>
            </button>
            <button @click="close" class="icon-btn" title="Close">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="modal-body">
          <div v-if="isLoading" class="loading-container">
            <LoadingSpinner />
          </div>

          <div v-else-if="error" class="error-container">
            <p class="error-text">{{ error }}</p>
            <button @click="handleRefresh" class="retry-btn">Retry</button>
          </div>

          <div v-else-if="reactions.length === 0" class="empty-container">
            <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
            <p class="empty-text">No reactions yet</p>
          </div>

          <div v-else class="reactions-grid">
            <router-link
              v-for="(reaction, index) in reactions"
              :key="reaction.id"
              :to="`/profile/${reaction.userId}`"
              class="reaction-item"
            >
              <img :src="reaction.userAvatar || defaultAvatar" :alt="reaction.username" class="reaction-avatar" />
              <div class="reaction-info">
                <p class="reaction-username">{{ reaction.username }}</p>
                <p class="reaction-time">{{ formatTime(reaction.createdAt) }}</p>
              </div>
            </router-link>
          </div>
        </div>

        <div v-if="totalPages > 1" class="modal-footer">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="pagination-btn"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
            </svg>
            Previous
          </button>

          <span class="pagination-text">
            Page {{ currentPage + 1 }} of {{ totalPages }}
          </span>

          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage >= totalPages - 1"
            class="pagination-btn"
          >
            Next
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import { reactionApi } from '@/api/reaction'
import LoadingSpinner from './LoadingSpinner.vue'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  postId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['close'])

const reactions = ref([])
const isLoading = ref(false)
const error = ref(null)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 100

const fetchReactions = async (page = 0) => {
  isLoading.value = true
  error.value = null

  try {
    const response = await reactionApi.getReactionsByPostId(props.postId, page, pageSize)
    if (response.data.status === 200) {
      const data = response.data.data
      reactions.value = data.content || []
      totalPages.value = data.totalPages || 0
      totalElements.value = data.totalElements || 0
      currentPage.value = page
    }
  } catch (err) {
    console.error('Error fetching reactions:', err)
    error.value = 'Failed to load reactions'
  } finally {
    isLoading.value = false
  }
}

const goToPage = async (page) => {
  if (page >= 0 && page < totalPages.value)
    await fetchReactions(page)
}

const handleRefresh = async () => await fetchReactions(currentPage.value)

const close = () => emit('close')

const handleOverlayClick = () => close()

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

    if (days > 7) {
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    } else if (days > 0) {
      return `${days}d ago`
    } else if (hours > 0) {
      return `${hours}h ago`
    } else if (minutes > 0) {
      return `${minutes}m ago`
    } else {
      return 'Just now'
    }
  } catch (err) {
    return ''
  }
}

watch(() => props.isOpen, (isOpen) => {
  if (isOpen)
    fetchReactions(0)
  else {
    reactions.value = []
    currentPage.value = 0
  }
})
</script>

<style scoped>
.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
  will-change: opacity;
}

.modal-container {
  @apply bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4 max-h-[80vh] flex flex-col;
  will-change: transform, opacity;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-2xl font-bold text-gray-900;
}

.header-actions {
  @apply flex items-center gap-2;
}

.icon-btn {
  @apply text-gray-500 hover:text-gray-700 transition-colors p-2 hover:bg-gray-100 rounded-lg;
}

.modal-body {
  @apply p-6 overflow-y-auto;
}

.loading-container,
.error-container,
.empty-container {
  @apply flex flex-col items-center justify-center py-12;
}

.error-text {
  @apply text-gray-600 mb-3;
}

.retry-btn {
  @apply text-primary-600 hover:text-primary-700 font-medium;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-3;
}

.empty-text {
  @apply text-gray-500;
}

.reactions-grid {
  @apply grid grid-cols-2 gap-4;
}

.reaction-item {
  @apply flex items-center space-x-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors;
}

.reaction-avatar {
  @apply w-12 h-12 rounded-full object-cover border-2 border-gray-200;
}

.reaction-info {
  @apply flex-1 min-w-0;
}

.reaction-username {
  @apply font-medium text-gray-900 truncate;
}

.reaction-time {
  @apply text-xs text-gray-500;
}

.modal-footer {
  @apply flex items-center justify-between px-6 py-4 border-t border-gray-200 bg-gray-50;
}

.pagination-btn {
  @apply flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors;
}

.pagination-text {
  @apply text-sm text-gray-700 font-medium;
}

.modal-enter-active {
  transition: opacity 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-leave-active {
  transition: opacity 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container {
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-leave-active .modal-container {
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.95) translateY(-16px);
}
</style>

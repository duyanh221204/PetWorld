<template>
  <div class="post-item">
    <div class="post-menu">
      <button @click="toggleMenu" class="menu-button">
        <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
          <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z"/>
        </svg>
      </button>
      <div v-if="showMenu" class="menu-dropdown">
        <button @click="handleRefresh" class="menu-item">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
          </svg>
          Refresh
        </button>
        <template v-if="isOwnPost">
          <button @click="handleEdit" class="menu-item">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
            </svg>
            Edit
          </button>
          <button @click="handleDeleteClick" class="menu-item text-red-600 hover:bg-red-50">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
            </svg>
            Delete
          </button>
        </template>
      </div>
    </div>

    <div class="post-header">
      <div class="user-info" @click="goToProfile">
        <img 
          :src="post.userAvatar || defaultAvatar"
          :alt="post.username"
          class="user-avatar"
        />
        <div class="user-details">
          <div class="user-name-group">
            <span class="username">{{ post.username }}</span>
            <template v-if="post.visibility && post.visibility !== 'GROUP_ONLY'">
              <span class="visibility-separator">·</span>
              <svg v-if="post.visibility === 'PUBLIC'" class="visibility-icon" fill="currentColor" viewBox="0 0 20 20" title="Public">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM4.332 8.027a6.012 6.012 0 011.912-2.706C6.512 5.73 6.974 6 7.5 6A1.5 1.5 0 019 7.5V8a2 2 0 004 0 2 2 0 011.523-1.943A5.977 5.977 0 0116 10c0 .34-.028.675-.083 1H15a2 2 0 00-2 2v2.197A5.973 5.973 0 0110 16v-2a2 2 0 00-2-2 2 2 0 01-2-2 2 2 0 00-1.668-1.973z" clip-rule="evenodd"/>
              </svg>
              <svg v-else-if="post.visibility === 'FRIENDS_ONLY'" class="visibility-icon" fill="currentColor" viewBox="0 0 20 20" title="Friends Only">
                <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
              </svg>
              <svg v-else-if="post.visibility === 'PRIVATE'" class="visibility-icon" fill="currentColor" viewBox="0 0 20 20" title="Private">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/>
              </svg>
            </template>
            <template v-if="post.groupName">
              <svg class="arrow-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
              </svg>
              <span class="group-name" @click.stop="goToGroup">{{ post.groupName }}</span>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div v-if="post.postMediaResources && post.postMediaResources.length > 0" class="post-media">
      <div class="media-carousel">
        <img 
          :src="currentMedia.mediaUrl" 
          :alt="`Post image ${currentMediaIndex + 1}`"
          class="media-image"
        />
        <button 
          v-if="post.postMediaResources.length > 1"
          @click="prevMedia"
          class="nav-button nav-left"
          :disabled="currentMediaIndex === 0"
        >
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"/>
          </svg>
        </button>
        <button 
          v-if="post.postMediaResources.length > 1"
          @click="nextMedia"
          class="nav-button nav-right"
          :disabled="currentMediaIndex === post.postMediaResources.length - 1"
        >
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
          </svg>
        </button>
        <div v-if="post.postMediaResources.length > 1" class="media-indicators">
          <span 
            v-for="(_, index) in post.postMediaResources" 
            :key="index"
            :class="['indicator', { active: index === currentMediaIndex }]"
          ></span>
        </div>
      </div>
    </div>

    <div v-if="post.content" class="post-content">
      {{ post.content }}
    </div>

    <div class="post-actions">
      <button @click="toggleReaction" class="action-button">
        <svg 
          class="w-6 h-6" 
          :class="{ 'text-red-500 fill-current': post.isReactedByCurrentUser }"
          :fill="post.isReactedByCurrentUser ? 'currentColor' : 'none'"
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path 
            stroke-linecap="round" 
            stroke-linejoin="round" 
            stroke-width="2" 
            d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
          />
        </svg>
        <span>{{ post.reactionCount }}</span>
      </button>
      <button class="action-button">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
        </svg>
        <span>{{ post.commentCount }}</span>
      </button>
    </div>

    <div class="post-timestamps">
      <span class="timestamp">Created: {{ formatTime(post.createdAt) }}</span>
      <span v-if="post.updatedAt && post.updatedAt !== post.createdAt" class="timestamp">
        Updated: {{ formatTime(post.updatedAt) }}
      </span>
    </div>

    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal-content" @click.stop>
        <h2 class="modal-title">Confirm Delete</h2>
        <p class="modal-message">
          Are you sure you want to delete this post? This action cannot be undone.
        </p>
        <div class="modal-actions">
          <button @click="showDeleteModal = false" class="btn-modal-cancel" :disabled="isDeleting">
            Cancel
          </button>
          <button @click="confirmDelete" class="btn-modal-confirm" :disabled="isDeleting">
            <LoadingSpinner v-if="isDeleting" size="sm" />
            <span v-else>Delete</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import defaultAvatar from '@/assets/images/default-avatar.png'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { postApi } from '@/api/post'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['refresh', 'toggleReaction', 'delete'])

const router = useRouter()
const { user } = useAuth()

const showMenu = ref(false)
const currentMediaIndex = ref(0)
const showDeleteModal = ref(false)
const isDeleting = ref(false)

const currentMedia = computed(() => {
  if (props.post.postMediaResources && props.post.postMediaResources.length > 0)
    return props.post.postMediaResources[currentMediaIndex.value]
  return null
})

const isOwnPost = computed(() => user.value?.id === props.post.userId)

const toggleMenu = () => showMenu.value = !showMenu.value

const handleRefresh = () => {
  showMenu.value = false
  emit('refresh', props.post.id)
}

const handleEdit = () => {
  showMenu.value = false
  router.push({
    name: 'CreatePost',
    query: { editPostId: props.post.id }
  })
}

const handleDeleteClick = () => {
  showMenu.value = false
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  isDeleting.value = true
  try {
    const response = await postApi.deletePost(props.post.id)
    if (response.data.status === 200) {
      showDeleteModal.value = false
      emit('delete', props.post.id)
      window.location.reload()
    }
  } catch (error) {
    alert('Failed to delete post. Please try again.')
    throw error
  } finally {
    isDeleting.value = false
  }
}

const toggleReaction = () => emit('toggleReaction', props.post.id)

const prevMedia = () => {
  if (currentMediaIndex.value > 0)
    --currentMediaIndex.value
}

const nextMedia = () => {
  if (currentMediaIndex.value < props.post.postMediaResources.length - 1)
    ++currentMediaIndex.value
}

const goToProfile = () => {
  if (props.post.userId)
    router.push({ name: 'Profile', params: { userId: props.post.userId } })
}

const goToGroup = () => {
  // TODO: Navigate to group
  console.log('Navigate to group:', props.post.groupId)
}

const formatTime = (timestamp) => {
  if (!timestamp)
    return ''

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
}

const handleClickOutside = (event) => {
  if (showMenu.value && !event.target.closest('.post-menu'))
    showMenu.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.post-item {
  @apply bg-white rounded-xl shadow-md p-4 mb-4 relative;
}

.post-menu {
  @apply absolute top-4 right-4 z-10;
}

.menu-button {
  @apply text-gray-500 hover:text-gray-700 p-1 rounded-full hover:bg-gray-100 transition-colors;
}

.menu-dropdown {
  @apply absolute right-0 mt-2 w-40 bg-white rounded-lg shadow-lg border border-gray-200 py-1;
}

.menu-item {
  @apply w-full px-4 py-2 text-left text-gray-700 hover:bg-gray-100 flex items-center space-x-2 transition-colors;
}

.post-header {
  @apply mb-3;
}

.user-info {
  @apply flex items-center cursor-pointer;
}

.user-avatar {
  @apply w-12 h-12 rounded-full object-cover border-2 border-gray-200;
}

.user-details {
  @apply ml-3;
}

.user-name-group {
  @apply flex items-center space-x-2;
}

.username {
  @apply font-semibold text-gray-900 hover:underline;
}

.visibility-separator {
  @apply text-gray-400 mx-1;
}

.visibility-icon {
  @apply w-4 h-4 text-gray-500;
}

.arrow-icon {
  @apply w-4 h-4 text-gray-500;
}

.group-name {
  @apply text-primary-600 font-medium hover:underline cursor-pointer;
}

.post-media {
  @apply mb-3 -mx-4;
}

.media-carousel {
  @apply relative;
}

.media-image {
  @apply w-full max-h-96 object-contain bg-gray-100;
}

.nav-button {
  @apply absolute top-1/2 transform -translate-y-1/2 bg-white/80 hover:bg-white rounded-full p-2 shadow-lg transition-all disabled:opacity-30 disabled:cursor-not-allowed;
}

.nav-left {
  @apply left-2;
}

.nav-right {
  @apply right-2;
}

.media-indicators {
  @apply absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-2;
}

.indicator {
  @apply w-2 h-2 rounded-full bg-white/50 transition-all;
}

.indicator.active {
  @apply bg-white w-6;
}

.post-content {
  @apply text-gray-800 mb-3 whitespace-pre-wrap;
}

.post-actions {
  @apply flex items-center space-x-6 mb-2 border-t border-gray-200 pt-3;
}

.action-button {
  @apply flex items-center space-x-2 text-gray-600 hover:text-primary-600 transition-colors;
}

.post-timestamps {
  @apply flex flex-col text-xs text-gray-500 space-y-1;
}

.timestamp {
  @apply text-gray-400;
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
  @apply flex items-center justify-center px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 min-w-[100px];
}
</style>

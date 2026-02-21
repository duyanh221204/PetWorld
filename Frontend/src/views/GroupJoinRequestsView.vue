<template>
  <div class="requests-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="requests-container">
        <div class="page-header">
          <button @click="goBack" class="back-button">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
            Back
          </button>
          <h1 class="page-title">Join Requests</h1>
        </div>

        <LoadingSpinner v-if="isLoading" size="lg" class="my-8" />

        <template v-else>
          <div class="requests-section">
            <div v-if="requests.length === 0" class="empty-state">
              <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
              </svg>
              <p class="empty-text">No join requests</p>
            </div>

            <div v-else class="requests-list">
              <div
                v-for="request in requests"
                :key="request.id"
                :class="['request-item', { highlight: highlightedRequestId === request.id }]"
                @click="viewRequest(request)"
              >
                <img
                  :src="request.senderAvatar || defaultAvatar"
                  :alt="request.senderUsername"
                  class="request-avatar"
                  @error="(e) => e.target.src = defaultAvatar"
                />
                <div class="request-info">
                  <h3 class="request-username">{{ request.senderUsername }}</h3>
                  <p class="request-time">{{ formatTime(request.submittedAt) }}</p>
                </div>
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
              </div>
            </div>

            <div v-if="totalPages > 1" class="pagination">
              <button
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 0"
                class="pagination-btn"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
              </button>

              <div class="pagination-pages">
                <button
                  v-for="page in visiblePages"
                  :key="page"
                  @click="goToPage(page)"
                  :class="['pagination-page', { active: currentPage === page }]"
                >
                  {{ page + 1 }}
                </button>
              </div>

              <button
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage >= totalPages - 1"
                class="pagination-btn"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
              </button>
            </div>
          </div>
        </template>
      </div>

      <GroupRightSidebar ref="groupSidebarRef" :groupId="groupId" />
    </main>

    <AppFooter />

    <GroupJoinRequestModal
      :show="showRequestModal"
      :group-id="groupId"
      :request="selectedRequest"
      @close="closeRequestModal"
      @approved="handleApproved"
      @rejected="handleRejected"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import defaultAvatar from '@/assets/images/default-avatar.png'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import GroupRightSidebar from '@/components/ui/group/GroupRightSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import GroupJoinRequestModal from '@/components/ui/group/GroupJoinRequestModal.vue'
import { groupJoinRequestApi } from '@/api/groupJoinRequest'

const route = useRoute()
const router = useRouter()

const groupId = computed(() => parseInt(route.params.groupId))
const highlightRequestId = computed(() => route.query.highlight ? parseInt(route.query.highlight) : null)

const groupSidebarRef = ref(null)

const isLoading = ref(true)
const requests = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const showRequestModal = ref(false)
const selectedRequest = ref(null)
const highlightedRequestId = ref(null)

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(0, currentPage.value - 2)
  let end = Math.min(totalPages.value - 1, start + maxVisible - 1)

  if (end - start < maxVisible - 1)
    start = Math.max(0, end - maxVisible + 1)

  for (let i = start; i <= end; ++i)
    pages.push(i)

  return pages
})

const loadRequests = async (page = 0) => {
  isLoading.value = true
  try {
    const response = await groupJoinRequestApi.getGroupJoinRequests(groupId.value, page, 100)
    if (response.data.status === 200) {
      const data = response.data.data
      requests.value = data.content || []
      currentPage.value = data.number
      totalPages.value = data.totalPages
    }
  } catch (error) {
    console.error('Error loading requests:', error)
  } finally {
    isLoading.value = false
  }
}

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    loadRequests(page)
    const query = { ...route.query }
    delete query.highlightId
    router.replace({ query })
  }
}

const viewRequest = (request) => {
  selectedRequest.value = request
  showRequestModal.value = true
}

const closeRequestModal = () => {
  showRequestModal.value = false
  selectedRequest.value = null
}

const handleApproved = (requestId) => {
  requests.value = requests.value.filter(r => r.id !== requestId)
  closeRequestModal()

  if (groupSidebarRef.value)
    groupSidebarRef.value.loadJoinRequestsCount()
}

const handleRejected = (requestId) => {
  requests.value = requests.value.filter(r => r.id !== requestId)
  closeRequestModal()

  if (groupSidebarRef.value)
    groupSidebarRef.value.loadJoinRequestsCount()
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

const goBack = () => router.push({ name: 'GroupDetail', params: { groupId: groupId.value } })

onMounted(async () => {
  if (highlightRequestId.value) {
    const pageFromQuery = route.query.page ? parseInt(route.query.page) : null
    
    if (pageFromQuery !== null) {
      await loadRequests(pageFromQuery)
      highlightedRequestId.value = highlightRequestId.value
      setTimeout(() => highlightedRequestId.value = null, 5000)
    } else {
      try {
        const response = await groupJoinRequestApi.getRequestPage(groupId.value, highlightRequestId.value, 100)
        if (response.data.status === 200) {
          const { page } = response.data.data
          await loadRequests(page)

          highlightedRequestId.value = highlightRequestId.value

          setTimeout(() => highlightedRequestId.value = null, 5000)
        }
      } catch (error) {
        console.error('Error getting request page:', error)
        await loadRequests(0)
      }
    }
  } else
    await loadRequests(0)
})

watch(() => route.query.reload, async (newVal) => {
  if (newVal) {
    const pageFromQuery = route.query.page ? parseInt(route.query.page) : 0
    await loadRequests(pageFromQuery)
    
    if (highlightRequestId.value) {
      highlightedRequestId.value = highlightRequestId.value
      setTimeout(() => highlightedRequestId.value = null, 5000)
    }
  }
})

</script>

<style scoped>
.requests-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.requests-container {
  @apply max-w-3xl mx-auto px-4 space-y-6;
}

.page-header {
  @apply bg-white rounded-xl shadow-sm p-6;
}

.back-button {
  @apply flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors mb-4;
}

.page-title {
  @apply text-2xl font-bold text-gray-900;
}

.requests-section {
  @apply bg-white rounded-xl shadow-sm p-6;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-3;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

.requests-list {
  @apply space-y-3;
}

.request-item {
  @apply flex items-center gap-4 p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:bg-gray-50 transition-all cursor-pointer;
}

.request-item.highlight {
  @apply border-primary-500 bg-primary-50 animate-pulse;
  animation: highlight 2s ease-in-out 3;
}

@keyframes highlight {
  0%, 100% { background-color: rgb(239 246 255); }
  50% { background-color: rgb(219 234 254); }
}

.request-avatar {
  @apply w-12 h-12 rounded-full object-cover border-2 border-gray-200;
}

.request-info {
  @apply flex-1 min-w-0;
}

.request-username {
  @apply font-semibold text-gray-900 truncate;
}

.request-time {
  @apply text-sm text-gray-500;
}

.pagination {
  @apply flex items-center justify-center gap-2 mt-6 pt-6 border-t border-gray-200;
}

.pagination-btn {
  @apply p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent;
}

.pagination-pages {
  @apply flex items-center gap-1;
}

.pagination-page {
  @apply px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors;
}

.pagination-page.active {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}
</style>

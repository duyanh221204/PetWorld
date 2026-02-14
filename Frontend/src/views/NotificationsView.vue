<template>
  <div class="notifications-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="notifications-container">
        <div class="page-header">
          <h1 class="page-title">Notifications</h1>
          <button
            v-if="allNotifications.length > 0"
            @click="handleMarkAllRead"
            class="mark-all-read-btn"
            :disabled="unreadCount === 0"
          >
            Mark all as read
          </button>
        </div>

        <div class="page-content">
          <div v-if="isLoading && currentPage === 0" class="loading-container">
            <LoadingSpinner />
          </div>

          <div v-else-if="error" class="error-container">
            <p class="error-text">{{ error }}</p>
            <button @click="handleRetry" class="retry-btn">Retry</button>
          </div>

          <div v-else-if="allNotifications.length === 0" class="empty-container">
            <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
            <p class="empty-text">You don't have any notifications yet</p>
            <p class="empty-subtext">When you get notifications, they'll show up here</p>
          </div>

          <div v-else>
            <div class="notifications-list">
              <NotificationItem
                v-for="notification in allNotifications"
                :key="notification.id"
                :notification="notification"
                @click="handleNotificationClick(notification)"
              />
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
                Previous
              </button>

              <div class="pagination-info">
                <span class="pagination-text">
                  Page {{ currentPage + 1 }} of {{ totalPages }}
                </span>
              </div>

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

            <div v-if="isLoading && currentPage > 0" class="pagination-loading">
              <LoadingSpinner />
            </div>
          </div>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNotifications } from '@/composables/useNotifications'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import NotificationItem from '@/components/ui/notification/NotificationItem.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const { unreadCount, isLoading, error, fetchNotifications, markAllAsRead, markAsRead } = useNotifications()

const allNotifications = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 50

const loadNotifications = async (page = 0) => {
  const result = await fetchNotifications(page, pageSize)
  allNotifications.value = result.content || []
  totalPages.value = result.totalPages || 0
  totalElements.value = result.totalElements || 0
  currentPage.value = page

  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToPage = async (page) => {
  if (page >= 0 && page < totalPages.value)
    await loadNotifications(page)
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    allNotifications.value = allNotifications.value.map(n => ({ ...n, isRead: true }))
  } catch (err) {
    console.error('Error marking all as read:', err)
  }
}

const handleNotificationClick = async (notification) => {
  if (!notification.isRead) {
    await markAsRead(notification.id)
    notification.isRead = true
  }
}

const handleRetry = async () => await loadNotifications(currentPage.value)

onMounted(async () => await loadNotifications(0))
</script>

<style scoped>
.notifications-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-6;
  @apply flex gap-6 relative;
}

.notifications-container {
  @apply flex-1 max-w-2xl mx-auto;
  width: 100%;
}

.page-header {
  @apply bg-white rounded-xl shadow-sm px-6 py-4 mb-4 sticky top-0 z-10;
  @apply flex items-center justify-between;
}

.page-title {
  @apply text-2xl font-bold text-gray-900;
}

.mark-all-read-btn {
  @apply text-sm font-medium text-primary-600 hover:text-primary-700 px-4 py-2 rounded-lg hover:bg-primary-50 transition-colors disabled:text-gray-400 disabled:hover:bg-transparent disabled:cursor-not-allowed;
}

.page-content {
  @apply bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden;
}

.loading-container,
.error-container,
.empty-container {
  @apply flex flex-col items-center justify-center py-16 px-4;
}

.error-text {
  @apply text-gray-600 mb-3;
}

.retry-btn {
  @apply text-primary-600 hover:text-primary-700 font-medium;
}

.empty-icon {
  @apply w-20 h-20 text-gray-300 mb-4;
}

.empty-text {
  @apply text-lg font-medium text-gray-700 mb-2;
}

.empty-subtext {
  @apply text-sm text-gray-500;
}

.notifications-list {
  @apply divide-y divide-gray-100;
}

.pagination {
  @apply flex items-center justify-between px-4 py-4 border-t border-gray-200 bg-gray-50;
}

.pagination-btn {
  @apply flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-white transition-colors;
}

.pagination-info {
  @apply flex items-center gap-2;
}

.pagination-text {
  @apply text-sm text-gray-700;
}

.pagination-loading {
  @apply flex justify-center py-4;
}
</style>

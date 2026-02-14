<template>
  <div class="notification-sidebar">
    <div class="sidebar-header">
      <h2 class="sidebar-title">Recent Notifications</h2>
    </div>

    <div class="sidebar-body">
      <div v-if="isLoading" class="loading-container">
        <LoadingSpinner />
      </div>

      <div v-else-if="error" class="error-container">
        <p class="error-text">{{ error }}</p>
        <button @click="handleRetry" class="retry-btn">Retry</button>
      </div>

      <div v-else-if="latestNotifications.length === 0" class="empty-container">
        <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
        </svg>
        <p class="empty-text">No notifications</p>
      </div>

      <div v-else class="notifications-list">
        <NotificationItem
          v-for="notification in latestNotifications"
          :key="notification.id"
          :notification="notification"
          @click="handleNotificationClick(notification)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useNotifications } from '@/composables/useNotifications.js'
import NotificationItem from './NotificationItem.vue'
import LoadingSpinner from '../LoadingSpinner.vue'

const { notifications, isLoading, error, fetchLatestNotifications, markAsRead } = useNotifications()

const latestNotifications = computed(() => notifications.value.slice(0, 5))

const handleNotificationClick = async (notification) => {
  if (!notification.isRead)
    await markAsRead(notification.id)
}

const handleRetry = async () => await fetchLatestNotifications(5)

onMounted(async () => {
  if (notifications.value.length === 0)
    await fetchLatestNotifications(5)
})
</script>

<style scoped>
.notification-sidebar {
  @apply bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden;
}

.sidebar-header {
  @apply px-4 py-3 border-b border-gray-200 bg-gray-50;
}

.sidebar-title {
  @apply text-lg font-semibold text-gray-900;
}

.sidebar-body {
  @apply min-h-[200px];
}

.loading-container,
.error-container,
.empty-container {
  @apply flex flex-col items-center justify-center py-12 px-4;
}

.error-text {
  @apply text-gray-600 text-sm mb-3;
}

.retry-btn {
  @apply text-primary-600 hover:text-primary-700 font-medium text-sm;
}

.empty-icon {
  @apply w-12 h-12 text-gray-300 mb-2;
}

.empty-text {
  @apply text-gray-500 text-sm;
}

.notifications-list {
  @apply divide-y divide-gray-100;
}
</style>

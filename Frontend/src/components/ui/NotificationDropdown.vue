<template>
  <div class="notification-dropdown" ref="dropdownRef">
    <button
      @click="toggleDropdown"
      class="notification-button"
      :class="{ 'has-unread': unreadCount > 0 }"
    >
      <svg class="notification-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
      </svg>
      <span v-if="unreadCount > 0" class="unread-badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <transition name="dropdown">
      <div v-if="isOpen" class="dropdown-panel">
        <div class="dropdown-header">
          <h3 class="dropdown-title">Notifications</h3>
          <button
            v-if="notifications.length > 0"
            @click="handleMarkAllRead"
            class="mark-read-btn"
            :disabled="unreadCount === 0"
          >
            Mark all as read
          </button>
        </div>

        <div class="dropdown-body">
          <div v-if="isLoading" class="loading-container">
            <LoadingSpinner />
          </div>

          <div v-else-if="error" class="error-container">
            <p class="error-text">{{ error }}</p>
            <button @click="handleRetry" class="retry-btn">Retry</button>
          </div>

          <div v-else-if="notifications.length === 0" class="empty-container">
            <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
            <p class="empty-text">No notifications yet</p>
          </div>

          <div v-else class="notifications-list">
            <NotificationItem
              v-for="notification in notifications"
              :key="notification.id"
              :notification="notification"
              @click="handleNotificationClick(notification)"
            />
          </div>
        </div>

        <div v-if="notifications.length > 0" class="dropdown-footer">
          <router-link
            to="/notifications"
            class="see-more-link"
          >
            See all notifications
          </router-link>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useNotifications } from '@/composables/useNotifications'
import NotificationItem from './NotificationItem.vue'
import LoadingSpinner from './LoadingSpinner.vue'

const route = useRoute()
const { notifications, unreadCount, isLoading, error, fetchLatestNotifications, markAllAsRead, markAsRead } = useNotifications()

const isOpen = ref(false)
const dropdownRef = ref(null)

const toggleDropdown = async () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && notifications.value.length === 0)
    await fetchLatestNotifications(15)
}

const closeDropdown = () => isOpen.value = false

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
  } catch (err) {
    console.error('Error marking all as read:', err)
  }
}

const handleNotificationClick = async (notification) => {
  if (!notification.isRead)
    await markAsRead(notification.id)
  closeDropdown()
}

const handleRetry = async () => await fetchLatestNotifications(15)

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target))
    closeDropdown()
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))

// đóng dropdown khi route thay đổi (khi navigate sang trang notifications)
watch(() => route.path, () => closeDropdown())
</script>

<style scoped>
.notification-dropdown {
  @apply relative;
}

.notification-button {
  @apply relative p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg transition-colors;
}

.notification-button.has-unread {
  @apply text-primary-600;
}

.notification-icon {
  @apply w-6 h-6;
}

.unread-badge {
  @apply absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full min-w-[20px] h-5 flex items-center justify-center px-1;
}

.dropdown-panel {
  @apply absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-gray-200 overflow-hidden z-50;
}

.dropdown-header {
  @apply flex items-center justify-between px-4 py-3 border-b border-gray-200 bg-gray-50 sticky top-0 z-10;
}

.dropdown-title {
  @apply text-lg font-semibold text-gray-900;
}

.mark-read-btn {
  @apply text-sm text-primary-600 hover:text-primary-700 font-medium disabled:text-gray-400 disabled:cursor-not-allowed;
}

.dropdown-body {
  @apply max-h-[500px] overflow-y-auto;
}

.loading-container,
.error-container,
.empty-container {
  @apply flex flex-col items-center justify-center py-12 px-4;
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

.notifications-list {
  @apply divide-y divide-gray-100;
}

.dropdown-footer {
  @apply border-t border-gray-200 bg-gray-50 sticky bottom-0;
}

.see-more-link {
  @apply block text-center py-3 text-sm font-medium text-primary-600 hover:text-primary-700 hover:bg-gray-100 transition-colors;
}

.dropdown-enter-active,
.dropdown-leave-active {
  @apply transition-all duration-200;
}

.dropdown-enter-from,
.dropdown-leave-to {
  @apply opacity-0 transform scale-95 -translate-y-2;
}

.dropdown-enter-to,
.dropdown-leave-from {
  @apply opacity-100 transform scale-100 translate-y-0;
}
</style>

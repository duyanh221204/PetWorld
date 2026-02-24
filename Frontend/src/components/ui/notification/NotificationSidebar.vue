<template>
  <div class="notification-sidebar">
    <div class="sidebar-body">
      <!-- Notifications Section -->
      <div class="section">
        <button 
          @click="toggleNotifications" 
          class="section-header"
        >
          <div class="section-title">
            <svg class="section-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
            <span>Recent Notifications</span>
          </div>
          <svg 
            class="chevron" 
            :class="{ 'rotate-180': showNotifications }"
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
          </svg>
        </button>

        <transition name="slide">
          <div v-show="showNotifications" class="section-content">
            <div v-if="isLoadingNotifications" class="loading-container">
              <LoadingSpinner />
            </div>

            <div v-else-if="notificationError" class="error-container">
              <p class="error-text">{{ notificationError }}</p>
              <button @click="handleRetryNotifications" class="retry-btn">Retry</button>
            </div>

            <div v-else-if="latestNotifications.length === 0" class="empty-container">
              <p class="empty-text">No notifications</p>
            </div>

            <div v-else class="items-list">
              <NotificationItem
                v-for="notification in latestNotifications"
                :key="notification.id"
                :notification="notification"
                @click="handleNotificationClick(notification)"
              />
            </div>
          </div>
        </transition>
      </div>

      <!-- Recent Messages Section -->
      <div class="section">
        <button 
          @click="toggleMessages" 
          class="section-header"
        >
          <div class="section-title">
            <svg class="section-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
            <span>Recent Messages</span>
          </div>
          <svg 
            class="chevron" 
            :class="{ 'rotate-180': showMessages }"
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
          </svg>
        </button>

        <transition name="slide">
          <div v-show="showMessages" class="section-content">
            <div v-if="isLoadingMessages" class="loading-container">
              <LoadingSpinner />
            </div>

            <div v-else-if="latestMessages.length === 0" class="empty-container">
              <p class="empty-text">No messages</p>
            </div>

            <div v-else class="items-list">
              <ChatItem
                v-for="chat in latestMessages"
                :key="chat.id"
                :chat="chat"
                @click="handleMessageClick(chat)"
                compact
              />
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotifications } from '@/composables/useNotifications.js'
import { useChat } from '@/composables/useChat.js'
import { groupJoinRequestApi } from '@/api/groupJoinRequest'
import NotificationItem from './NotificationItem.vue'
import ChatItem from '../chat/ChatItem.vue'
import LoadingSpinner from '../LoadingSpinner.vue'

const router = useRouter()

const { 
  notifications, 
  isLoading: isLoadingNotifications, 
  error: notificationError, 
  fetchLatestNotifications, 
  markAsRead
} = useNotifications()

const { chats, isLoading: isLoadingMessages, fetchLatestChats } = useChat()

const showNotifications = ref(true)
const showMessages = ref(true)

const latestNotifications = computed(() => notifications.value.slice(0, 3))
const latestMessages = computed(() => chats.value.slice(0, 3))

const toggleNotifications = () => showNotifications.value = !showNotifications.value

const toggleMessages = () => showMessages.value = !showMessages.value

const handleNotificationClick = async (notification) => {
  if (!notification.isRead)
    await markAsRead(notification.id)

  if (notification.type === 'GROUP_JOIN_REQUEST_RECEIVED') {
    try {
      const pageResponse = await groupJoinRequestApi.getRequestPage(
        notification.groupId,
        notification.groupJoinRequestId,
        100
      )
      
      if (pageResponse.data.status === 200) {
        const pageNum = pageResponse.data.data.page
        const currentRoute = router.currentRoute.value

        if (currentRoute.name === 'GroupJoinRequests' && currentRoute.params.groupId === String(notification.groupId))
          await router.push({
            name: 'GroupJoinRequests',
            params: { groupId: String(notification.groupId) },
            query: { 
              page: String(pageNum),
              highlight: String(notification.groupJoinRequestId),
              reload: String(Date.now())
            }
          })
        else
          await router.push({
            name: 'GroupJoinRequests',
            params: { groupId: String(notification.groupId) },
            query: { 
              page: String(pageNum),
              highlight: String(notification.groupJoinRequestId)
            }
          })
      }
    } catch (error) {
      await router.push({
        name: 'GroupJoinRequests',
        params: { groupId: String(notification.groupId) }
      })
    }
  } else if (notification.type === 'GROUP_JOIN_REQUEST_ACCEPTED') {
    const currentRoute = router.currentRoute.value
    if (currentRoute.name === 'GroupDetail' && currentRoute.params.groupId === String(notification.groupId))
      window.location.reload()
    else
      await router.push({
        name: 'GroupDetail',
        params: { groupId: String(notification.groupId) }
      })
  }
}

const handleMessageClick = async (chat) => {
  await router.push({
    name: 'Chats',
    params: { chatId: chat.id }
  })
}

const handleRetryNotifications = async () => {
  await fetchLatestNotifications(3)
}

onMounted(async () => {
  if (notifications.value.length === 0)
    await fetchLatestNotifications(3)
  
  if (chats.value.length === 0)
    await fetchLatestChats(3)
})
</script>

<style scoped>
.notification-sidebar {
  @apply bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden;
}

.sidebar-body {
  @apply divide-y divide-gray-100;
}

.section {
  @apply bg-white;
}

.section-header {
  @apply w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors cursor-pointer;
}

.section-title {
  @apply flex items-center gap-2 font-medium text-gray-700;
}

.section-icon {
  @apply w-5 h-5;
}

.chevron {
  @apply w-5 h-5 text-gray-400 transition-transform;
}

.section-content {
  @apply border-t border-gray-100;
}

.loading-container,
.error-container,
.empty-container {
  @apply flex flex-col items-center justify-center py-8 px-4;
}

.error-text {
  @apply text-gray-600 text-sm mb-3;
}

.retry-btn {
  @apply text-primary-600 hover:text-primary-700 font-medium text-sm;
}

.empty-text {
  @apply text-gray-500 text-sm;
}

.items-list {
  @apply divide-y divide-gray-100;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>

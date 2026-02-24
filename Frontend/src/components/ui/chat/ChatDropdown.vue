<template>
  <div class="chat-dropdown-container" ref="dropdownRef">
    <button
      @click="toggleDropdown"
      class="chat-button"
    >
      <svg class="chat-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
      </svg>
      <span v-if="unreadCount > 0" class="unread-badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <transition name="dropdown">
      <div v-if="isOpen" class="dropdown-panel">
        <div class="dropdown-header">
          <h3 class="dropdown-title">Messages</h3>
        </div>

        <div class="dropdown-body">
          <LoadingSpinner v-if="isLoading" size="md" class="my-4" />
          
          <div v-else-if="chats.length === 0" class="empty-state">
            <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
            <p class="empty-text">No messages yet</p>
          </div>

          <div v-else class="chats-list">
            <ChatItem
              v-for="chat in displayChats"
              :key="chat.id"
              :chat="chat"
              @click="handleChatClick(chat)"
            />
          </div>
        </div>

        <div class="dropdown-footer">
          <router-link to="/chats" class="view-all-link">
            View all messages
          </router-link>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChat } from '@/composables/useChat'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import ChatItem from './ChatItem.vue'

const router = useRouter()
const { chats, unreadCount, isLoading, fetchLatestChats } = useChat()

const isOpen = ref(false)
const dropdownRef = ref(null)

const displayChats = computed(() => chats.value.slice(0, 15))

const toggleDropdown = async () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && chats.value.length === 0)
    await fetchLatestChats(15)
}

const handleChatClick = async (chat) => {
  isOpen.value = false
  await router.push({ 
    name: 'Chats', 
    params: { chatId: String(chat.id) }
  })
}

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target))
    isOpen.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.chat-dropdown-container {
  @apply relative;
}

.chat-button {
  @apply relative p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg transition-colors;
}

.chat-icon {
  @apply w-6 h-6;
}

.unread-badge {
  @apply absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full min-w-[20px] h-5 flex items-center justify-center px-1;
}

.dropdown-panel {
  @apply absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-gray-200 overflow-hidden z-50;
  max-height: 600px;
  display: flex;
  flex-direction: column;
}

.dropdown-header {
  @apply p-4 border-b border-gray-200 bg-gray-50 sticky top-0 z-10;
}

.dropdown-title {
  @apply text-lg font-bold text-gray-900;
}

.dropdown-body {
  @apply flex-1 overflow-y-auto;
  max-height: 480px;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-12 px-4;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-3;
}

.empty-text {
  @apply text-gray-500;
}

.chats-list {
  @apply divide-y divide-gray-100;
}

.dropdown-footer {
  @apply p-3 border-t border-gray-200 bg-gray-50 text-center sticky bottom-0;
}

.view-all-link {
  @apply block text-sm font-medium text-primary-600 hover:text-primary-700 hover:bg-gray-100 transition-colors py-1;
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

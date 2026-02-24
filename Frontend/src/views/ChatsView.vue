<template>
  <div class="chats-view">
    <AppHeader />
    
    <div class="chats-main">
      <div class="chat-list-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">Messages</h1>
      </div>

      <div class="sidebar-body">
        <div v-if="isLoading && chats.length === 0" class="loading-container">
          <LoadingSpinner />
        </div>

        <div v-else-if="chats.length === 0" class="empty-container">
          <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
          </svg>
          <p class="empty-text">No messages yet</p>
          <p class="empty-subtext">Start a conversation by visiting a user's profile</p>
        </div>

        <div v-else class="chat-list" @scroll="handleScroll" ref="chatListRef">
          <ChatItem
            v-for="chat in chats"
            :key="chat.id"
            :chat="chat"
            :active="selectedChatId === chat.id"
            @click="selectChat(chat.id)"
          />
          
          <div v-if="isLoading && chats.length > 0" class="loading-more">
            <LoadingSpinner size="small" />
          </div>
        </div>
      </div>
    </div>

    <div class="chat-window-container">
      <ChatWindow
        v-if="selectedChatId || newChatRecipientId"
        :chatId="selectedChatId"
        :recipientId="newChatRecipientId"
        @close="handleCloseChat"
        @chatCreated="handleChatCreated"
      />

      <div v-else class="no-chat-selected">
        <svg class="placeholder-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
        </svg>
        <p class="placeholder-text">Select a conversation to start messaging</p>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useChat } from '@/composables/useChat'
import ChatItem from '@/components/ui/chat/ChatItem.vue'
import ChatWindow from '@/components/ui/chat/ChatWindow.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const { chats, isLoading, fetchChats, createOrGetChat } = useChat()

const chatListRef = ref(null)
const currentPage = ref(0)
const totalPages = ref(1)
const newChatRecipientId = ref(null)

const selectedChatId = computed(() => {
  const chatId = route.params.chatId
  if (chatId && chatId !== 'new')
    return parseInt(chatId)
  return null
})

const selectChat = async (chatId) => {
  await router.push({
    name: 'Chats',
    params: { chatId: String(chatId) }
  })
}

const handleCloseChat = async () => {
  newChatRecipientId.value = null
  await router.push({ name: 'Chats' })
}

const handleChatCreated = async (chatId) => {
  newChatRecipientId.value = null
  await router.replace({
    name: 'Chats',
    params: { chatId: String(chatId) }
  })
}

const loadMore = async () => {
  if (isLoading.value || currentPage.value >= totalPages.value - 1)
    return

  ++currentPage.value
  const result = await fetchChats(currentPage.value, 50)
  totalPages.value = result.totalPages
}

const handleScroll = (event) => {
  const { scrollTop, scrollHeight, clientHeight } = event.target
  const bottom = scrollHeight - scrollTop - clientHeight < 100

  if (bottom)
    loadMore()
}

onMounted(async () => {
  if (chats.value.length === 0) {
    const result = await fetchChats(0, 50)
    totalPages.value = result.totalPages
  }

  if (route.params.chatId === 'new' && route.query.recipientId) {
    const recipientId = parseInt(route.query.recipientId)
    const chatId = await createOrGetChat(recipientId)
    
    if (chatId)
      await router.replace({
        name: 'Chats',
        params: { chatId: String(chatId) }
      })
    else
      newChatRecipientId.value = recipientId
  }
})

// theo dõi thay đổi của route để tạo chat mới
watch(() => route.params.chatId, (newChatId) => {
  if (newChatId === 'new' && route.query.recipientId)
    newChatRecipientId.value = parseInt(route.query.recipientId)
  else
    newChatRecipientId.value = null
})
</script>

<style scoped>
.chats-view {
  @apply flex flex-col min-h-screen bg-gray-50;
}

.chats-main {
  @apply flex h-[calc(100vh-4rem)] bg-gray-50;
}

.chat-list-sidebar {
  @apply w-80 bg-white border-r border-gray-200 flex flex-col flex-shrink-0;
}

.sidebar-header {
  @apply px-4 py-4 border-b border-gray-200;
}

.sidebar-title {
  @apply text-xl font-bold text-gray-900;
}

.sidebar-body {
  @apply flex-1 overflow-y-auto;
}

.loading-container,
.empty-container {
  @apply flex flex-col items-center justify-center h-full px-4 py-12;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-4;
}

.empty-text {
  @apply text-gray-600 font-medium mb-1;
}

.empty-subtext {
  @apply text-gray-500 text-sm text-center;
}

.chat-list {
  @apply divide-y divide-gray-100 overflow-y-auto;
  max-height: calc(100vh - 8rem);
}

.loading-more {
  @apply flex justify-center py-4;
}

.chat-window-container {
  @apply flex-1 bg-gray-50;
}

.no-chat-selected {
  @apply flex flex-col items-center justify-center text-center px-4 h-full;
}

.placeholder-icon {
  @apply w-20 h-20 text-gray-300 mb-4;
}

.placeholder-text {
  @apply text-gray-500 text-lg;
}
</style>

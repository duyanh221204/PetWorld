<template>
  <div class="chat-window">
    <div class="chat-header">
      <div class="user-info" @click="handleProfileClick">
        <img
          :src="otherUser?.avatar || defaultAvatar"
          :alt="otherUser?.username"
          class="avatar"
          @error="(e) => e.target.src = defaultAvatar"
        />
        <div class="user-details">
          <h3 class="username">{{ otherUser?.username || 'Loading...' }}</h3>
          <p v-if="isTyping" class="status typing">Typing...</p>
        </div>
      </div>
      
      <button @click="handleClose" class="close-btn">
        <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    </div>

    <div class="messages-container" @scroll="handleScroll" ref="messagesRef">
      <div v-if="isLoadingMessages && messages.length === 0" class="loading-container">
        <LoadingSpinner />
      </div>

      <div v-else class="messages-list">
        <div v-if="hasMore" class="load-more-container">
          <button 
            v-if="!isLoadingMore" 
            @click="loadMore" 
            class="load-more-btn"
          >
            Load earlier messages
          </button>
          <LoadingSpinner v-else size="small" />
        </div>

        <MessageItem
          v-for="(message, index) in messages"
          :key="message.id"
          :message="message"
          :isOwn="message.senderId === userId"
          :otherUser="otherUser"
          :showReadStatus="index === messages.length - 1"
        />

        <div ref="bottomRef" class="scroll-anchor"></div>
      </div>
    </div>

    <div class="input-container">
      <textarea
        v-model="newMessage"
        @keydown.enter.exact.prevent="handleSendMessage"
        placeholder="Type a message..."
        class="message-input"
        rows="1"
        ref="inputRef"
      ></textarea>
      
      <button 
        @click="handleSendMessage" 
        :disabled="!newMessage.trim() || isSending"
        class="send-btn"
        :class="{ 'disabled': !newMessage.trim() || isSending }"
      >
        <svg v-if="!isSending" class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
        </svg>
        <LoadingSpinner v-else size="small" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useChat } from '@/composables/useChat'
import { chatMessageApi } from '@/api/chatMessage'
import { userApi } from '@/api/user'
import MessageItem from './MessageItem.vue'
import LoadingSpinner from '../LoadingSpinner.vue'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps({
  chatId: {
    type: Number,
    required: false,
    default: null
  },
  recipientId: {
    type: Number,
    required: false,
    default: null
  }
})

const emit = defineEmits(['close', 'chatCreated'])

const router = useRouter()
const { userId } = useAuth()
const { chats, sendMessage, markAsRead, currentlyViewingChatId } = useChat()

const messages = ref([])
const newMessage = ref('')
const isLoadingMessages = ref(false)
const isLoadingMore = ref(false)
const isSending = ref(false)
const isTyping = ref(false)
const hasMore = ref(false)
const currentPage = ref(0)
const messagesRef = ref(null)
const bottomRef = ref(null)
const inputRef = ref(null)
const recipientInfo = ref(null)
let typingTimeout = null

// lấy thông tin chat từ danh sách chats hoặc sử dụng recipientId cho chat mới
const currentChat = computed(() => {
  if (props.chatId)
    return chats.value.find(chat => chat.id === props.chatId)
  return null
})

const otherUser = computed(() => {
  if (currentChat.value)
    return {
      id: currentChat.value.otherUserId,
      username: currentChat.value.name,
      avatar: currentChat.value.avatar
    }

  // đối với chat mới, sử dụng thông tin người nhận đã lấy
  if (props.recipientId && recipientInfo.value)
    return {
      id: props.recipientId,
      username: recipientInfo.value.username,
      avatar: recipientInfo.value.avatar
    }

  if (props.recipientId)
    return {
      id: props.recipientId,
      username: 'Loading...',
      avatar: null
    }

  return null
})

const handleProfileClick = async () => {
  if (otherUser.value?.id)
    await router.push({
      name: 'Profile',
      params: { userId: String(otherUser.value.id) }
    })
}

const handleClose = () => emit('close')

const fetchRecipientInfo = async (recipientId) => {
  try {
    const response = await userApi.getUserProfile(recipientId)
    if (response.data.status === 200)
      recipientInfo.value = response.data.data
  } catch (error) {
    console.error('Error fetching recipient info:', error)
  }
}

const loadMessages = async (page = 0) => {
  if (!props.chatId) {
    isLoadingMessages.value = false
    return
  }

  if (page === 0)
    isLoadingMessages.value = true
  else
    isLoadingMore.value = true

  try {
    const response = await chatMessageApi.getMessages(props.chatId, page, 50)
    if (response.data.status === 200) {
      const data = response.data.data
      const newMessages = data.content || []

      if (page === 0) {
        messages.value = newMessages.reverse()
        await nextTick()
        scrollToBottom()
      } else {
        const oldScrollHeight = messagesRef.value?.scrollHeight || 0
        messages.value = [...newMessages.reverse(), ...messages.value]
        await nextTick()

        // giữ vị trí cuộn sau khi tải thêm
        if (messagesRef.value) {
          const newScrollHeight = messagesRef.value.scrollHeight
          messagesRef.value.scrollTop = newScrollHeight - oldScrollHeight
        }
      }

      hasMore.value = !data.last
      currentPage.value = page
    }
  } catch (error) {
    console.error('Error loading messages:', error)
  } finally {
    isLoadingMessages.value = false
    isLoadingMore.value = false
  }
}

const loadMore = async () => {
  if (hasMore.value && !isLoadingMore.value)
    await loadMessages(currentPage.value + 1)
}

const handleScroll = (event) => {
  const { scrollTop } = event.target
  if (scrollTop < 100 && hasMore.value && !isLoadingMore.value)
    loadMore()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (bottomRef.value)
      bottomRef.value.scrollIntoView({ behavior: 'smooth' })
  })
}

const handleSendMessage = async () => {
  if (!newMessage.value.trim() || isSending.value || !otherUser.value?.id)
    return

  const content = newMessage.value.trim()
  newMessage.value = ''
  isSending.value = true

  try {
    // thêm optimistic message ngay lập tức
    const optimisticMessage = {
      id: `temp-${Date.now()}`, // id tạm thời
      content,
      senderId: userId.value,
      createdAt: new Date().toISOString(),
      isRead: false,
      isOptimistic: true // đánh dấu là optimistic để dễ thay thế về sau
    }
    messages.value.push(optimisticMessage)
    
    await nextTick()
    scrollToBottom()
    
    // gửi message thực tế qua WebSocket
    await sendMessage(otherUser.value.id, content)
  } catch (error) {
    console.error('Error sending message:', error)
    messages.value = messages.value.filter(m => !m.isOptimistic)
    newMessage.value = content
  } finally {
    isSending.value = false
    inputRef.value?.focus()
  }
}

// theo dõi tin nhắn trong chat hiện tại từ useChat
// useChat xử lý tất cả các đăng ký WebSocket và cập nhật
watch(() => currentChat.value?.lastMessage, (newLastMessage) => {
  if (!newLastMessage)
    return

  const existingMessage = messages.value.find(m => m.id === newLastMessage.id)
  if (existingMessage)
    return

  // thay thế optimistic message bằng message thực tế khi server phản hồi
  const optimisticIndex = messages.value.findIndex(m => 
    m.isOptimistic && 
    m.content === newLastMessage.content && 
    m.senderId === newLastMessage.senderId
  )
  
  if (optimisticIndex !== -1)
    messages.value.splice(optimisticIndex, 1, newLastMessage)
  else
    messages.value.push(newLastMessage)
  
  scrollToBottom()

  if (newLastMessage.senderId !== userId.value && props.chatId)
    markAsRead(props.chatId)
}, { deep: true })

// theo dõi việc tạo chat mới (khi recipientId được cung cấp nhưng không có chatId)
watch(() => chats.value, (newChats) => {
  if (!props.chatId && props.recipientId) {
    const newChat = newChats.find(chat => chat.otherUserId === props.recipientId)
    if (newChat && newChat.id)
      emit('chatCreated', newChat.id)
  }
}, { deep: true })

// theo dõi thay đổi của read status
watch(() => currentChat.value?.hasUnread, (hasUnread) => {
  // nếu được đánh dấu là đã đọc từ bên ngoài, cập nhật read status của các message trong chat
  if (hasUnread === false && props.chatId)
    messages.value.forEach(message => {
      if (message.senderId === userId.value)
        message.isRead = true
    })
})

onMounted(async () => {
  if (props.chatId)
    currentlyViewingChatId.value = props.chatId
  
  // fetch thông tin người nhận nếu đang tạo chat mới
  if (props.recipientId && !props.chatId)
    await fetchRecipientInfo(props.recipientId)
  
  await loadMessages(0)
  // đánh dấu là đã đọc khi mở chat
  if (props.chatId)
    await markAsRead(props.chatId)
  
  inputRef.value?.focus()
})
onUnmounted(() => currentlyViewingChatId.value = null)

// theo dõi chatId mới
watch(() => props.chatId, async (newChatId, oldChatId) => {
  if (newChatId) {
    // cập nhật chat đang view
    currentlyViewingChatId.value = newChatId
    
    messages.value = []
    currentPage.value = 0
    hasMore.value = false
    await loadMessages(0)
    await markAsRead(newChatId)
  } else if (oldChatId)
    currentlyViewingChatId.value = null
})

// theo dõi thay đổi recipientId (chat mới)
watch(() => props.recipientId, async (newRecipientId) => {
  if (newRecipientId && !props.chatId) {
    await fetchRecipientInfo(newRecipientId)
    messages.value = []
    currentPage.value = 0
    hasMore.value = false
    inputRef.value?.focus()
  }
})
</script>

<style scoped>
.chat-window {
  @apply flex flex-col h-full bg-white;
}

.chat-header {
  @apply flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-white;
}

.user-info {
  @apply flex items-center gap-3 cursor-pointer hover:bg-gray-50 -ml-2 px-2 py-1 rounded-lg transition-colors;
}

.avatar {
  @apply w-10 h-10 rounded-full object-cover;
}

.user-details {
  @apply flex flex-col;
}

.username {
  @apply font-semibold text-gray-900;
}

.status {
  @apply text-sm text-gray-500;
}

.status.typing {
  @apply text-primary-600 italic;
}

.close-btn {
  @apply p-2 hover:bg-gray-100 rounded-full transition-colors;
}

.close-btn .icon {
  @apply w-5 h-5 text-gray-500;
}

.messages-container {
  @apply flex-1 overflow-y-auto px-6 py-4 bg-gray-50;
}

.loading-container {
  @apply flex items-center justify-center h-full;
}

.messages-list {
  @apply flex flex-col gap-2;
}

.load-more-container {
  @apply flex justify-center py-4;
}

.load-more-btn {
  @apply text-sm text-primary-600 hover:text-primary-700 font-medium;
}

.scroll-anchor {
  @apply h-px;
}

.input-container {
  @apply flex items-end gap-3 px-6 py-4 border-t border-gray-200 bg-white;
}

.message-input {
  @apply flex-1 resize-none rounded-lg border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent;
  max-height: 120px;
}

.send-btn {
  @apply p-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center;
  min-width: 48px;
  min-height: 48px;
}

.send-btn.disabled {
  @apply bg-gray-300 hover:bg-gray-300;
}

.send-btn .icon {
  @apply w-5 h-5;
}
</style>

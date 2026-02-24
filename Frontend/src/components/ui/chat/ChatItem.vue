<template>
  <div
    class="chat-item"
    :class="{ 
      'unread': chat.hasUnread,
      'compact': compact,
      'active': active
    }"
  >
    <div class="chat-avatar">
      <img
        :src="chat.avatar || defaultAvatar"
        :alt="chat.name"
        class="avatar-image"
        @error="(e) => e.target.src = defaultAvatar"
      />
    </div>
    <div class="chat-content">
      <div class="chat-header">
        <span class="chat-name">{{ chat.name }}</span>
        <span class="chat-time">{{ formatTime(chat.lastMessagedAt) }}</span>
      </div>
      <p class="chat-preview">
        <span v-if="isOwnMessage" class="you-prefix">You: </span>
        {{ truncatedPreview }}
      </p>
    </div>
    <div v-if="chat.hasUnread" class="unread-indicator"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import defaultAvatar from '@/assets/images/default-avatar.png'
import { formatTime } from '@/utils/formatTime'

const props = defineProps({
  chat: {
    type: Object,
    required: true
  },
  compact: {
    type: Boolean,
    default: false
  },
  active: {
    type: Boolean,
    default: false
  }
})

const { userId } = useAuth()

const isOwnMessage = computed(() => props.chat.lastSenderId === userId.value)

const truncatedPreview = computed(() => {
  const preview = props.chat.lastMessagePreview || ''
  const maxLength = props.compact ? 40 : 50
  
  if (preview.length > maxLength)
    return preview.substring(0, maxLength) + '...'
  return preview
})
</script>

<style scoped>
.chat-item {
  @apply flex items-center gap-3 px-4 py-3 hover:bg-gray-50 transition-colors cursor-pointer relative;
}

.chat-item.compact {
  @apply px-3 py-2;
}

.chat-item.unread {
  @apply bg-blue-50 hover:bg-blue-100;
}

.chat-item.active {
  @apply bg-primary-50 border-l-4 border-primary-600;
}

.chat-avatar {
  @apply flex-shrink-0;
}

.avatar-image {
  @apply w-12 h-12 rounded-full object-cover;
}

.chat-item.compact .avatar-image {
  @apply w-10 h-10;
}

.chat-content {
  @apply flex-1 min-w-0;
}

.chat-header {
  @apply flex items-center justify-between mb-1;
}

.chat-name {
  @apply font-semibold text-gray-900 truncate;
}

.chat-item.compact .chat-name {
  @apply text-sm;
}

.chat-time {
  @apply text-xs text-gray-500 flex-shrink-0 ml-2;
}

.chat-preview {
  @apply text-sm text-gray-600 truncate;
}

.chat-item.compact .chat-preview {
  @apply text-xs;
}

.you-prefix {
  @apply font-medium;
}

.unread-indicator {
  @apply w-2 h-2 bg-primary-600 rounded-full flex-shrink-0;
}
</style>

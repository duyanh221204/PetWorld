<template>
  <div class="message-item" :class="{ 'own': isOwn }">
    <div v-if="!isOwn" class="avatar-wrapper">
      <img
        :src="otherUser?.avatar || defaultAvatar"
        :alt="otherUser?.username"
        class="avatar"
        @click="handleProfileClick"
        @error="(e) => e.target.src = defaultAvatar"
      />
    </div>

    <div class="message-content">
      <div class="message-bubble" :class="{ 'own': isOwn }">
        <p class="message-text">{{ message.content }}</p>
      </div>
      
      <div class="message-meta" :class="{ 'own': isOwn }">
        <span class="time">{{ formatTime(message.createdAt) }}</span>
        <span v-if="isOwn && showReadStatus" class="read-status">
          <svg v-if="message.isRead" class="icon read" fill="currentColor" viewBox="0 0 20 20">
            <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
          </svg>
          <svg v-else class="icon sent" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/>
          </svg>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  isOwn: {
    type: Boolean,
    default: false
  },
  otherUser: {
    type: Object,
    default: null
  },
  showReadStatus: {
    type: Boolean,
    default: true
  }
})

const router = useRouter()

const handleProfileClick = async () => {
  if (props.otherUser?.id)
    await router.push({
      name: 'Profile',
      params: { userId: props.otherUser.id }
    })
}

const formatTime = (timestamp) => {
  if (!timestamp)
    return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const timeString = date.toLocaleTimeString('en-US', { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false 
  })

  if (date.toDateString() === now.toDateString())
    return timeString

  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString())
    return `Yesterday ${timeString}`

  const dateString = date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
  })
  return `${dateString}, ${timeString}`
}
</script>

<style scoped>
.message-item {
  @apply flex gap-2 items-start;
}

.message-item.own {
  @apply flex-row-reverse;
}

.avatar-wrapper {
  @apply flex-shrink-0;
}

.avatar {
  @apply w-8 h-8 rounded-full object-cover cursor-pointer hover:opacity-80 transition-opacity;
}

.message-content {
  @apply flex flex-col gap-1 max-w-[70%];
}

.message-bubble {
  @apply px-4 py-2 rounded-2xl break-words;
  background-color: #e5e7eb;
}

.message-bubble.own {
  @apply bg-primary-600 text-white;
}

.message-text {
  @apply text-sm whitespace-pre-wrap;
}

.message-meta {
  @apply flex items-center gap-1 text-xs text-gray-500 px-2;
}

.message-meta.own {
  @apply flex-row-reverse;
}

.time {
  @apply text-xs;
}

.read-status {
  @apply flex items-center;
}

.icon {
  @apply w-4 h-4;
}

.icon.read {
  @apply text-blue-500;
}

.icon.sent {
  @apply text-gray-400;
}
</style>

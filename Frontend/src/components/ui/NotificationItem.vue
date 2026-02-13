<template>
  <router-link
    :to="notificationLink"
    class="notification-item"
    :class="{ 'unread': !notification.isRead }"
    @click="handleClick"
  >
    <div class="notification-avatar">
      <img
        :src="senderAvatar"
        :alt="senderName"
        class="avatar-image"
      />
    </div>
    <div class="notification-content">
      <p class="notification-text">
        <span class="sender-name">{{ senderName }}</span>
        {{ notificationMessage }}
      </p>
      <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
    </div>
    <div v-if="!notification.isRead" class="unread-indicator"></div>
  </router-link>
</template>

<script setup>
import defaultAvatar from '@/assets/images/default-avatar.png'
import { computed } from 'vue'

const props = defineProps({
  notification: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const senderName = computed(() => props.notification.senderUsername || 'PetWorld')

const senderAvatar = computed(() => props.notification.senderAvatar || defaultAvatar)

const notificationMessage = computed(() => {
  const type = props.notification.type
  
  switch (type) {
    case 'FRIEND_REQUEST_RECEIVED':
      return 'sent you a friend request'
    case 'FRIEND_REQUEST_ACCEPTED':
      return 'accepted your friend request'
    case 'POST_REACTED':
      return 'reacted to your post'
    case 'POST_COMMENTED':
      return 'commented on your post'
    case 'COMMENT_REPLIED':
      return 'replied to your comment'
    case 'GROUP_JOIN_REQUEST_RECEIVED':
      return 'requested to join your group'
    case 'GROUP_JOIN_REQUEST_ACCEPTED':
      return 'accepted your group join request'
    default:
      return props.notification.message || ''
  }
})

const notificationLink = computed(() => {
  const type = props.notification.type
  const senderId = props.notification.senderId

  switch (type) {
    case 'FRIEND_REQUEST_RECEIVED':
    case 'FRIEND_REQUEST_ACCEPTED':
      return senderId ? `/profile/${senderId}` : '#'
    default:
      return '#'
  }
})

const formatTime = (timestamp) => {
  if (!timestamp)
    return ''

  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 7) {
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } else if (days > 0) {
    return `${days}d ago`
  } else if (hours > 0) {
    return `${hours}h ago`
  } else if (minutes > 0) {
    return `${minutes}m ago`
  } else {
    return 'Just now'
  }
}

const handleClick = () => emit('click', props.notification)
</script>

<style scoped>
.notification-item {
  @apply flex items-start gap-3 px-4 py-4 hover:bg-gray-50 transition-colors cursor-pointer relative;
}

.notification-item.unread {
  @apply bg-purple-50 hover:bg-purple-100;
}

.notification-avatar {
  @apply flex-shrink-0;
}

.avatar-image {
  @apply w-12 h-12 rounded-full object-cover;
}

.notification-content {
  @apply flex-1 min-w-0;
}

.notification-text {
  @apply text-sm text-gray-800 mb-1.5 leading-relaxed;
}

.sender-name {
  @apply font-semibold text-gray-900;
}

.notification-time {
  @apply text-xs text-gray-500;
}

.unread-indicator {
  @apply w-2 h-2 bg-primary-600 rounded-full flex-shrink-0 mt-2;
}
</style>

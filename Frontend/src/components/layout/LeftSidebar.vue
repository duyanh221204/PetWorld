<template>
  <aside class="left-sidebar">
    <div class="sidebar-card" @click="goToMyProfile">
      <img 
        :src="currentUser?.avatar || defaultAvatar" 
        :alt="currentUser?.username"
        class="sidebar-avatar"
      />
      <div class="sidebar-user-info">
        <span class="sidebar-username">{{ currentUser?.username }}</span>
      </div>
    </div>
  </aside>
</template>

<script setup>
import defaultAvatar from '@/assets/images/default-avatar.png'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { user } = useAuth()

const currentUser = computed(() => user.value)

const goToMyProfile = () => {
  if (currentUser.value?.id) {
    router.push({ name: 'Profile', params: { userId: currentUser.value.id } })
  }
}
</script>

<style scoped>
.left-sidebar {
  @apply hidden lg:block fixed left-4 top-20 w-64;
  max-width: 256px;
}

.sidebar-card {
  @apply bg-white rounded-xl shadow-sm p-4 flex items-center space-x-3 cursor-pointer hover:shadow-md transition-shadow;
}

.sidebar-avatar {
  @apply w-14 h-14 rounded-full object-cover border-2 border-gray-200;
}

.sidebar-user-info {
  @apply flex-1;
}

.sidebar-username {
  @apply font-semibold text-gray-900;
}
</style>

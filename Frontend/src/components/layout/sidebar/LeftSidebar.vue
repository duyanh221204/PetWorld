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

    <div class="sidebar-menu">
      <router-link to="/friend-requests" class="menu-item">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
        </svg>
        <span>Friend Requests</span>
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import defaultAvatar from '@/assets/images/default-avatar.png'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth.js'

const router = useRouter()
const { user } = useAuth()

const currentUser = computed(() => user.value)

const goToMyProfile = () => {
  if (currentUser.value?.id)
    router.push({ name: 'Profile', params: { userId: currentUser.value.id } })
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

.sidebar-menu {
  @apply mt-4 space-y-2;
}

.menu-item {
  @apply flex items-center space-x-3 px-4 py-3 bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow cursor-pointer text-gray-700 hover:text-primary-600;
}

.menu-item.router-link-active {
  @apply bg-primary-50 text-primary-600;
}
</style>

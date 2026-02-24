<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-content">
        <router-link to="/newsfeed" class="logo-link">
          <img src="/logo.png" alt="PetWorld Logo" class="logo-image" />
          <span class="logo-text">PetWorld</span>
        </router-link>

        <div v-if="auth.isAuthenticated.value" class="search-wrapper">
          <div class="search-container">
            <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
            <input 
              type="text"
              v-model="searchQuery"
              placeholder="Search on PetWorld"
              class="search-input"
              @keyup.enter="handleSearch"
            />
          </div>
        </div>

        <div class="user-menu">
          <template v-if="!auth.isAuthenticated.value">
            <router-link to="/login" class="nav-btn">
              Sign In
            </router-link>
            <router-link to="/register" class="nav-link">
              Sign Up
            </router-link>
          </template>
          <template v-else>
            <ChatDropdown />
            <NotificationDropdown />
            <button @click="handleLogout" class="logout-btn">
              Logout
            </button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useAuth } from '@/composables/useAuth.js'
import { useRouter } from 'vue-router'
import NotificationDropdown from '@/components/ui/notification/NotificationDropdown.vue'
import ChatDropdown from '@/components/ui/chat/ChatDropdown.vue'

const auth = useAuth()
const router = useRouter()

const searchQuery = ref('')

const handleSearch = () => {
  // TODO: Implement search functionality
  console.log('Search for:', searchQuery.value)
}

const handleLogout = async () => {
  try {
    await auth.logout()
  } finally {
    await router.push('/login')
  }
}
</script>

<style scoped>
.app-header {
  @apply bg-white shadow-sm border-b border-gray-100 sticky top-0 z-50;
}

.header-container {
  @apply max-w-7xl mx-auto px-4 sm:px-6 lg:px-8;
}

.header-content {
  @apply flex justify-between items-center h-16 gap-4;
}

.logo-link {
  @apply flex items-center space-x-3 flex-shrink-0;
}

.logo-image {
  @apply h-10 w-10 object-contain;
}

.logo-text {
  @apply text-2xl font-bold text-primary-600;
}

.search-wrapper {
  @apply flex-1 max-w-md;
}

.search-container {
  @apply relative;
}

.search-icon {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400;
}

.search-input {
  @apply w-full pl-10 pr-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent text-sm;
}

.user-menu {
  @apply flex items-center space-x-4 flex-shrink-0;
}

.nav-link {
  @apply text-gray-600 hover:text-primary-600 font-medium transition-colors;
}

.nav-btn {
  @apply bg-primary-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-primary-700 transition-colors;
}

.logout-btn {
  @apply text-gray-600 hover:text-red-500 font-medium transition-colors px-4 py-2 rounded-lg hover:bg-gray-50;
}
</style>

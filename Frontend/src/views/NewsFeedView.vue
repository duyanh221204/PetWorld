<template>
  <div class="newsfeed-view">
    <AppHeader />

    <main class="main-content">
      <div class="newsfeed-container">
        <div class="tabs-wrapper">
          <div class="tabs">
            <button 
              @click="switchTab('home')" 
              :class="['tab', { active: currentTab === 'home' }]"
            >
              <svg class="tab-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"/>
              </svg>
            </button>
            <button 
              @click="switchTab('group')" 
              :class="['tab', { active: currentTab === 'group' }]"
            >
              <svg class="tab-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z"/>
              </svg>
            </button>
            <button 
              @click="switchTab('friends')" 
              :class="['tab', { active: currentTab === 'friends' }]"
              disabled
            >
              <svg class="tab-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
              </svg>
              <span class="coming-soon">(Coming Soon)</span>
            </button>
          </div>
        </div>

        <div class="posts-feed">
          <LoadingSpinner v-if="isLoading && posts.length === 0" size="lg" />
          
          <template v-else>
            <PostItem 
              v-for="post in posts" 
              :key="post.id"
              :post="post"
              @refresh="handleRefreshPost"
              @toggle-reaction="handleToggleReaction"
            />

            <div v-if="isLoading && posts.length > 0" class="load-more">
              <LoadingSpinner size="md" />
            </div>

            <div v-if="!hasMore && posts.length > 0" class="no-more-posts">
              <p>You've reached the end! 🎉</p>
            </div>

            <div v-if="!isLoading && posts.length === 0" class="empty-state">
              <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
              </svg>
              <p class="empty-text">No posts yet</p>
            </div>
          </template>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import PostItem from '@/components/ui/PostItem.vue'
import { postApi } from '@/api/post'

const route = useRoute()
const router = useRouter()

const currentTab = ref('home')
const posts = ref([])
const isLoading = ref(false)
const currentPage = ref(0)
const hasMore = ref(true)

const switchTab = (tab) => {
  if (tab === 'friends') return // Disabled for now
  
  currentTab.value = tab
  posts.value = []
  currentPage.value = 0
  hasMore.value = true
  
  // Update route
  router.push({ name: `NewsFeed${tab.charAt(0).toUpperCase() + tab.slice(1)}` })
  
  loadPosts()
}

const loadPosts = async () => {
  if (isLoading.value || !hasMore.value) return

  isLoading.value = true
  try {
    let response
    if (currentTab.value === 'home') {
      response = await postApi.getPostsForNewsFeed(currentPage.value, 10)
    } else if (currentTab.value === 'group') {
      response = await postApi.getGroupPosts(currentPage.value, 10)
    }

    if (response.data.status === 200) {
      const newPosts = response.data.data.content
      posts.value.push(...newPosts)
      hasMore.value = !response.data.data.last
      ++currentPage.value
    }
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    isLoading.value = false
  }
}

const handleRefreshPost = async (postId) => {
  try {
    const response = await postApi.getPostById(postId)
    if (response.data.status === 200) {
      const updatedPost = response.data.data
      const index = posts.value.findIndex(p => p.id === postId)
      if (index !== -1) {
        posts.value[index] = updatedPost
      }
    }
  } catch (error) {
    console.error('Failed to refresh post:', error)
  }
}

const handleToggleReaction = async (postId) => {
  // TODO: Implement reaction API
  console.log('Toggle reaction for post:', postId)
  
  // Optimistic update
  const post = posts.value.find(p => p.id === postId)
  if (post) {
    if (post.isReactedByCurrentUser) {
      post.reactionCount--
      post.isReactedByCurrentUser = false
    } else {
      post.reactionCount++
      post.isReactedByCurrentUser = true
    }
  }
}

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 500) {
    loadPosts()
  }
}

onMounted(() => {
  // Determine current tab from route
  const path = route.path
  if (path.includes('/group')) {
    currentTab.value = 'group'
  } else if (path.includes('/friends')) {
    currentTab.value = 'friends'
  } else {
    currentTab.value = 'home'
  }

  loadPosts()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.newsfeed-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.newsfeed-container {
  @apply max-w-2xl mx-auto px-4;
}

.tabs-wrapper {
  @apply bg-white rounded-xl shadow-sm mb-4 sticky top-0 z-10;
}

.tabs {
  @apply flex border-b border-gray-200;
}

.tab {
  @apply flex-1 flex items-center justify-center space-x-2 py-4 text-gray-600 font-medium transition-colors border-b-2 border-transparent hover:text-primary-600 hover:border-primary-200 disabled:opacity-50 disabled:cursor-not-allowed;
}

.tab.active {
  @apply text-primary-600 border-primary-600;
}

.tab-icon {
  @apply w-5 h-5;
}

.coming-soon {
  @apply text-xs text-gray-400 ml-1;
}

.posts-feed {
  @apply space-y-4;
}

.load-more {
  @apply flex justify-center py-8;
}

.no-more-posts {
  @apply text-center py-8 text-gray-500;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-4;
}

.empty-text {
  @apply text-gray-500 text-lg;
}
</style>

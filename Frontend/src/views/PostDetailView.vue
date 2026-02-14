<template>
  <div class="post-detail-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="post-container">
        <div v-if="isLoading" class="loading-container">
          <LoadingSpinner />
        </div>

        <div v-else-if="error" class="error-container">
          <svg class="error-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
          </svg>
          <p class="error-text">{{ error }}</p>
          <button @click="handleRetry" class="retry-btn">Try again</button>
        </div>

        <div v-else-if="!post" class="empty-container">
          <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
          </svg>
          <p class="empty-text">No posts</p>
          <p class="empty-subtext">This post may have been deleted</p>
        </div>

        <PostItem
          v-else
          :post="post"
          @refresh="fetchPost"
          @toggle-reaction="handleToggleReaction"
        />
      </div>

      <RightSidebar />
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { postApi } from '@/api/post'
import { reactionApi } from '@/api/reaction'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import RightSidebar from '@/components/layout/sidebar/RightSidebar.vue'
import PostItem from '@/components/ui/PostItem.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const route = useRoute()
const postId = ref(parseInt(route.params.id))

const post = ref(null)
const isLoading = ref(false)
const error = ref(null)

const fetchPost = async () => {
  isLoading.value = true
  error.value = null

  try {
    const response = await postApi.getPostById(postId.value)
    if (response.data.status === 200)
      post.value = response.data.data
    else
      post.value = null
  } catch (err) {
    console.error('Error fetching post:', err)
    if (err.response?.status === 404)
      post.value = null
    else
      error.value = 'Failed to load post'
  } finally {
    isLoading.value = false
  }
}

const handleToggleReaction = async (postIdParam) => {
  if (!post.value)
    return

  const wasReacted = post.value.isReactedByCurrentUser
  if (wasReacted) {
    --post.value.reactionCount
    post.value.isReactedByCurrentUser = false
  } else {
    ++post.value.reactionCount
    post.value.isReactedByCurrentUser = true
  }

  try {
    if (wasReacted)
      await reactionApi.deleteReaction(postIdParam)
    else
      await reactionApi.createReaction(postIdParam)
  } catch (error) {
    console.error('Failed to toggle reaction:', error)
    if (wasReacted) {
      ++post.value.reactionCount
      post.value.isReactedByCurrentUser = true
    } else {
      --post.value.reactionCount
      post.value.isReactedByCurrentUser = false
    }
  }
}

const handleRetry = () => fetchPost()

onMounted(() => fetchPost())
</script>

<style scoped>
.post-detail-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-6;
  @apply flex gap-6 relative;
}

.post-container {
  @apply flex-1 max-w-2xl mx-auto;
  width: 100%;
}

.loading-container,
.error-container,
.empty-container {
  @apply flex flex-col items-center justify-center py-16 px-4;
}

.error-icon,
.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-4;
}

.error-text,
.empty-text {
  @apply text-lg font-medium text-gray-700 mb-2;
}

.empty-subtext {
  @apply text-sm text-gray-500;
}

.retry-btn {
  @apply mt-4 px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors;
}
</style>

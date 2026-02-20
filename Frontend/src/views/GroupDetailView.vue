<template>
  <div class="group-detail-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="group-main-container">
        <LoadingSpinner v-if="isLoadingGroup" size="lg" class="my-8" />

        <template v-else-if="group">
          <div class="group-header">
            <div class="cover-image-container">
              <img
                :src="group.coverImageUrl || defaultGroupCover"
                :alt="group.name"
                class="cover-image"
                @error="(e) => e.target.src = defaultGroupCover"
              />
            </div>

            <div class="group-info">
              <h1 class="group-name">{{ group.name }}</h1>
              <button @click="showMembers" class="members-button">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                </svg>
                {{ group.memberCount }} {{ group.memberCount === 1 ? 'member' : 'members' }}
              </button>
            </div>
          </div>

          <div v-if="group.currentUserRole" class="create-post-section">
            <button @click="createPost" class="create-post-btn">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
              </svg>
              Create Post
            </button>
          </div>

          <div class="posts-section">
            <LoadingSpinner v-if="isLoadingPosts && posts.length === 0" size="md" class="my-4" />

            <div v-else-if="accessDenied" class="access-denied">
              <svg class="access-denied-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
              </svg>
              <p class="access-denied-text">You need to be a member to view posts in this group</p>
            </div>

            <div v-else-if="posts.length === 0" class="empty-posts">
              <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
              </svg>
              <p class="empty-text">No posts in this group yet</p>
            </div>

            <template v-else>
              <PostItem
                v-for="post in posts"
                :key="post.id"
                :post="post"
                :hide-group-name="true"
                @refresh="handleRefreshPost"
                @toggle-reaction="handleToggleReaction"
              />

              <div v-if="isLoadingPosts && posts.length > 0" class="load-more">
                <LoadingSpinner size="md" />
              </div>

              <div v-if="!hasMorePosts && posts.length > 0" class="no-more-posts">
                <p>No more posts</p>
              </div>
            </template>
          </div>
        </template>

        <div v-else class="error-state">
          <p>Group not found</p>
        </div>
      </div>

      <aside v-if="group" class="group-right-sidebar">
        <div class="sidebar-card">
          <div v-if="group.description" class="sidebar-section">
            <h3 class="sidebar-title">About</h3>
            <p class="sidebar-text">{{ group.description }}</p>
          </div>

          <div v-if="group.currentUserRole" class="sidebar-section">
            <p class="role-text">
              You are {{ group.currentUserRole.toLowerCase() }} of the group
            </p>
            <button @click="handleLeaveGroup" class="action-btn leave-btn">
              Leave Group
            </button>
          </div>

          <div v-else class="sidebar-section">
            <button
              v-if="!group.isRequestedToJoin"
              @click="handleJoinGroup"
              class="action-btn join-btn"
            >
              Join Group
            </button>
            <button
              v-else
              @click="handleCancelRequest"
              class="action-btn cancel-btn"
            >
              Cancel Join Request
            </button>
          </div>
        </div>
      </aside>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import defaultGroupCover from '@/assets/images/group-default-cover-image.png'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import PostItem from '@/components/ui/PostItem.vue'
import { groupApi } from '@/api/group'
import { postApi } from '@/api/post'
import { reactionApi } from '@/api/reaction'

const route = useRoute()
const router = useRouter()

const group = ref(null)
const posts = ref([])
const isLoadingGroup = ref(true)
const isLoadingPosts = ref(false)
const currentPage = ref(0)
const hasMorePosts = ref(true)
const accessDenied = ref(false)

const groupId = computed(() => parseInt(route.params.groupId))

const loadGroup = async () => {
  isLoadingGroup.value = true
  try {
    const response = await groupApi.getGroupById(groupId.value)
    if (response.data.status === 200) {
      group.value = response.data.data
      await loadPosts()
    }
  } catch (error) {
    console.error('Error loading group:', error)
    group.value = null
  } finally {
    isLoadingGroup.value = false
  }
}

const loadPosts = async () => {
  if (isLoadingPosts.value || !hasMorePosts.value)
    return

  isLoadingPosts.value = true
  accessDenied.value = false

  try {
    const response = await postApi.getGroupPosts(groupId.value, currentPage.value, 10)
    if (response.data.status === 200) {
      const data = response.data.data
      const newPosts = data.content || []
      posts.value.push(...newPosts)
      hasMorePosts.value = !data.last
      ++currentPage.value
    }
  } catch (error) {
    console.error('Error loading posts:', error)
    if (error.response?.status === 403)
      accessDenied.value = true
  } finally {
    isLoadingPosts.value = false
  }
}

const createPost = () => router.push({
    name: 'CreatePost',
    query: { groupId: groupId.value }
  })

const handleRefreshPost = async (postId) => {
  try {
    const response = await postApi.getPostById(postId)
    if (response.data.status === 200) {
      const updatedPost = response.data.data
      const index = posts.value.findIndex(p => p.id === postId)
      if (index !== -1)
        posts.value[index] = updatedPost
    }
  } catch (error) {
    console.error('Error refreshing post:', error)
  }
}

const handleToggleReaction = async (postId) => {
  const post = posts.value.find(p => p.id === postId)
  if (!post)
    return

  const wasReacted = post.isReactedByCurrentUser
  if (wasReacted) {
    --post.reactionCount
    post.isReactedByCurrentUser = false
  } else {
    ++post.reactionCount
    post.isReactedByCurrentUser = true
  }

  try {
    if (wasReacted)
      await reactionApi.deleteReaction(postId)
    else
      await reactionApi.createReaction(postId)
  } catch (error) {
    console.error('Error toggling reaction:', error)
    if (wasReacted) {
      ++post.reactionCount
      post.isReactedByCurrentUser = true
    } else {
      --post.reactionCount
      post.isReactedByCurrentUser = false
    }
  }
}

const showMembers = () => {
  // TODO: Implement show members modal
  console.log('Show members')
}

const handleJoinGroup = () => {
  // TODO: Implement join group
  console.log('Join group')
}

const handleCancelRequest = () => {
  // TODO: Implement cancel request
  console.log('Cancel request')
}

const handleLeaveGroup = () => {
  // TODO: Implement leave group
  console.log('Leave group')
}

const handleScroll = () => {
  if (accessDenied.value)
    return

  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 500)
    loadPosts()
}

onMounted(() => {
  loadGroup()
  window.addEventListener('scroll', handleScroll)
})
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.group-detail-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.group-main-container {
  @apply max-w-3xl mx-auto px-4 space-y-4;
}

.group-header {
  @apply bg-white rounded-xl shadow-sm overflow-hidden;
}

.cover-image-container {
  @apply h-48 bg-gray-200 relative;
}

.cover-image {
  @apply w-full h-full object-cover;
}

.cover-placeholder {
  @apply w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-200 to-gray-300;
}

.group-info {
  @apply p-6;
}

.group-name {
  @apply text-3xl font-bold text-gray-900 mb-3;
}

.members-button {
  @apply flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors;
}

.create-post-section {
  @apply bg-white rounded-xl shadow-sm p-4;
}

.create-post-btn {
  @apply w-full flex items-center justify-center gap-2 px-4 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium;
}

.posts-section {
  @apply space-y-4;
}

.access-denied {
  @apply flex flex-col items-center justify-center py-16 bg-white rounded-xl shadow-sm;
}

.access-denied-icon {
  @apply w-20 h-20 text-gray-300 mb-4;
}

.access-denied-text {
  @apply text-gray-500 text-lg;
}

.empty-posts {
  @apply flex flex-col items-center justify-center py-16 bg-white rounded-xl shadow-sm;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-3;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

.load-more {
  @apply flex justify-center py-8;
}

.no-more-posts {
  @apply text-center py-8 text-gray-500;
}

.error-state {
  @apply flex items-center justify-center py-16 text-gray-500 text-lg;
}

.group-right-sidebar {
  @apply hidden xl:block fixed right-4 top-20 w-80;
  max-width: 320px;
}

.sidebar-card {
  @apply bg-white rounded-xl shadow-sm p-6 sticky top-20 space-y-6;
}

.sidebar-section {
  @apply space-y-3;
}

.sidebar-title {
  @apply text-lg font-bold text-gray-900;
}

.sidebar-text {
  @apply text-gray-700 leading-relaxed;
}

.role-text {
  @apply text-gray-700;
}

.action-btn {
  @apply w-full px-4 py-2.5 rounded-lg font-medium transition-colors;
}

.join-btn {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}

.cancel-btn {
  @apply bg-yellow-600 text-white hover:bg-yellow-700;
}

.leave-btn {
  @apply bg-red-600 text-white hover:bg-red-700;
}
</style>

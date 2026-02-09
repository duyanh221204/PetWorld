<template>
  <div class="profile-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="profile-container">
        <LoadingSpinner v-if="isLoading && !profile" size="lg" class="my-8" />

        <template v-else-if="profile">
          <!-- Profile Header -->
          <div class="profile-header">
            <img 
              :src="profile.avatar || defaultAvatar" 
              :alt="profile.username"
              class="profile-avatar"
            />
            <div class="profile-info">
              <h1 class="profile-username">{{ profile.username }}</h1>
              <div class="profile-stats">
                <button @click="scrollToPosts" class="stat-item">
                  <span class="stat-value">{{ profile.postCount || 0 }}</span>
                  <span class="stat-label">Posts</span>
                </button>
                <button @click="toggleFriendsList" class="stat-item">
                  <span class="stat-value">{{ profile.friendCount || 0 }}</span>
                  <span class="stat-label">Friends</span>
                </button>
              </div>
              <p v-if="profile.description" class="profile-description">{{ profile.description }}</p>
            </div>
            
            <!-- Action Button -->
            <div class="profile-actions">
              <button v-if="isOwnProfile" @click="showCreatePost" class="btn-create-post">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                </svg>
                Create Post
              </button>
              
              <!-- For REQUEST_RECEIVED, show both Accept and Decline buttons -->
              <template v-else-if="friendshipStatus && friendshipStatus.status === 'PENDING_RECEIVED'">
                <button 
                  @click="handleAcceptRequest"
                  class="btn-friendship btn-accept"
                  :disabled="true"
                  title="API coming soon"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                  Accept Request
                </button>
                <button 
                  @click="handleDeclineRequestButton"
                  class="btn-friendship btn-cancel"
                  :disabled="true"
                  title="API coming soon"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                  </svg>
                  Decline
                </button>
              </template>
              
              <!-- For other statuses, show single button -->
              <button 
                v-else-if="friendshipStatus" 
                @click="handleFriendshipAction"
                :class="['btn-friendship', friendshipButtonClass]"
                :disabled="true"
                title="API coming soon"
              >
                <LoadingSpinner v-if="isActionLoading" size="sm" />
                <template v-else>
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="friendshipIconPath"/>
                  </svg>
                  {{ friendshipButtonText }} (Coming Soon)
                </template>
              </button>
            </div>
          </div>

          <!-- Friends List Modal -->
          <div v-if="showFriendsList" class="friends-modal-overlay" @click="toggleFriendsList">
            <div class="friends-modal" @click.stop>
              <div class="friends-modal-header">
                <h2 class="friends-modal-title">Friends</h2>
                <button @click="toggleFriendsList" class="close-button">
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                  </svg>
                </button>
              </div>
              
              <div class="friends-list">
                <LoadingSpinner v-if="isLoadingFriends" size="md" class="my-4" />
                
                <template v-else>
                  <div class="friends-grid">
                    <div 
                      v-for="friend in friends" 
                      :key="friend.id"
                      @click="goToProfile(friend.id)"
                      class="friend-item"
                    >
                      <img 
                        :src="friend.avatar || defaultAvatar" 
                        :alt="friend.username"
                        class="friend-avatar"
                      />
                      <span class="friend-username">{{ friend.username }}</span>
                    </div>
                  </div>

                  <div v-if="hasMoreFriends" class="load-more-friends">
                    <button @click="loadMoreFriends" class="btn-load-more" :disabled="isLoadingFriends">
                      Load More
                    </button>
                  </div>

                  <div v-if="!isLoadingFriends && friends.length === 0" class="empty-friends">
                    <p>No friends yet</p>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- Posts Section -->
          <div ref="postsSection" class="posts-section">
            <h2 class="section-title">Posts</h2>
            
            <LoadingSpinner v-if="isLoadingPosts && userPosts.length === 0" size="md" class="my-4" />
            
            <template v-else>
              <PostItem 
                v-for="post in userPosts" 
                :key="post.id"
                :post="post"
                @refresh="handleRefreshPost"
                @toggle-reaction="handleToggleReaction"
              />

              <div v-if="isLoadingPosts && userPosts.length > 0" class="load-more">
                <LoadingSpinner size="md" />
              </div>

              <div v-if="!hasMorePosts && userPosts.length > 0" class="no-more-posts">
                <p>No more posts</p>
              </div>

              <div v-if="!isLoadingPosts && userPosts.length === 0" class="empty-posts">
                <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                </svg>
                <p class="empty-text">No posts yet</p>
              </div>
            </template>
          </div>
        </template>

        <div v-else class="error-state">
          <p>Profile not found</p>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import defaultAvatar from '@/assets/images/default-avatar.png'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/LeftSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import PostItem from '@/components/ui/PostItem.vue'
import { userApi } from '@/api/user'
import { postApi } from '@/api/post'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { user } = useAuth()

const profile = ref(null)
const friendshipStatus = ref(null)
const userPosts = ref([])
const friends = ref([])

const isLoading = ref(true)
const isLoadingPosts = ref(false)
const isLoadingFriends = ref(false)
const isActionLoading = ref(false)

const currentPostPage = ref(0)
const hasMorePosts = ref(true)

const currentFriendPage = ref(0)
const hasMoreFriends = ref(true)

const showFriendsList = ref(false)

const postsSection = ref(null)

const isOwnProfile = computed(() => {
  return user.value?.id === profile.value?.id
})

const friendshipButtonText = computed(() => {
  if (!friendshipStatus.value) return ''
  
  const status = friendshipStatus.value.status
  switch (status) {
    case 'NONE':
      return 'Add Friend'
    case 'PENDING_SENT':
      return 'Cancel Request'
    case 'PENDING_RECEIVED':
      return 'Accept Request'
    case 'FRIENDS':
      return 'Remove Friend'
    default:
      return ''
  }
})

const friendshipButtonClass = computed(() => {
  if (!friendshipStatus.value) return ''
  
  const status = friendshipStatus.value.status
  switch (status) {
    case 'NONE':
      return 'btn-add'
    case 'PENDING_SENT':
      return 'btn-cancel'
    case 'PENDING_RECEIVED':
      return 'btn-accept'
    case 'FRIENDS':
      return 'btn-remove'
    default:
      return ''
  }
})

const friendshipIconPath = computed(() => {
  if (!friendshipStatus.value) return ''
  
  const status = friendshipStatus.value.status
  switch (status) {
    case 'NONE':
      return 'M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z'
    case 'PENDING_SENT':
      return 'M6 18L18 6M6 6l12 12'
    case 'PENDING_RECEIVED':
      return 'M5 13l4 4L19 7'
    case 'FRIENDS':
      return 'M13 7a4 4 0 11-8 0 4 4 0 018 0zM9 14a6 6 0 00-6 6v1h12v-1a6 6 0 00-6-6z'
    default:
      return ''
  }
})

const loadProfile = async () => {
  const userId = route.params.userId
  if (!userId) return

  isLoading.value = true
  try {
    const response = await userApi.getUserProfile(userId)
    if (response.data.status === 200) {
      profile.value = response.data.data
    }

    // Load friendship status if not own profile
    if (!isOwnProfile.value) {
      const statusResponse = await userApi.getFriendshipStatus(userId)
      if (statusResponse.data.status === 200) {
        friendshipStatus.value = statusResponse.data.data
      }
    }

    // Load posts
    loadPosts()
  } catch (error) {
    console.error('Failed to load profile:', error)
  } finally {
    isLoading.value = false
  }
}

const loadPosts = async () => {
  const userId = route.params.userId
  if (!userId || isLoadingPosts.value || !hasMorePosts.value) return

  isLoadingPosts.value = true
  try {
    const response = await postApi.getUserPosts(userId, currentPostPage.value, 10)
    if (response.data.status === 200) {
      const newPosts = response.data.data.content
      userPosts.value.push(...newPosts)
      hasMorePosts.value = !response.data.data.last
      currentPostPage.value++
    }
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    isLoadingPosts.value = false
  }
}

const loadFriends = async (reset = false) => {
  const userId = route.params.userId
  if (!userId) return

  if (reset) {
    friends.value = []
    currentFriendPage.value = 0
    hasMoreFriends.value = true
  }

  if (isLoadingFriends.value || !hasMoreFriends.value) return

  isLoadingFriends.value = true
  try {
    const response = await userApi.getFriendsList(userId, currentFriendPage.value, 100)
    if (response.data.status === 200) {
      const newFriends = response.data.data.content
      friends.value.push(...newFriends)
      hasMoreFriends.value = !response.data.data.last
      currentFriendPage.value++
    }
  } catch (error) {
    console.error('Failed to load friends:', error)
  } finally {
    isLoadingFriends.value = false
  }
}

const loadMoreFriends = () => {
  loadFriends(false)
}

const toggleFriendsList = () => {
  showFriendsList.value = !showFriendsList.value
  if (showFriendsList.value && friends.value.length === 0) {
    loadFriends(true)
  }
}

const scrollToPosts = () => {
  postsSection.value?.scrollIntoView({ behavior: 'smooth' })
}

const goToProfile = (userId) => {
  showFriendsList.value = false
  router.push({ name: 'Profile', params: { userId } })
}

// TODO: Implement when friendship APIs are ready
const handleFriendshipAction = () => {
  alert('Friendship actions API coming soon!')
}

const handleAcceptRequest = () => {
  alert('Accept request API coming soon!')
}

const handleDeclineRequestButton = () => {
  alert('Decline request API coming soon!')
}

const showCreatePost = () => {
  // TODO: Implement create post modal
  alert('Create post feature coming soon!')
}

const handleRefreshPost = async (postId) => {
  try {
    const response = await postApi.getPostById(postId)
    if (response.data.status === 200) {
      const updatedPost = response.data.data
      const index = userPosts.value.findIndex(p => p.id === postId)
      if (index !== -1) {
        userPosts.value[index] = updatedPost
      }
    }
  } catch (error) {
    console.error('Failed to refresh post:', error)
  }
}

const handleToggleReaction = async (postId) => {
  // TODO: Implement reaction API
  const post = userPosts.value.find(p => p.id === postId)
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

// Watch for route changes
watch(() => route.params.userId, (newUserId, oldUserId) => {
  if (newUserId && newUserId !== oldUserId) {
    // Reset state
    profile.value = null
    friendshipStatus.value = null
    userPosts.value = []
    friends.value = []
    currentPostPage.value = 0
    hasMorePosts.value = true
    currentFriendPage.value = 0
    hasMoreFriends.value = true
    showFriendsList.value = false
    
    // Load new profile
    loadProfile()
  }
})

onMounted(() => {
  loadProfile()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.profile-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.profile-container {
  @apply max-w-3xl mx-auto px-4;
  width: 100%;
}

.profile-header {
  @apply bg-white rounded-xl shadow-md p-6 mb-6 flex items-start space-x-6;
}

.profile-avatar {
  @apply w-32 h-32 rounded-full object-cover border-4 border-gray-200;
}

.profile-info {
  @apply flex-1;
}

.profile-username {
  @apply text-3xl font-bold text-gray-900 mb-4;
}

.profile-stats {
  @apply flex space-x-8 mb-4;
}

.stat-item {
  @apply flex flex-col items-center cursor-pointer hover:opacity-75 transition-opacity;
}

.stat-value {
  @apply text-2xl font-bold text-gray-900;
}

.stat-label {
  @apply text-sm text-gray-600;
}

.profile-description {
  @apply text-gray-700 leading-relaxed;
}

.profile-actions {
  @apply flex flex-col space-y-2;
}

.btn-create-post {
  @apply flex items-center space-x-2 px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium;
}

.btn-friendship {
  @apply flex items-center justify-center space-x-2 px-6 py-2 rounded-lg transition-colors font-medium disabled:opacity-50;
}

.btn-add {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}

.btn-cancel {
  @apply bg-gray-200 text-gray-700 hover:bg-gray-300;
}

.btn-accept {
  @apply bg-green-600 text-white hover:bg-green-700;
}

.btn-remove {
  @apply bg-red-600 text-white hover:bg-red-700;
}

.friends-modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
}

.friends-modal {
  @apply bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4 max-h-[80vh] flex flex-col;
}

.friends-modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.friends-modal-title {
  @apply text-2xl font-bold text-gray-900;
}

.close-button {
  @apply text-gray-500 hover:text-gray-700 transition-colors;
}

.friends-list {
  @apply p-6 overflow-y-auto;
}

.friends-grid {
  @apply grid grid-cols-2 gap-4;
}

.friend-item {
  @apply flex items-center space-x-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors;
}

.friend-avatar {
  @apply w-12 h-12 rounded-full object-cover border-2 border-gray-200;
}

.friend-username {
  @apply font-medium text-gray-900;
}

.load-more-friends {
  @apply mt-4 text-center;
}

.btn-load-more {
  @apply px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors disabled:opacity-50;
}

.empty-friends {
  @apply text-center py-8 text-gray-500;
}

.posts-section {
  @apply space-y-4;
}

.section-title {
  @apply text-2xl font-bold text-gray-900 mb-4;
}

.load-more {
  @apply flex justify-center py-8;
}

.no-more-posts {
  @apply text-center py-8 text-gray-500;
}

.empty-posts {
  @apply flex flex-col items-center justify-center py-16;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-4;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

.error-state {
  @apply text-center py-16 text-gray-500 text-lg;
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
}

.modal-content {
  @apply bg-white rounded-xl shadow-2xl p-6 max-w-md w-full mx-4;
}

.modal-title {
  @apply text-xl font-bold text-gray-900 mb-2;
}

.modal-message {
  @apply text-gray-700 mb-6;
}

.modal-actions {
  @apply flex space-x-3 justify-end;
}

.btn-cancel {
  @apply px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors;
}

.btn-confirm {
  @apply px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 flex items-center space-x-2;
}
</style>

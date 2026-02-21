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
              <div class="group-actions">
                <button @click="showMembers" class="members-button">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                  </svg>
                  {{ group.memberCount }} {{ group.memberCount === 1 ? 'member' : 'members' }}
                </button>
                <button v-if="isOwnerOrAdmin" @click="handleSettingsClick" class="settings-button">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  </svg>
                  Settings
                </button>
              </div>
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
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2H5a2 2 0 00-2 2v2M7 7h10"/>
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

      <GroupRightSidebar 
        v-if="group" 
        :groupId="groupId"
        :group="group"
        @join-group="handleJoinGroup"
        @cancel-request="showCancelRequestModal = true"
        @leave-group="handleLeaveGroup"
      />
    </main>

    <AppFooter />

    <GroupSettingsModal
      :show="showSettingsModal"
      :group="group"
      @close="showSettingsModal = false"
      @updated="handleGroupUpdated"
      @deleted="handleGroupDeleted"
    />

    <Transition name="modal">
      <div v-if="showCancelRequestModal" class="modal-overlay" @click="showCancelRequestModal = false">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">Cancel Join Request</h3>
            <button @click="showCancelRequestModal = false" class="modal-close">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <p class="modal-text">Are you sure you want to cancel your join request to this group?</p>
            
            <div class="modal-actions">
              <button type="button" @click="showCancelRequestModal = false" class="btn-secondary">No</button>
              <button type="button" @click="handleCancelRequest" class="btn-danger">Yes</button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="modal">
      <div v-if="showJoinGroupModal" class="modal-overlay" @click="showJoinGroupModal = false">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">Join Group</h3>
            <button @click="showJoinGroupModal = false" class="modal-close">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <p class="modal-text">Do you want to send a join request to this group?</p>
            
            <div class="modal-actions">
              <button type="button" @click="showJoinGroupModal = false" class="btn-secondary">No</button>
              <button type="button" @click="confirmJoinGroup" class="btn-primary">Yes</button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import defaultGroupCover from '@/assets/images/group-default-cover-image.png'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import PostItem from '@/components/ui/PostItem.vue'
import GroupSettingsModal from '@/components/ui/group/GroupSettingsModal.vue'
import GroupRightSidebar from '@/components/ui/group/GroupRightSidebar.vue'
import { groupApi } from '@/api/group'
import { postApi } from '@/api/post'
import { reactionApi } from '@/api/reaction'
import { groupJoinRequestApi } from '@/api/groupJoinRequest'
import { groupJoinFormApi } from '@/api/groupJoinForm'
import { useNotifications } from '@/composables/useNotifications'

const route = useRoute()
const router = useRouter()
const { notifications } = useNotifications()

const group = ref(null)
const posts = ref([])
const isLoadingGroup = ref(true)
const isLoadingPosts = ref(false)
const currentPage = ref(0)
const hasMorePosts = ref(true)
const accessDenied = ref(false)
const showSettingsModal = ref(false)
const showCancelRequestModal = ref(false)
const showJoinGroupModal = ref(false)

const groupId = computed(() => parseInt(route.params.groupId))

const isOwnerOrAdmin = computed(() => 
  group.value?.currentUserRole === 'OWNER' || group.value?.currentUserRole === 'ADMIN'
)

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

const handleSettingsClick = () => showSettingsModal.value = true

const handleGroupUpdated = (updatedGroup) => group.value = updatedGroup

const handleGroupDeleted = () => router.push({ name: 'Groups' })

const handleJoinGroup = async () => {
  try {
    const response = await groupJoinFormApi.getActiveGroupJoinForm(groupId.value)
    if (response.data.status === 200 && response.data.data) {
      await router.push({name: 'GroupJoinForm', params: {groupId: groupId.value}})
      return
    }
  } catch (error) {
    if (error.response?.status !== 404) {
      console.error('Error checking active form:', error)
      alert('Failed to check join form. Please try again.')
      return
    }
  }

  showJoinGroupModal.value = true
}

const confirmJoinGroup = async () => {
  try {
    const response = await groupJoinRequestApi.createGroupJoinRequest(groupId.value, null)
    if (response.data.status === 200) {
      await loadGroup()
      showJoinGroupModal.value = false
    } else
      alert(response.data.message || 'Failed to send join request.')
  } catch (err) {
    console.error('Error creating join request:', err)
    const errorMsg = err.response?.data?.message || 'Failed to send join request. Please try again.'
    alert(errorMsg)
  }
}

const handleCancelRequest = async () => {
  try {
    const response = await groupJoinRequestApi.cancelGroupJoinRequest(groupId.value)
    if (response.data.status === 200) {
      await loadGroup()
      showCancelRequestModal.value = false
    } else
      alert(response.data.message || 'Failed to cancel join request.')
  } catch (error) {
    console.error('Error canceling request:', error)
    alert(error.response?.data?.message || 'Failed to cancel join request. Please try again.')
  }
}

const handleLeaveGroup = () => {
  // TODO: Implement leave group when backend API is ready
  alert('Leave group feature is not yet available. Please wait for backend implementation.')
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

.group-info {
  @apply p-6;
}

.group-name {
  @apply text-3xl font-bold text-gray-900 mb-3;
}

.group-actions {
  @apply flex items-center gap-3 mt-4;
}

.members-button {
  @apply flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors;
}

.settings-button {
  @apply flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium;
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

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-xl font-bold text-gray-900;
}

.modal-close {
  @apply text-gray-400 hover:text-gray-600 transition-colors;
}

.modal-body {
  @apply p-6;
}

.modal-text {
  @apply text-gray-700 mb-6;
}

.modal-actions {
  @apply flex items-center justify-end gap-3;
}

.btn-secondary {
  @apply px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors;
}

.btn-primary {
  @apply px-6 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors;
}

.btn-danger {
  @apply px-6 py-2 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 transition-colors;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-dialog,
.modal-leave-active .modal-dialog {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.modal-enter-from .modal-dialog,
.modal-leave-to .modal-dialog {
  transform: scale(0.95);
  opacity: 0;
}
</style>

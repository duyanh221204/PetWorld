<template>
  <transition name="modal">
    <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">Comments</h2>
          <div class="header-actions">
            <button @click="handleRefresh" class="icon-btn" title="Refresh">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
              </svg>
            </button>
            <button @click="close" class="icon-btn" title="Close">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <div ref="bodyRef" class="modal-body">
          <div v-if="isLoading && comments.length === 0" class="loading-container">
            <LoadingSpinner />
          </div>

          <div v-else-if="error" class="error-container">
            <p class="error-text">{{ error }}</p>
            <button @click="handleRefresh" class="retry-btn">Retry</button>
          </div>

          <div v-else-if="comments.length === 0" class="empty-container">
            <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
            <p class="empty-text">No comments yet</p>
            <p class="empty-subtext">Be the first to comment!</p>
          </div>

          <div v-else class="comments-list">
            <div 
              v-for="comment in comments" 
              :key="comment.id"
              :id="`comment-${comment.id}`"
              :class="['comment-item', { 'highlight': highlightedCommentId === comment.id }]"
            >
              <router-link :to="`/profile/${comment.senderId}`" class="comment-avatar-link">
                <img :src="comment.senderAvatar || defaultAvatar" :alt="comment.senderUsername" class="comment-avatar" />
              </router-link>
              
              <div class="comment-content-wrapper">
                <div v-if="editingCommentId === comment.id" class="edit-mode">
                  <textarea
                    v-model="editContent"
                    class="edit-textarea"
                    rows="2"
                    @keydown.enter.exact.prevent="handleSaveEdit(comment)"
                    @keydown.esc="handleCancelEdit"
                  ></textarea>
                  <div class="edit-actions">
                    <button @click="handleCancelEdit" class="cancel-edit-btn">Cancel</button>
                    <button @click="handleSaveEdit(comment)" :disabled="!editContent.trim() || isUpdating" class="save-edit-btn">
                      <LoadingSpinner v-if="isUpdating" size="sm" />
                      <span v-else>Save</span>
                    </button>
                  </div>
                </div>

                <template v-else>
                  <div class="comment-bubble">
                    <router-link :to="`/profile/${comment.senderId}`" class="comment-username">
                      {{ comment.senderUsername }}
                    </router-link>
                    <p class="comment-text">{{ comment.content }}</p>
                  </div>
                  
                  <div class="comment-meta">
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                    <span v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt" class="edited-label">(edited)</span>
                    <button @click="handleReply(comment)" class="reply-btn">Reply</button>
                    <button v-if="isOwner(comment)" @click="handleEdit(comment)" class="edit-btn">Edit</button>
                    <button v-if="isOwner(comment)" @click="handleDeleteClick(comment)" class="delete-btn">Delete</button>
                  </div>
                </template>

                <button 
                  v-if="comment.replyCount > 0"
                  @click="toggleReplies(comment.id)"
                  class="view-replies-btn"
                >
                  <svg v-if="!expandedReplies[comment.id]" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                  </svg>
                  <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"/>
                  </svg>
                  <span>{{ expandedReplies[comment.id] ? 'Hide' : 'View' }} {{ comment.replyCount }} {{ comment.replyCount === 1 ? 'reply' : 'replies' }}</span>
                </button>

                <div v-if="expandedReplies[comment.id]" class="replies-list">
                  <div v-if="loadingReplies[comment.id]" class="replies-loading">
                    <LoadingSpinner size="sm" />
                  </div>
                  
                  <div 
                    v-else 
                    v-for="reply in replies[comment.id] || []"
                    :key="reply.id"
                    :id="`comment-${reply.id}`"
                    :class="['reply-item', { 'highlight': highlightedCommentId === reply.id }]"
                  >
                    <router-link :to="`/profile/${reply.senderId}`" class="comment-avatar-link reply-avatar-link">
                      <img :src="reply.senderAvatar || defaultAvatar" :alt="reply.senderUsername" class="comment-avatar reply-avatar" />
                    </router-link>
                    
                    <div class="comment-content-wrapper">
                      <div v-if="editingCommentId === reply.id" class="edit-mode">
                        <textarea
                          v-model="editContent"
                          class="edit-textarea"
                          rows="2"
                          @keydown.enter.exact.prevent="handleSaveEdit(reply, comment.id)"
                          @keydown.esc="handleCancelEdit"
                        ></textarea>
                        <div class="edit-actions">
                          <button @click="handleCancelEdit" class="cancel-edit-btn">Cancel</button>
                          <button @click="handleSaveEdit(reply, comment.id)" :disabled="!editContent.trim() || isUpdating" class="save-edit-btn">
                            <LoadingSpinner v-if="isUpdating" size="sm" />
                            <span v-else>Save</span>
                          </button>
                        </div>
                      </div>

                      <template v-else>
                        <div class="comment-bubble">
                          <router-link :to="`/profile/${reply.senderId}`" class="comment-username">
                            {{ reply.senderUsername }}
                          </router-link>

                          <span v-if="reply.parentCommentSenderUsername" class="replying-to">
                            @Replying to {{ reply.parentCommentSenderUsername }}
                          </span>
                          
                          <p class="comment-text">{{ reply.content }}</p>
                        </div>
                        
                        <div class="comment-meta">
                          <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                          <span v-if="reply.updatedAt && reply.updatedAt !== reply.createdAt" class="edited-label">(edited)</span>
                          <button @click="handleReply(reply)" class="reply-btn">Reply</button>
                          <button v-if="isOwner(reply)" @click="handleEdit(reply)" class="edit-btn">Edit</button>
                          <button v-if="isOwner(reply)" @click="handleDeleteClick(reply, comment.id)" class="delete-btn">Delete</button>
                        </div>
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="totalPages > 1" class="pagination-container">
              <button
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 0 || isLoading"
                class="pagination-btn"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
                Previous
              </button>

              <span class="pagination-text">
                Page {{ currentPage + 1 }} of {{ totalPages }}
              </span>

              <button
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage >= totalPages - 1 || isLoading"
                class="pagination-btn"
              >
                Next
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <div v-if="replyingTo" class="replying-indicator">
            <span class="replying-text">Replying to @{{ replyingTo.senderUsername }}</span>
            <button @click="cancelReply" class="cancel-reply-btn">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
          
          <div class="comment-input-container">
            <img :src="currentUserAvatar || defaultAvatar" :alt="currentUserName" class="input-avatar" />
            <textarea
              ref="commentInputRef"
              v-model="newComment"
              @keydown.enter.exact.prevent="handleSubmitComment"
              placeholder="Write a comment..."
              rows="1"
              class="comment-input"
            ></textarea>
            <button 
              @click="handleSubmitComment" 
              :disabled="!newComment.trim() || isSending"
              class="send-btn"
            >
              <LoadingSpinner v-if="isSending" size="sm" />
              <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>

  <transition name="modal">
    <div v-if="showDeleteModal" class="modal-overlay delete-modal-overlay" @click="showDeleteModal = false">
      <div class="delete-modal-content" @click.stop>
        <h3 class="delete-modal-title">Delete Comment</h3>
        <p class="delete-modal-message">Are you sure you want to delete this comment? This action cannot be undone.</p>
        <div class="delete-modal-actions">
          <button @click="showDeleteModal = false" class="cancel-delete-btn" :disabled="isDeleting">Cancel</button>
          <button @click="confirmDelete" class="confirm-delete-btn" :disabled="isDeleting">
            <LoadingSpinner v-if="isDeleting" size="sm" />
            <span v-else>Delete</span>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import defaultAvatar from '@/assets/images/default-avatar.png'
import { ref, computed, watch, nextTick } from 'vue'
import { commentApi } from '@/api/comment.js'
import { useAuth } from '@/composables/useAuth.js'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  postId: {
    type: Number,
    required: true
  },
  // để navigate đến comment cụ thể từ notification
  targetCommentId: {
    type: Number,
    default: null
  },
  targetRootCommentId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['close', 'commentAdded', 'commentDeleted'])

const { user } = useAuth()

const bodyRef = ref(null)
const commentInputRef = ref(null)

const comments = ref([])
const replies = ref({}) // { rootCommentId: [replies] }
const expandedReplies = ref({}) // { rootCommentId: true/false }
const loadingReplies = ref({}) // { rootCommentId: true/false }

const isLoading = ref(false)
const error = ref(null)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 50

const newComment = ref('')
const isSending = ref(false)
const replyingTo = ref(null) // comment được reply đến

const editingCommentId = ref(null)
const editContent = ref('')
const isUpdating = ref(false)
const showDeleteModal = ref(false)
const deletingComment = ref(null)
const isDeleting = ref(false)

const highlightedCommentId = ref(null)

const currentUserAvatar = computed(() => user.value?.avatar)
const currentUserName = computed(() => user.value?.username)

const fetchComments = async (page = 0) => {
  isLoading.value = true
  error.value = null

  try {
    const response = await commentApi.getCommentsByPostId(props.postId, page, pageSize)
    if (response.data.status === 200) {
      const data = response.data.data
      comments.value = data.content || []
      totalPages.value = data.totalPages || 0
      currentPage.value = page

      for (const commentId in expandedReplies.value) {
        if (expandedReplies.value[commentId] && replies.value[commentId])
          await fetchReplies(parseInt(commentId))
      }
    }
  } catch (err) {
    console.error('Error fetching comments:', err)
    error.value = 'Failed to load comments'
  } finally {
    isLoading.value = false
  }
}

const fetchReplies = async (rootCommentId) => {
  loadingReplies.value[rootCommentId] = true

  try {
    const response = await commentApi.getRepliesByRootCommentId(props.postId, rootCommentId)
    if (response.data.status === 200)
      replies.value[rootCommentId] = response.data.data || []
  } catch (err) {
    console.error('Error fetching replies:', err)
  } finally {
    loadingReplies.value[rootCommentId] = false
  }
}

const toggleReplies = async (rootCommentId) => {
  if (expandedReplies.value[rootCommentId])
    expandedReplies.value[rootCommentId] = false
  else {
    expandedReplies.value[rootCommentId] = true
    if (!replies.value[rootCommentId])
      await fetchReplies(rootCommentId)
  }
}

const goToPage = async (page) => {
  if (page >= 0 && page < totalPages.value) {
    await fetchComments(page)
    if (bodyRef.value)
      bodyRef.value.scrollTop = 0
  }
}

const handleRefresh = async () => await fetchComments(currentPage.value)

const handleReply = (comment) => {
  replyingTo.value = comment
  nextTick(() => {
    if (commentInputRef.value)
      commentInputRef.value.focus()
  })
}

const cancelReply = () => replyingTo.value = null

const isOwner = (comment) => user.value?.id === comment.senderId

const handleEdit = (comment) => {
  editingCommentId.value = comment.id
  editContent.value = comment.content
}

const handleCancelEdit = () => {
  editingCommentId.value = null
  editContent.value = ''
}

const handleSaveEdit = async (comment, rootCommentId = null) => {
  const content = editContent.value.trim()
  if (!content || isUpdating.value)
    return

  isUpdating.value = true
  error.value = null

  try {
    const response = await commentApi.updateComment(props.postId, comment.id, content)
    
    if (response.data.status === 200) {
      const updatedComment = response.data.data

      if (rootCommentId) {
        const replyList = replies.value[rootCommentId]
        if (replyList) {
          const index = replyList.findIndex(r => r.id === comment.id)
          if (index !== -1)
            replyList[index] = updatedComment
        }
      } else {
        const index = comments.value.findIndex(c => c.id === comment.id)
        if (index !== -1)
          comments.value[index] = updatedComment
      }

      editingCommentId.value = null
      editContent.value = ''
    }
  } catch (err) {
    console.error('Error updating comment:', err)
    error.value = 'Failed to update comment'
  } finally {
    isUpdating.value = false
  }
}

const handleDeleteClick = (comment, rootCommentId = null) => {
  deletingComment.value = { comment, rootCommentId }
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  if (!deletingComment.value || isDeleting.value)
    return

  const { comment, rootCommentId } = deletingComment.value
  isDeleting.value = true

  try {
    const response = await commentApi.deleteComment(props.postId, comment.id)
    
    if (response.data.status === 200) {
      showDeleteModal.value = false
      deletingComment.value = null

      emit('commentDeleted')

      if (rootCommentId) {
        const rootComment = comments.value.find(c => c.id === rootCommentId)
        if (rootComment)
          rootComment.replyCount = Math.max(0, rootComment.replyCount - 1)
        await fetchReplies(rootCommentId)
      } else
        await fetchComments(currentPage.value)
    }
  } catch (err) {
    console.error('Error deleting comment:', err)
    error.value = 'Failed to delete comment'
  } finally {
    isDeleting.value = false
  }
}

const handleSubmitComment = async () => {
  const content = newComment.value.trim()
  if (!content || isSending.value)
    return

  isSending.value = true
  error.value = null

  try {
    let response
    
    if (replyingTo.value) {
      const parentCommentId = replyingTo.value.id
      
      response = await commentApi.createComment(props.postId, content, parentCommentId)
      
      if (response.data.status === 200) {
        const rootCommentId = replyingTo.value.rootCommentId || replyingTo.value.id

        await fetchReplies(rootCommentId)

        expandedReplies.value[rootCommentId] = true

        await nextTick()
        const newReplyElement = document.getElementById(`comment-${response.data.data.id}`)
        if (newReplyElement)
          newReplyElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    } else {
      response = await commentApi.createComment(props.postId, content)
      
      if (response.data.status === 200) {
        await fetchComments(currentPage.value)
        emit('commentAdded')
      }
    }

    newComment.value = ''
    replyingTo.value = null
  } catch (err) {
    console.error('Error posting comment:', err)
    error.value = 'Failed to post comment'
  } finally {
    isSending.value = false
  }
}

const scrollToComment = async (commentId) => {
  await nextTick()
  
  const element = document.getElementById(`comment-${commentId}`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' })

    highlightedCommentId.value = commentId
    setTimeout(() => highlightedCommentId.value = null, 5000)
  }
}

const handleNotificationNavigation = async () => {
  if (!props.targetCommentId)
    return

  try {
    const pageResponse = await commentApi.getCommentPage(
      props.postId, 
      props.targetRootCommentId || props.targetCommentId, 
      pageSize
    )
    
    if (pageResponse.data.status === 200) {
      const pageData = pageResponse.data.data
      const targetPage = pageData.page

      await fetchComments(targetPage)

      if (props.targetRootCommentId) {
        await scrollToComment(props.targetRootCommentId)

        expandedReplies.value[props.targetRootCommentId] = true
        await fetchReplies(props.targetRootCommentId)

        await nextTick()
        await scrollToComment(props.targetCommentId)
      } else
        await scrollToComment(props.targetCommentId)
    }
  } catch (err) {
    console.error('Error navigating to comment:', err)
  }
}

const close = () => emit('close')

const handleOverlayClick = () => close()

const formatTime = (timestamp) => {
  if (!timestamp)
    return ''
  
  try {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (days > 7)
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    if (days > 0)
      return `${days}d ago`
    if (hours > 0)
      return `${hours}h ago`
    if (minutes > 0)
      return `${minutes}m ago`
    return 'Just now'
  } catch (e) {
    return ''
  }
}

watch(() => props.isOpen, async (isOpen) => {
  if (isOpen) {
    comments.value = []
    replies.value = {}
    expandedReplies.value = {}
    loadingReplies.value = {}
    currentPage.value = 0
    newComment.value = ''
    replyingTo.value = null
    highlightedCommentId.value = null
    
    // kiểm tra xem có navigate từ notification không
    if (props.targetCommentId)
      await handleNotificationNavigation()
    else
      await fetchComments(0)
  }
})
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, opacity;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  opacity: 0;
  transform: scale(0.95) translateY(-20px);
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
  will-change: opacity;
}

.modal-container {
  @apply bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 flex flex-col;
  max-height: 85vh;
  will-change: transform, opacity;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-2xl font-bold text-gray-900;
}

.header-actions {
  @apply flex items-center gap-2;
}

.icon-btn {
  @apply text-gray-500 hover:text-gray-700 transition-colors p-2 hover:bg-gray-100 rounded-lg;
}

.modal-body {
  @apply flex-1 overflow-y-auto p-6;
}

.loading-container {
  @apply flex justify-center py-12;
}

.error-container {
  @apply flex flex-col items-center justify-center py-12;
}

.error-text {
  @apply text-red-600 mb-3;
}

.retry-btn {
  @apply text-primary-600 hover:text-primary-700 font-medium;
}

.empty-container {
  @apply flex flex-col items-center justify-center py-12;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-3;
}

.empty-text {
  @apply text-gray-500 text-lg font-medium;
}

.empty-subtext {
  @apply text-gray-400 text-sm mt-1;
}

.comments-list {
  @apply space-y-4;
}

.comment-item {
  @apply flex gap-3 transition-colors rounded-lg p-3;
}

.comment-item.highlight {
  @apply bg-primary-50 border-2 border-primary-300;
  animation: pulse 0.5s ease-in-out 3;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.comment-avatar-link {
  @apply flex-shrink-0;
}

.reply-avatar-link {
  @apply flex-shrink-0 self-start;
}

.comment-avatar {
  @apply w-10 h-10 rounded-full object-cover border-2 border-gray-200;
}

.reply-avatar {
  @apply w-10 h-10;
}

.comment-content-wrapper {
  @apply flex-1 min-w-0;
}

.comment-bubble {
  @apply bg-gray-100 rounded-2xl px-4 py-2 inline-block max-w-full;
}

.comment-username {
  @apply font-semibold text-gray-900 hover:underline block mb-1;
}

.replying-to {
  @apply text-primary-600 font-semibold text-sm block mb-1;
}

.comment-text {
  @apply text-gray-800 break-words whitespace-pre-wrap;
}

.comment-meta {
  @apply flex items-center gap-3 mt-1 px-3;
}

.comment-time {
  @apply text-xs text-gray-500;
}

.edited-label {
  @apply text-xs text-gray-400 italic;
}

.reply-btn,
.edit-btn,
.delete-btn {
  @apply text-xs text-gray-600 hover:text-primary-600 font-medium transition-colors;
}

.delete-btn {
  @apply hover:text-red-600;
}

.view-replies-btn {
  @apply flex items-center gap-1.5 text-sm text-gray-600 hover:text-primary-600 font-medium mt-2 ml-12 transition-colors;
}

.view-replies-btn svg {
  @apply flex-shrink-0;
}

.view-replies-btn span {
  @apply leading-none whitespace-nowrap;
}

.replies-list {
  @apply mt-3 space-y-3 pl-12 border-l-2 border-gray-200 ml-0.5;
}

.replies-loading {
  @apply flex justify-center py-4;
}

.reply-item {
  @apply flex gap-3 transition-colors rounded-lg p-2;
}

.reply-item.highlight {
  @apply bg-primary-50 border-2 border-primary-300;
  animation: pulse 0.5s ease-in-out 3;
}

.edit-mode {
  @apply flex-1;
}

.edit-textarea {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent;
}

.edit-actions {
  @apply flex items-center gap-2 mt-2;
}

.cancel-edit-btn {
  @apply px-3 py-1.5 text-sm text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors;
}

.save-edit-btn {
  @apply px-3 py-1.5 text-sm text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-1;
}

.modal-footer {
  @apply border-t border-gray-200 bg-gray-50 p-4;
}

.replying-indicator {
  @apply flex items-center justify-between bg-primary-50 border-l-4 border-primary-500 px-4 py-2 mb-3 rounded;
}

.replying-text {
  @apply text-sm text-primary-700 font-medium;
}

.cancel-reply-btn {
  @apply text-primary-600 hover:text-primary-800 p-1 hover:bg-primary-100 rounded transition-colors;
}

.comment-input-container {
  @apply flex items-end gap-3;
}

.input-avatar {
  @apply w-10 h-10 rounded-full object-cover border-2 border-gray-200 flex-shrink-0;
}

.comment-input {
  @apply flex-1 px-4 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent min-h-[40px] max-h-[120px];
}

.send-btn {
  @apply flex-shrink-0 w-10 h-10 flex items-center justify-center bg-primary-600 text-white rounded-full hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors;
}

.delete-modal-overlay {
  z-index: 60;
}

.delete-modal-content {
  @apply bg-white rounded-xl shadow-2xl p-6 max-w-md w-full mx-4;
  will-change: transform, opacity;
}

.delete-modal-title {
  @apply text-xl font-bold text-gray-900 mb-3;
}

.delete-modal-message {
  @apply text-gray-700 mb-6;
}

.delete-modal-actions {
  @apply flex items-center justify-end gap-3;
}

.cancel-delete-btn {
  @apply px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors;
}

.confirm-delete-btn {
  @apply px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2;
}
</style>

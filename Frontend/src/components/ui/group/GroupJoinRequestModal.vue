<template>
  <Transition name="modal">
    <div v-if="show && request" class="modal-overlay" @click="emit('close')">
      <div class="modal-dialog" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Join Request</h3>
          <button @click="emit('close')" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <LoadingSpinner v-if="isLoading" size="md" class="my-4" />

          <template v-else>
            <div class="user-section">
              <img
                :src="request.senderAvatar || defaultAvatar"
                :alt="request.senderUsername"
                class="user-avatar"
                @error="(e) => e.target.src = defaultAvatar"
              />
              <div class="user-info">
                <h4 class="user-name">{{ request.senderUsername }}</h4>
                <p class="user-time">Submitted {{ formatTime(request.submittedAt) }}</p>
              </div>
            </div>

            <div v-if="answers.length > 0" class="answers-section">
              <h4 class="answers-title">Responses</h4>
              <div class="answers-list">
                <div v-for="answer in answers" :key="answer.questionId" class="answer-item">
                  <div class="question">
                    <span class="question-text">{{ answer.questionText }}</span>
                    <span v-if="answer.isRequired" class="required-badge">Required</span>
                  </div>
                  <p class="answer-text">{{ answer.answerText || '(No answer provided)' }}</p>
                </div>
              </div>
            </div>

            <div v-else class="no-answers">
              <p class="no-answers-text">No form responses (direct join request)</p>
            </div>
          </template>
        </div>

        <div class="modal-footer">
          <button
            @click="handleReject"
            :disabled="isSubmitting"
            class="btn-reject"
          >
            {{ isSubmitting && action === 'reject' ? 'Rejecting...' : 'Reject' }}
          </button>
          <button
            @click="handleApprove"
            :disabled="isSubmitting"
            class="btn-approve"
          >
            {{ isSubmitting && action === 'approve' ? 'Approving...' : 'Approve' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import defaultAvatar from '@/assets/images/default-avatar.png'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { groupJoinRequestApi } from '@/api/groupJoinRequest.js'
import { groupJoinRequestAnswerApi } from '@/api/groupJoinRequestAnswer.js'
import { formatTime } from '@/utils/formatTime'

const props = defineProps({
  show: Boolean,
  groupId: Number,
  request: Object
})

const emit = defineEmits(['close', 'approved', 'rejected'])

const isLoading = ref(false)
const isSubmitting = ref(false)
const action = ref('')
const answers = ref([])

watch(() => props.show, async (newVal) => {
  if (newVal && props.request && props.groupId)
    await loadAnswers()
})

const loadAnswers = async () => {
  isLoading.value = true
  answers.value = []

  try {
    const response = await groupJoinRequestAnswerApi.getAnswers(
      props.groupId,
      props.request.id
    )
    if (response.data.status === 200)
      answers.value = response.data.data || []
  } catch (error) {
    console.error('Error loading answers:', error)
    if (error.response?.status === 404)
      answers.value = []
  } finally {
    isLoading.value = false
  }
}

const handleApprove = async () => {
  if (isSubmitting.value)
    return

  isSubmitting.value = true
  action.value = 'approve'

  try {
    const response = await groupJoinRequestApi.approveGroupJoinRequest(
      props.groupId,
      props.request.id
    )
    if (response.data.status === 200)
      emit('approved', props.request.id)
  } catch (error) {
    console.error('Error approving request:', error)
    alert('Failed to approve request. Please try again.')
  } finally {
    isSubmitting.value = false
    action.value = ''
  }
}

const handleReject = async () => {
  if (isSubmitting.value)
    return

  if (!confirm('Are you sure you want to reject this request?'))
    return

  isSubmitting.value = true
  action.value = 'reject'

  try {
    const response = await groupJoinRequestApi.rejectGroupJoinRequest(
      props.groupId,
      props.request.id
    )
    if (response.data.status === 200)
      emit('rejected', props.request.id)
  } catch (error) {
    console.error('Error rejecting request:', error)
    alert('Failed to reject request. Please try again.')
  } finally {
    isSubmitting.value = false
    action.value = ''
  }
}
</script>

<style scoped>
.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col;
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
  @apply flex-1 overflow-y-auto p-6 space-y-6;
}

.user-section {
  @apply flex items-center gap-4 p-4 bg-gray-50 rounded-lg;
}

.user-avatar {
  @apply w-16 h-16 rounded-full object-cover border-2 border-gray-200;
}

.user-info {
  @apply flex-1;
}

.user-name {
  @apply text-lg font-bold text-gray-900;
}

.user-time {
  @apply text-sm text-gray-500;
}

.answers-section {
  @apply space-y-4;
}

.answers-title {
  @apply text-lg font-bold text-gray-900 mb-4;
}

.answers-list {
  @apply space-y-4;
}

.answer-item {
  @apply p-4 border border-gray-200 rounded-lg space-y-2;
}

.question {
  @apply flex items-center gap-2 flex-wrap;
}

.question-text {
  @apply font-medium text-gray-900;
}

.required-badge {
  @apply px-2 py-0.5 bg-red-100 text-red-700 text-xs font-semibold rounded-full;
}

.answer-text {
  @apply text-gray-700 whitespace-pre-wrap;
}

.no-answers {
  @apply p-8 bg-gray-50 rounded-lg text-center;
}

.no-answers-text {
  @apply text-gray-500;
}

.modal-footer {
  @apply flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50;
}

.btn-reject {
  @apply px-6 py-2.5 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.btn-approve {
  @apply px-6 py-2.5 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
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

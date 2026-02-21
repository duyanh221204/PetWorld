<template>
  <div class="questions-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="questions-container">
        <div class="page-header">
          <button @click="goBack" class="back-button">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
            Back
          </button>
          <h1 class="page-title">{{ formTitle }}</h1>
        </div>

        <LoadingSpinner v-if="isLoading" size="lg" class="my-8" />

        <template v-else>
          <div class="questions-section">
            <div class="section-header">
              <h2 class="section-title">Questions ({{ questions.length }})</h2>
              <button v-if="canEdit" @click="showAddModal = true" class="btn-add">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                </svg>
                Add Question
              </button>
            </div>

            <div v-if="questions.length === 0" class="empty-state">
              <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <p class="empty-text">No questions yet</p>
            </div>

            <draggable
              v-else
              v-model="questions"
              item-key="id"
              @end="handleReorder"
              :disabled="!canReorder"
              class="questions-list"
              :animation="200"
            >
              <template #item="{ element, index }">
                <div class="question-item">
                  <div v-if="canReorder" class="drag-handle">
                    <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z"/>
                    </svg>
                  </div>

                  <div class="question-content">
                    <div class="question-header">
                      <span class="question-number">{{ index + 1 }}.</span>
                      <p class="question-text">{{ element.questionText }}</p>
                      <span v-if="element.isRequired" class="required-badge">Required</span>
                    </div>
                  </div>

                  <div v-if="canEdit" class="question-actions">
                    <button @click="editQuestion(element)" class="btn-icon">
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>
                    <button @click="deletingQuestionId = element.id" class="btn-icon text-red-600">
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </template>
      </div>

      <GroupRightSidebar :groupId="groupId" />
    </main>

    <AppFooter />

    <Transition name="modal">
      <div v-if="showAddModal || editingQuestion" class="modal-overlay" @click="closeQuestionModal">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">{{ editingQuestion ? 'Edit Question' : 'Add Question' }}</h3>
            <button @click="closeQuestionModal" class="modal-close">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <form @submit.prevent="handleSaveQuestion">
              <div class="form-group">
                <label class="form-label">Question Text *</label>
                <textarea
                  v-model="questionFormData.questionText"
                  placeholder="Enter question text"
                  rows="3"
                  class="form-textarea"
                  required
                ></textarea>
              </div>

              <div class="form-group">
                <label class="checkbox-label">
                  <input v-model="questionFormData.isRequired" type="checkbox" class="checkbox" />
                  <span>Required (user must answer this question)</span>
                </label>
              </div>

              <div v-if="questionError" class="error-message">{{ questionError }}</div>

              <div class="form-actions">
                <button type="button" @click="closeQuestionModal" class="btn-secondary">Cancel</button>
                <button type="submit" :disabled="isSubmittingQuestion" class="btn-primary">
                  {{ isSubmittingQuestion ? 'Saving...' : editingQuestion ? 'Update' : 'Add' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="modal">
      <div v-if="deletingQuestionId" class="modal-overlay" @click="deletingQuestionId = null">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">Delete Question</h3>
            <button @click="deletingQuestionId = null" class="modal-close">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <p class="text-gray-700 mb-6">Are you sure you want to delete this question? This action cannot be undone.</p>
            
            <div class="form-actions">
              <button type="button" @click="deletingQuestionId = null" class="btn-secondary">No</button>
              <button type="button" @click="deleteQuestion" class="btn-danger">Yes</button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import draggable from 'vuedraggable'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import GroupRightSidebar from '@/components/ui/group/GroupRightSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { groupApi } from '@/api/group'
import { groupJoinFormApi } from '@/api/groupJoinForm'
import { groupJoinFormQuestionApi } from '@/api/groupJoinFormQuestion'

const route = useRoute()
const router = useRouter()

const groupId = computed(() => parseInt(route.params.groupId))

const formId = computed(() => parseInt(route.params.formId))

const isLoading = ref(true)
const group = ref(null)
const formTitle = ref('')
const questions = ref([])
const showAddModal = ref(false)
const editingQuestion = ref(null)
const isSubmittingQuestion = ref(false)
const questionError = ref('')

const questionFormData = ref({
  questionText: '',
  isRequired: false
})

const deletingQuestionId = ref(null)

const canEdit = computed(() => group.value?.currentUserRole === 'OWNER')
const canReorder = computed(() => 
  group.value?.currentUserRole === 'OWNER' || group.value?.currentUserRole === 'ADMIN'
)

const loadGroup = async () => {
  try {
    const response = await groupApi.getGroupById(groupId.value)
    if (response.data.status === 200)
      group.value = response.data.data
  } catch (error) {
    console.error('Error loading group:', error)
  }
}

const loadForm = async () => {
  try {
    const response = await groupJoinFormApi.getGroupJoinForms(groupId.value)
    if (response.data.status === 200) {
      const form = response.data.data.find(f => f.id === formId.value)
      if (form)
        formTitle.value = form.title
    }
  } catch (error) {
    console.error('Error loading form:', error)
  }
}

const loadQuestions = async () => {
  isLoading.value = true
  try {
    const response = await groupJoinFormQuestionApi.getGroupJoinFormQuestions(
      groupId.value,
      formId.value
    )
    if (response.data.status === 200)
      questions.value = response.data.data.sort((a, b) => a.questionOrder - b.questionOrder)
  } catch (error) {
    console.error('Error loading questions:', error)
  } finally {
    isLoading.value = false
  }
}

const handleSaveQuestion = async () => {
  questionError.value = ''
  isSubmittingQuestion.value = true

  if (!formId.value) {
    questionError.value = 'Invalid form ID. Please refresh the page.'
    isSubmittingQuestion.value = false
    return
  }

  try {
    const payload = {
      questionText: questionFormData.value.questionText.trim(),
      isRequired: questionFormData.value.isRequired,
      questionOrder: editingQuestion.value ? editingQuestion.value.questionOrder : questions.value.length + 1
    }

    let response
    if (editingQuestion.value)
      response = await groupJoinFormQuestionApi.updateGroupJoinFormQuestion(
        groupId.value,
        formId.value,
        editingQuestion.value.id,
        payload
      )
    else
      response = await groupJoinFormQuestionApi.createGroupJoinFormQuestion(
        groupId.value,
        formId.value,
        payload
      )

    if (response.data.status === 200) {
      await loadQuestions()
      closeQuestionModal()
    } else
      questionError.value = response.data.message || 'Failed to save question'
  } catch (error) {
    console.error('Error saving question:', error)
    questionError.value = error.response?.data?.message || 'Failed to save question. Please try again.'
  } finally {
    isSubmittingQuestion.value = false
  }
}

const editQuestion = (question) => {
  editingQuestion.value = question
  questionFormData.value.questionText = question.questionText
  questionFormData.value.isRequired = question.isRequired
}

const closeQuestionModal = () => {
  showAddModal.value = false
  editingQuestion.value = null
  questionFormData.value = { questionText: '', isRequired: false }
  questionError.value = ''
}

const deleteQuestion = async () => {
  if (!deletingQuestionId.value)
    return
  
  try {
    const response = await groupJoinFormQuestionApi.deleteGroupJoinFormQuestion(
      groupId.value,
      formId.value,
      deletingQuestionId.value
    )
    if (response.data.status === 200) {
      await loadQuestions()
      deletingQuestionId.value = null
    } else {
      alert(response.data.message || 'Failed to delete question.')
      deletingQuestionId.value = null
    }
  } catch (error) {
    console.error('Error deleting question:', error)
    alert(error.response?.data?.message || 'Failed to delete question. Please try again.')
    deletingQuestionId.value = null
  }
}

const handleReorder = async () => {
  if (!canReorder.value)
    return

  const updates = questions.value.map((q, index) => ({
    id: q.id,
    questionOrder: index + 1
  }))

  try {
    await groupJoinFormQuestionApi.updateGroupJoinFormQuestionOrders(
      groupId.value,
      formId.value,
      updates
    )
  } catch (error) {
    console.error('Error reordering questions:', error)
  }
}

const goBack = () => router.push({ name: 'GroupDetail', params: { groupId: groupId.value } })

onMounted(async () => {
  await loadGroup()
  await loadForm()
  await loadQuestions()
})
</script>

<style scoped>
.questions-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.questions-container {
  @apply max-w-3xl mx-auto px-4 space-y-6;
}

.page-header {
  @apply bg-white rounded-xl shadow-sm p-6;
}

.back-button {
  @apply flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors mb-4;
}

.page-title {
  @apply text-2xl font-bold text-gray-900;
}

.questions-section {
  @apply bg-white rounded-xl shadow-sm p-6;
}

.section-header {
  @apply flex items-center justify-between mb-6;
}

.section-title {
  @apply text-xl font-bold text-gray-900;
}

.btn-add {
  @apply flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16;
}

.empty-icon {
  @apply w-16 h-16 text-gray-300 mb-3;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

.questions-list {
  @apply space-y-3;
}

.question-item {
  @apply flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:border-primary-300 transition-colors bg-white;
}

.drag-handle {
  @apply cursor-move text-gray-400 hover:text-gray-600 flex-shrink-0;
}

.question-content {
  @apply flex-1 min-w-0;
}

.question-header {
  @apply flex items-center gap-2;
}

.question-number {
  @apply font-semibold text-gray-700 flex-shrink-0;
}

.question-text {
  @apply flex-1 text-gray-900;
}

.required-badge {
  @apply px-2.5 py-1 bg-red-100 text-red-700 text-xs font-semibold rounded-full flex-shrink-0;
}

.question-actions {
  @apply flex items-center gap-2 flex-shrink-0;
}

.btn-icon {
  @apply p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-100 rounded-lg transition-colors;
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[90vh] overflow-hidden flex flex-col;
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
  @apply flex-1 overflow-y-auto p-6;
}

.form-group {
  @apply space-y-2 mb-4;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.form-textarea {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none;
}

.checkbox-label {
  @apply flex items-center gap-2 cursor-pointer;
}

.checkbox {
  @apply w-4 h-4 text-primary-600 rounded focus:ring-2 focus:ring-primary-500;
}

.error-message {
  @apply bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm mb-4;
}

.form-actions {
  @apply flex items-center justify-end gap-3;
}

.btn-secondary {
  @apply px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors;
}

.btn-primary {
  @apply px-6 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors;
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

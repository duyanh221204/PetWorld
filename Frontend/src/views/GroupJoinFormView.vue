<template>
  <div class="join-form-view">
    <AppHeader />

    <main class="main-content">
      <LeftSidebar />

      <div class="form-container">
        <LoadingSpinner v-if="isLoading" size="lg" class="my-8" />

        <template v-else-if="formData">
          <div class="form-card">
            <div class="form-header">
              <h1 class="form-title">{{ formData.title }}</h1>
              <p class="form-description">Please answer the following questions to join this group</p>
            </div>

            <form @submit.prevent="handleSubmit" class="questions-form">
              <div
                v-for="(question, index) in questions"
                :key="question.id"
                class="question-group"
              >
                <label class="question-label">
                  <span class="question-number">{{ index + 1 }}.</span>
                  <span class="question-text">{{ question.questionText }}</span>
                  <span v-if="question.isRequired" class="required-star">*</span>
                </label>
                <textarea
                  v-model="answers[question.id]"
                  :placeholder="question.isRequired ? 'Your answer (required)' : 'Your answer (optional)'"
                  rows="4"
                  class="answer-textarea"
                  :class="{ 'error': errors[question.id] }"
                ></textarea>
                <p v-if="errors[question.id]" class="error-text">{{ errors[question.id] }}</p>
              </div>

              <div v-if="submitError" class="error-message">
                {{ submitError }}
              </div>

              <div class="form-actions">
                <button type="button" @click="handleCancel" class="btn-cancel">
                  Cancel
                </button>
                <button type="submit" :disabled="isSubmitting" class="btn-submit">
                  {{ isSubmitting ? 'Submitting...' : 'Submit Request' }}
                </button>
              </div>
            </form>
          </div>
        </template>

        <div v-else class="error-state">
          <p>Form not found or no longer available</p>
          <button @click="goBack" class="btn-primary mt-4">
            Go Back
          </button>
        </div>
      </div>

      <RightSidebar />
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LeftSidebar from '@/components/layout/sidebar/LeftSidebar.vue'
import RightSidebar from '@/components/layout/sidebar/RightSidebar.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { groupJoinFormApi } from '@/api/groupJoinForm'
import { groupJoinFormQuestionApi } from '@/api/groupJoinFormQuestion'
import { groupJoinRequestApi } from '@/api/groupJoinRequest'

const route = useRoute()
const router = useRouter()

const groupId = computed(() => parseInt(route.params.groupId))

const isLoading = ref(true)
const isSubmitting = ref(false)
const formData = ref(null)
const questions = ref([])
const answers = ref({})
const errors = ref({})
const submitError = ref('')

const loadForm = async () => {
  isLoading.value = true

  try {
    const formResponse = await groupJoinFormApi.getActiveGroupJoinForm(groupId.value)
    if (formResponse.data.status === 200) {
      formData.value = formResponse.data.data

      const questionsResponse = await groupJoinFormQuestionApi.getGroupJoinFormQuestions(
        groupId.value,
        formData.value.id
      )
      if (questionsResponse.data.status === 200) {
        questions.value = questionsResponse.data.data.sort((a, b) => a.questionOrder - b.questionOrder)
        questions.value.forEach(q => answers.value[q.id] = '')
      }
    }
  } catch (error) {
    console.error('Error loading form:', error)
    if (error.response?.status === 404)
      formData.value = null
  } finally {
    isLoading.value = false
  }
}

const validateForm = () => {
  errors.value = {}
  let isValid = true

  questions.value.forEach(question => {
    if (question.isRequired && !answers.value[question.id]?.trim()) {
      errors.value[question.id] = 'This question is required'
      isValid = false
    }
  })

  return isValid
}

const handleSubmit = async () => {
  submitError.value = ''

  if (!validateForm()) {
    submitError.value = 'Please answer all required questions'
    return
  }

  isSubmitting.value = true

  try {
    const answersArray = questions.value.map(question => ({
      questionId: question.id,
      answerText: answers.value[question.id]?.trim() || null
    }))

    const response = await groupJoinRequestApi.createGroupJoinRequest(
      groupId.value,
      answersArray
    )

    if (response.data.status === 200)
      await router.push({name: 'GroupDetail', params: {groupId: groupId.value}})
    else
      submitError.value = response.data.message || 'Failed to submit request'
  } catch (error) {
    console.error('Error submitting request:', error)
    submitError.value = error.response?.data?.message || 'Failed to submit request. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}

const handleCancel = () => {
  if (confirm('Are you sure you want to cancel? Your answers will not be saved.'))
    goBack()
}

const goBack = () => router.push({ name: 'GroupDetail', params: { groupId: groupId.value } })

onMounted(async () => await loadForm())
</script>

<style scoped>
.join-form-view {
  @apply min-h-screen flex flex-col bg-gray-50;
}

.main-content {
  @apply flex-1 py-4;
}

.form-container {
  @apply max-w-3xl mx-auto px-4;
}

.form-card {
  @apply bg-white rounded-xl shadow-sm overflow-hidden;
}

.form-header {
  @apply p-6 border-b border-gray-200 bg-gradient-to-r from-primary-50 to-primary-100;
}

.form-title {
  @apply text-2xl font-bold text-gray-900 mb-2;
}

.form-description {
  @apply text-gray-600;
}

.questions-form {
  @apply p-6 space-y-6;
}

.question-group {
  @apply space-y-2;
}

.question-label {
  @apply flex items-start gap-2 text-gray-900 font-medium;
}

.question-number {
  @apply flex-shrink-0;
}

.question-text {
  @apply flex-1;
}

.required-star {
  @apply text-red-500 flex-shrink-0;
}

.answer-textarea {
  @apply w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none transition-shadow;
}

.answer-textarea.error {
  @apply border-red-500 focus:ring-red-500;
}

.error-text {
  @apply text-xs text-red-600;
}

.error-message {
  @apply p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm;
}

.form-actions {
  @apply flex items-center justify-end gap-3 pt-4 border-t border-gray-200;
}

.btn-cancel {
  @apply px-6 py-2.5 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition-colors;
}

.btn-submit {
  @apply px-6 py-2.5 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors;
}

.error-state {
  @apply flex flex-col items-center justify-center py-16 bg-white rounded-xl shadow-sm;
}

.btn-primary {
  @apply px-6 py-2.5 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors;
}
</style>

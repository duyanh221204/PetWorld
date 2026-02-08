<template>
  <div class="register-view">
    <AppHeader />

    <main class="main-content">
      <div class="form-container">
        <div class="form-card">
          <div class="form-header">
            <h1 class="form-title">Create Account</h1>
            <p class="form-subtitle">Join the PetWorld community today</p>
          </div>

          <form @submit.prevent="handleSubmit" class="form-body">
            <div class="form-group">
              <label class="form-label">
                Username <span class="required">*</span>
              </label>
              <input
                  ref="usernameInput"
                  v-model="form.username"
                  type="text"
                  placeholder="Enter username (2-15 characters)"
                  class="input-field"
                  :class="{ 'input-error': showError('username') }"
                  @blur="handleUsernameBlur"
              />
              <p v-if="showError('username')" class="error-text">
                {{ errors.username }}
              </p>
            </div>

            <div class="form-group">
              <label class="form-label">
                Email <span class="required">*</span>
              </label>
              <input
                  ref="emailInput"
                  v-model="form.email"
                  type="email"
                  placeholder="Enter your email"
                  class="input-field"
                  :class="{ 'input-error': showError('email') }"
                  @blur="handleEmailBlur"
              />
              <p v-if="showError('email')" class="error-text">
                {{ errors.email }}
              </p>
            </div>

            <div class="form-group">
              <label class="form-label">
                Password <span class="required">*</span>
              </label>
              <input
                  ref="passwordInput"
                  v-model="form.password"
                  type="password"
                  placeholder="Enter password (6-20 characters)"
                  class="input-field"
                  :class="{ 'input-error': showError('password') }"
                  @blur="handlePasswordBlur"
              />
              <p v-if="showError('password')" class="error-text">
                {{ errors.password }}
              </p>
            </div>

            <div class="form-group">
              <label class="form-label">
                Confirm Password <span class="required">*</span>
              </label>
              <input
                  ref="confirmPasswordInput"
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="Re-enter password"
                  class="input-field"
                  :class="{ 'input-error': showError('confirmPassword') }"
                  @blur="handleConfirmPasswordBlur"
              />
              <p v-if="showError('confirmPassword')" class="error-text">
                {{ errors.confirmPassword }}
              </p>
            </div>

            <div class="form-group">
              <label class="form-label">
                Avatar <span class="optional">(optional)</span>
              </label>
              <div class="avatar-section">
                <div v-if="avatarPreview" class="avatar-preview-wrapper">
                  <img
                      :src="avatarPreview"
                      alt="Avatar preview"
                      class="avatar-preview"
                  />
                  <button
                      type="button"
                      @click="removeAvatar"
                      class="avatar-remove-btn"
                  >
                    ×
                  </button>
                </div>
                <div class="avatar-upload-wrapper">
                  <input
                      type="file"
                      accept="image/*"
                      @change="handleAvatarChange"
                      class="hidden"
                      ref="avatarInput"
                  />
                  <button
                      type="button"
                      @click="$refs.avatarInput.click()"
                      class="avatar-upload-btn"
                      :disabled="isUploading || isSubmitting"
                  >
                    <span v-if="isUploading">Uploading...</span>
                    <span v-else>
                      {{ avatarPreview ? 'Change Photo' : 'Choose Avatar' }}
                    </span>
                  </button>
                </div>
              </div>
              <p v-if="uploadError" class="error-text">{{ uploadError }}</p>
            </div>

            <div class="form-group">
              <label class="form-label">
                About Yourself <span class="optional">(optional)</span>
              </label>
              <textarea
                  v-model="form.description"
                  rows="3"
                  placeholder="Tell us about you and your pets..."
                  class="input-field textarea-field"
              ></textarea>
            </div>

            <div v-if="apiError" class="api-error">
              {{ apiError }}
            </div>

            <button
                type="submit"
                :disabled="!isFormValid || isSubmitting"
                class="btn-primary submit-btn"
            >
              <LoadingSpinner v-if="isSubmitting" size="sm" class="spinner" />
              {{ isSubmitting ? 'Signing up...' : 'Sign Up' }}
            </button>
          </form>

          <div class="form-footer">
            <p class="footer-text">
              Already have an account?
              <router-link to="/login" class="footer-link">
                Sign In
              </router-link>
            </p>
          </div>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { userApi } from '@/api/user'
import { uploadApi } from '@/api/upload'
import { emailApi } from '@/api/email'

const router = useRouter()
const usernameInput = ref(null)
const emailInput = ref(null)
const passwordInput = ref(null)
const confirmPasswordInput = ref(null)
const avatarInput = ref(null)

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  avatar: '',
  description: ''
})

const errors = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const touched = ref({
  username: false,
  email: false,
  password: false,
  confirmPassword: false
})

const avatarPreview = ref(null)
const avatarFile = ref(null)
const isUploading = ref(false)
const uploadError = ref('')
const isSubmitting = ref(false)
const apiError = ref('')

const showError = (field) => touched.value[field] && !!errors.value[field]

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const validateUsername = () => {
  const username = form.value.username.trim()
  if (!username)
    errors.value.username = 'Username is required'
  else if (username.length < 2 || username.length > 15)
    errors.value.username = 'Username must be 2-15 characters'
  else
    errors.value.username = ''
}

const validateEmail = () => {
  const email = form.value.email.trim()
  if (!email)
    errors.value.email = 'Email is required'
  else if (!emailRegex.test(email))
    errors.value.email = 'Invalid email format'
  else
    errors.value.email = ''
}

const validatePassword = () => {
  const password = form.value.password
  if (!password)
    errors.value.password = 'Password is required'
  else if (password.length < 6 || password.length > 20)
    errors.value.password = 'Password must be 6-20 characters'
  else
    errors.value.password = ''
}

const validateConfirmPassword = () => {
  if (!form.value.confirmPassword)
    errors.value.confirmPassword = 'Please re-enter password'
  else if (form.value.password !== form.value.confirmPassword)
    errors.value.confirmPassword = 'Passwords do not match'
  else
    errors.value.confirmPassword = ''
}

const handleUsernameBlur = () => {
  touched.value.username = true
  validateUsername()
}

const handleEmailBlur = () => {
  touched.value.email = true
  validateEmail()
}

const handlePasswordBlur = () => {
  touched.value.password = true
  validatePassword()
}

const handleConfirmPasswordBlur = () => {
  touched.value.confirmPassword = true
  validateConfirmPassword()
}

// watch: chỉ validate lại những field này sau khi touched
watch(
    () => form.value.username,
    () => {
      if (touched.value.username)
        validateUsername()
    }
)

watch(
    () => form.value.email,
    () => {
      if (touched.value.email)
        validateEmail()
    }
)

watch(
    () => form.value.password,
    () => {
      if (touched.value.password)
        validatePassword()
    }
)

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file)
    return

  avatarFile.value = file
  avatarPreview.value = URL.createObjectURL(file)
  uploadError.value = ''
  form.value.avatar = ''

  isUploading.value = true
  try {
    const response = await uploadApi.uploadFile(file)

    if (response.data.status === 200)
      form.value.avatar = response.data.data
    else {
      uploadError.value = response.data.message || 'Upload failed'
      form.value.avatar = ''
    }
  } catch (error) {
    uploadError.value = error.response?.data?.message || error.message || 'Upload failed'
    form.value.avatar = ''
  } finally {
    isUploading.value = false
  }
}

const removeAvatar = () => {
  avatarPreview.value = null
  avatarFile.value = null
  form.value.avatar = ''
  uploadError.value = ''

  if (avatarInput.value)
    avatarInput.value.value = ''
}

const isFormValid = computed(() => {
  const username = form.value.username.trim()
  const email = form.value.email.trim()

  const requiredOk =
      username.length >= 2 &&
      username.length <= 15 &&
      emailRegex.test(email) &&
      form.value.password.length >= 6 &&
      form.value.password.length <= 20 &&
      form.value.confirmPassword.length > 0

  const avatarReady = !avatarFile.value || (form.value.avatar && !isUploading.value)

  return requiredOk && avatarReady && !isUploading.value
})

const focusFirstError = async () => {
  await nextTick()

  if (errors.value.username)
    return usernameInput.value?.focus()
  if (errors.value.email)
    return emailInput.value?.focus()
  if (errors.value.password)
    return passwordInput.value?.focus()
  if (errors.value.confirmPassword)
    return confirmPasswordInput.value?.focus()
}

const handleSubmit = async () => {
  // bật touched hết để show lỗi nếu sai
  touched.value.username = true
  touched.value.email = true
  touched.value.password = true
  touched.value.confirmPassword = true

  validateUsername()
  validateEmail()
  validatePassword()
  validateConfirmPassword()

  const hasError = Object.values(errors.value).some((e) => !!e)
  if (hasError) {
    await focusFirstError()
    return
  }

  if (isUploading.value)
    return

  isSubmitting.value = true
  apiError.value = ''

  try {
    const requestData = {
      username: form.value.username.trim(),
      email: form.value.email.trim(),
      password: form.value.password
    }

    if (form.value.avatar)
      requestData.avatar = form.value.avatar
    if (form.value.description.trim())
      requestData.description = form.value.description.trim()

    const response = await userApi.register(requestData)
    if (response.data.status === 200) {
      const email = form.value.email.trim()

      await emailApi.sendVerificationCode(email)

      // lưu session để VerifyOTP không auto-send lại khi refresh
      sessionStorage.setItem(`otp_sent_${email}`, '1')
      sessionStorage.setItem(`otp_sent_at_${email}`, String(Date.now()))

      await router.push({
        name: 'VerifyOTP',
        query: { email }
      })
    } else
      apiError.value = response.data.message || 'Registration failed.'
  } catch (error) {
    apiError.value = error.response?.data?.message || 'Registration failed. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.register-view {
  @apply min-h-screen flex flex-col bg-gradient-to-br from-primary-50 to-white;
}

.main-content {
  @apply flex-1 flex items-center justify-center px-4 py-12;
}

.form-container {
  @apply w-full max-w-md;
}

.form-card {
  @apply bg-white rounded-2xl shadow-xl p-8;
}

.form-header {
  @apply text-center mb-8;
}

.form-title {
  @apply text-3xl font-bold text-gray-900;
}

.form-subtitle {
  @apply text-gray-500 mt-2;
}

.form-body {
  @apply space-y-5;
}

.form-group {
  @apply space-y-1;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.required {
  @apply text-red-500;
}

.optional {
  @apply text-gray-400;
}

.input-field {
  @apply w-full px-4 py-3 border border-gray-200 rounded-xl
  focus:outline-none focus:ring-2 focus:ring-primary-300 focus:border-primary-400
  transition-all;
}

.input-error {
  @apply border-red-400 focus:ring-red-200 focus:border-red-400;
}

.error-text {
  @apply text-sm text-red-500;
}

.textarea-field {
  @apply resize-none;
}

.avatar-section {
  @apply flex flex-col items-center space-y-3;
}

.avatar-preview-wrapper {
  @apply relative;
}

.avatar-preview {
  @apply w-20 h-20 rounded-full object-cover border-2 border-primary-200;
}

.avatar-remove-btn {
  @apply absolute -top-1 -right-1 bg-red-500 text-white rounded-full w-5 h-5
  flex items-center justify-center text-xs hover:bg-red-600 transition-colors;
}

.avatar-upload-wrapper {
  @apply w-full;
}

.avatar-upload-btn {
  @apply w-full px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg
  text-gray-500 hover:border-primary-400 hover:text-primary-600 transition-colors
  disabled:opacity-50 disabled:cursor-not-allowed;
}

.api-error {
  @apply bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm;
}

.submit-btn {
  @apply w-full flex items-center justify-center py-3 rounded-xl;
}

.spinner {
  @apply mr-2;
}

.form-footer {
  @apply mt-6 text-center;
}

.footer-text {
  @apply text-gray-600;
}

.footer-link {
  @apply text-primary-600 font-semibold hover:underline;
}
</style>

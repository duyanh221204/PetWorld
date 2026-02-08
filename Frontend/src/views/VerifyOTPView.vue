<template>
  <div class="verify-otp-view">
    <AppHeader />

    <main class="main-content">
      <div class="form-container">
        <div class="form-card">
          <div class="form-header">
            <div class="icon-wrapper">
              <svg class="icon-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <h1 class="form-title">Verify Email</h1>
            <p class="form-subtitle">
              Verification code has been sent to
              <span class="email-highlight">{{ email }}</span>
            </p>
          </div>

          <div class="form-body">
            <OTPInput
                v-model="otpCode"
                :length="6"
                :has-error="!!otpError"
            />

            <p v-if="otpError" class="error-message">
              {{ otpError }}
            </p>

            <p v-if="successMessage" class="success-message">
              {{ successMessage }}
            </p>

            <button
                @click="handleVerify"
                :disabled="!canVerify"
                class="btn-primary verify-btn"
            >
              <LoadingSpinner v-if="isVerifying" size="sm" class="spinner" />
              {{ isVerifying ? 'Verifying...' : 'Verify' }}
            </button>

            <div class="resend-section">
              <p class="resend-label">Didn't receive the code?</p>
              <button
                  @click="handleResend"
                  :disabled="resendCountdown > 0 || isResending"
                  class="resend-btn"
              >
                <span v-if="isResending">Sending...</span>
                <span v-else-if="resendCountdown > 0">
                  Resend in {{ formatTime(resendCountdown) }}
                </span>
                <span v-else>Resend Code</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import OTPInput from '@/components/ui/OTPInput.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { emailApi } from '@/api/email'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const email = ref(route.query.email || '')
const otpCode = ref('')
const otpError = ref('')
const successMessage = ref('')
const isVerifying = ref(false)
const isResending = ref(false)
const resendCountdown = ref(0)
const hasVerificationFailed = ref(false)

let countdownInterval = null

const RESEND_DELAY = 300

const canVerify = computed(() => {
  if (otpCode.value.length !== 6 || isVerifying.value)
    return false
  return !(hasVerificationFailed.value && resendCountdown.value > 0)
})

const stopCountdown = () => {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

const updateCountdownFromTimestamp = () => {
  const sentAtKey = `otp_sent_at_${email.value}`
  const sentAtRaw = sessionStorage.getItem(sentAtKey)

  if (!sentAtRaw) {
    resendCountdown.value = 0
    return
  }

  const sentAt = Number(sentAtRaw)
  if (!sentAt || Number.isNaN(sentAt)) {
    resendCountdown.value = 0
    return
  }

  const elapsed = Math.floor((Date.now() - sentAt) / 1000)
  const remaining = RESEND_DELAY - elapsed

  resendCountdown.value = Math.max(0, remaining)
}

const startCountdown = () => {
  stopCountdown()

  updateCountdownFromTimestamp()

  if (resendCountdown.value <= 0) return

  countdownInterval = setInterval(() => {
    --resendCountdown.value

    if (resendCountdown.value <= 0) {
      resendCountdown.value = 0
      stopCountdown()
      hasVerificationFailed.value = false
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const handleVerify = async () => {
  if (!canVerify.value) return

  isVerifying.value = true
  otpError.value = ''
  successMessage.value = ''

  try {
    const response = await authApi.activateUser({
      email: email.value,
      verificationCode: otpCode.value
    })

    if (response.data.status === 200) {
      successMessage.value = 'Verification successful! Redirecting to login...'
      setTimeout(() => {
        router.push({ name: 'Login' })
      }, 1500)
    }
  } catch (error) {
    otpError.value = error.response?.data?.message || 'Invalid verification code. Please try again.'
    otpCode.value = ''
    hasVerificationFailed.value = true
  } finally {
    isVerifying.value = false
  }
}

const handleResend = async () => {
  if (resendCountdown.value > 0)
    return

  isResending.value = true
  otpError.value = ''
  successMessage.value = ''
  hasVerificationFailed.value = false

  try {
    await emailApi.sendVerificationCode(email.value)

    sessionStorage.setItem(`otp_sent_${email.value}`, '1')
    sessionStorage.setItem(`otp_sent_at_${email.value}`, String(Date.now()))

    successMessage.value = 'New verification code has been sent!'
    startCountdown()

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (error) {
    otpError.value = error.response?.data?.message || 'Failed to send code. Please try again.'
  } finally {
    isResending.value = false
  }
}

onMounted(async () => {
  if (!email.value) {
    await router.push({ name: 'Register' })
    return
  }

  // chỉ đọc sessionStorage để chạy countdown do trước đó đã gửi
  const sentKey = `otp_sent_${email.value}`
  if (sessionStorage.getItem(sentKey))
    startCountdown()
  else
    resendCountdown.value = 0
})
onUnmounted(() => stopCountdown())
</script>

<style scoped>
.verify-otp-view {
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

.icon-wrapper {
  @apply w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4;
}

.icon-svg {
  @apply w-8 h-8 text-primary-600;
}

.form-title {
  @apply text-3xl font-bold text-gray-900;
}

.form-subtitle {
  @apply text-gray-500 mt-2;
}

.email-highlight {
  @apply font-semibold text-primary-600;
}

.form-body {
  @apply space-y-6;
}

.error-message {
  @apply text-center text-red-500 text-sm;
}

.success-message {
  @apply text-center text-green-500 text-sm;
}

.verify-btn {
  @apply w-full flex items-center justify-center;
}

.spinner {
  @apply mr-2;
}

.resend-section {
  @apply text-center;
}

.resend-label {
  @apply text-gray-500 text-sm mb-2;
}

.resend-btn {
  @apply text-primary-600 font-semibold hover:underline disabled:text-gray-400 disabled:no-underline disabled:cursor-not-allowed;
}
</style>

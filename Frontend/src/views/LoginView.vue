<template>
  <div class="login-view">
    <AppHeader />

    <main class="main-content">
      <div class="form-container">
        <div class="form-card">
          <div class="form-header">
            <h1 class="form-title">Welcome Back</h1>
            <p class="form-subtitle">Sign in to continue 🐾</p>
          </div>

          <form @submit.prevent="handleSubmit" class="form-body">
            <div class="form-group">
              <label class="form-label">Username</label>
              <input
                  v-model="form.username"
                  type="text"
                  placeholder="Enter your username"
                  class="input-field"
                  :class="{ 'input-error': errors.username }"
              />
              <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
            </div>

            <div class="form-group">
              <label class="form-label">Password</label>
              <input
                  v-model="form.password"
                  type="password"
                  placeholder="Enter your password"
                  class="input-field"
                  :class="{ 'input-error': errors.password }"
              />
              <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
            </div>

            <div class="forgot-password">
              <a href="#" class="forgot-link">Forgot password?</a>
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
              {{ isSubmitting ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>

          <div class="form-footer">
            <p class="footer-text">
              Don't have an account?
              <router-link to="/register" class="footer-link">
                Sign Up
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { login } = useAuth()

const form = ref({
  username: '',
  password: ''
})

const errors = ref({
  username: '',
  password: ''
})

const isSubmitting = ref(false)
const apiError = ref('')

const isFormValid = computed(() => form.value.username.trim() && form.value.password)

const handleSubmit = async () => {
  errors.value = { username: '', password: '' }

  if (!form.value.username.trim()) {
    errors.value.username = 'Username is required'
    return
  }
  if (!form.value.password) {
    errors.value.password = 'Password is required'
    return
  }

  isSubmitting.value = true
  apiError.value = ''

  try {
    await login({
      username: form.value.username.trim(),
      password: form.value.password
    })

    const redirect = router.currentRoute.value.query.redirect
    if (redirect)
      await router.push(redirect)
    else
      await router.push({ name: 'NewsFeedHome' })
  } catch (error) {
    apiError.value = error.response?.data?.message || 'Login failed. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.login-view {
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

.forgot-password {
  @apply text-right;
}

.forgot-link {
  @apply text-sm text-primary-600 hover:underline;
}

.api-error {
  @apply bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm;
}

.submit-btn {
  @apply w-full flex items-center justify-center;
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

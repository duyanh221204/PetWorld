<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { useNotifications } from '@/composables/useNotifications'

const auth = useAuth()
const notifications = useNotifications()

onMounted(() => {
  if (auth.isAuthenticated.value)
    notifications.initialize()
})

watch(
  () => auth.isAuthenticated.value,
  (authenticated) => {
    if (authenticated)
      notifications.initialize()
  }
)
</script>

<style scoped>
.app-container {
  @apply min-h-screen flex flex-col bg-white;
}
</style>

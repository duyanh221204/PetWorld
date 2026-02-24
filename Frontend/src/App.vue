<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup>
import { useNotifications } from '@/composables/useNotifications'
import { useChat } from '@/composables/useChat'

useNotifications()
useChat()
</script>

<style scoped>
.app-container {
  @apply min-h-screen flex flex-col bg-white;
}
</style>

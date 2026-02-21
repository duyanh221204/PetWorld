<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup>
import { useNotifications } from '@/composables/useNotifications'

useNotifications()
</script>

<style scoped>
.app-container {
  @apply min-h-screen flex flex-col bg-white;
}
</style>

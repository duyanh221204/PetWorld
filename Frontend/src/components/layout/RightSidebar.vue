<template>
  <aside class="right-sidebar">
    <NotificationSidebar />
  </aside>
</template>

<script setup>
import NotificationSidebar from '@/components/ui/NotificationSidebar.vue'
</script>

<style scoped>
.right-sidebar {
  @apply hidden xl:block fixed right-4 top-20 w-80;
  max-width: 320px;
}
</style>

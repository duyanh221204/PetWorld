<template>
  <div class="not-found-view">
    <main class="main-content">
      <div class="not-found-container">
        <div class="icon-wrapper">
          <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h1 class="title">404 - Page Not Found</h1>
        <p class="description">
          Oops! The page you're looking for doesn't exist.
        </p>
        <p class="suggestion">
          It might have been moved or deleted, or you may have mistyped the URL.
        </p>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from "@/components/layout/AppFooter.vue";
</script>

<style scoped>
.not-found-view {
  /*@apply min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-white px-4;*/
  @apply min-h-screen flex flex-col bg-gradient-to-br from-primary-50 to-white;
}

.main-content {
  @apply flex-1 flex items-center justify-center px-4 py-12;
}

.not-found-container {
  @apply text-center max-w-md;
}

.icon-wrapper {
  @apply flex justify-center mb-6;
}

.icon {
  @apply w-24 h-24 text-primary-400;
}

.title {
  @apply text-4xl font-bold text-gray-900 mb-4;
}

.description {
  @apply text-lg text-gray-600 mb-2;
}

.suggestion {
  @apply text-gray-500 mb-8;
}
</style>

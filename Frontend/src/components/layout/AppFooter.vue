<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
<!--        <div class="logo-section">-->
<!--          <img src="/logo.png" alt="PetWorld Logo" class="logo-image" />-->
<!--          <span class="logo-text">PetWorld</span>-->
<!--        </div>-->

        <div class="copyright-section">
          <p class="copyright-text">© 2025 PetWorld. All rights reserved.</p>
          <p class="tagline">Connecting pet lovers around the world 🐾</p>
        </div>

        <div class="links-section">
          <a href="#" class="footer-link">Privacy Policy</a>
          <a href="#" class="footer-link">Terms of Service</a>
          <a href="#" class="footer-link">Contact</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.app-footer {
  @apply bg-gray-50 border-t border-gray-200 mt-auto;
}

.footer-container {
  @apply max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8;
}

.footer-content {
  @apply flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0;
}

.copyright-section {
  @apply text-gray-500 text-sm text-center;
}

.copyright-text {
  @apply mb-1;
}

.tagline {
  @apply text-xs;
}

.links-section {
  @apply flex space-x-6 text-gray-500 text-sm;
}

.footer-link {
  @apply hover:text-primary-600 transition-colors;
}
</style>

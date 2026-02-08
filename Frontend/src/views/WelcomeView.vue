<template>
  <div class="welcome-view">
    <AppHeader />

    <main class="main-content">
      <section class="hero-section">
        <div class="hero-background"></div>

        <div class="hero-container">
          <div class="hero-grid">
            <div class="hero-text">
              <div class="hero-title-block">
                <h1 class="hero-title">
                  Welcome to
                  <span class="hero-title-highlight">PetWorld!</span>
                </h1>
                <p class="hero-description">
                  A place to connect pet lovers from everywhere.
                  Share adorable moments, learn care tips,
                  and find friends who share your passion.
                </p>
                <p class="hero-tagline">
                  Where pets bring people together 🐶🐱
                </p>

                <div class="hero-pills">
                  <span class="hero-pill">🐾 Friendly community</span>
                  <span class="hero-pill">💜 Made for pet lovers</span>
                  <span class="hero-pill">✨ Share your joy</span>
                </div>
              </div>
            </div>

            <div class="hero-image-wrapper">
              <div class="hero-image-glow"></div>
              <img
                  src="@/assets/images/welcome.png"
                  alt="PetWorld Community"
                  class="hero-image"
              />
            </div>
          </div>
        </div>
      </section>

      <section class="cta-section">
        <div class="cta-container">
          <div class="cta-grid">
            <div class="cta-image-wrapper">
              <img
                  src="@/assets/images/house.png"
                  alt="Pet House"
                  class="cta-image"
              />
            </div>

            <div class="cta-content">
              <h2 class="cta-title">
                Start your journey at PetWorld
              </h2>
              <p class="cta-description">
                Step inside and see what’s new in PetWorld today!
              </p>

              <div class="features-list">
                <div class="feature-item">
                  <div class="feature-icon">
                    <svg class="feature-svg" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  </div>
                  <p class="feature-text">Share photos and stories about your pets</p>
                </div>
                <div class="feature-item">
                  <div class="feature-icon">
                    <svg class="feature-svg" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  </div>
                  <p class="feature-text">Discuss and exchange pet care experiences</p>
                </div>
                <div class="feature-item">
                  <div class="feature-icon">
                    <svg class="feature-svg" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  </div>
                  <p class="feature-text">Connect with the pet-loving community worldwide</p>
                </div>
              </div>

              <router-link to="/login" class="btn-cta-primary">
                <span>Explore Community</span>
                <span class="btn-arrow">→</span>
              </router-link>
            </div>
          </div>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
</script>

<style scoped>
.welcome-view {
  @apply min-h-screen flex flex-col;
}

.main-content {
  @apply flex-1;
}

.hero-section {
  @apply relative bg-gradient-to-br from-primary-50 via-white to-primary-100 overflow-hidden;
}

.hero-background {
  @apply absolute inset-0 opacity-5 pointer-events-none;
  background-image: url('data:image/svg+xml,...');
}

.hero-container {
  @apply max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24;
}

.hero-grid {
  @apply grid lg:grid-cols-2 gap-12 items-center;
}

.hero-text {
  @apply space-y-8;
}

.hero-title-block {
  @apply space-y-5;
}

.hero-title {
  @apply text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 leading-tight;
}

.hero-title-highlight {
  @apply text-primary-600;
}

.hero-description {
  @apply text-xl text-gray-600 leading-relaxed;
}

.hero-image-wrapper {
  @apply relative;
}

.hero-image-glow {
  @apply absolute -inset-4 bg-gradient-to-r from-primary-400 to-primary-600 rounded-3xl opacity-20 blur-2xl pointer-events-none;
}

.hero-image {
  @apply relative rounded-2xl shadow-2xl w-full object-cover;
}

.hero-tagline {
  @apply text-lg sm:text-xl font-semibold text-gray-800;
}

.hero-pills {
  @apply flex flex-wrap gap-3;
}

.hero-pill {
  @apply bg-white/70 text-gray-800 px-4 py-2 rounded-full text-sm font-medium
  shadow-sm border border-primary-100;
}

.cta-section {
  @apply bg-primary-600 py-16;
}

.cta-container {
  @apply max-w-6xl mx-auto px-4 sm:px-6 lg:px-8;
}

.cta-grid {
  @apply grid lg:grid-cols-2 gap-12 items-center;
}

.cta-image-wrapper {
  @apply flex justify-center lg:justify-start;
}

.cta-image {
  @apply w-64 h-64 lg:w-80 lg:h-80 object-contain;
}

.cta-content {
  @apply text-center lg:text-left;
}

.cta-title {
  @apply text-3xl font-bold text-white mb-4;
}

.cta-description {
  @apply text-primary-100 text-lg mb-6;
}

.features-list {
  @apply space-y-3 mb-8;
}

.feature-item {
  @apply flex items-start space-x-3;
}

.feature-icon {
  @apply flex-shrink-0 w-6 h-6 bg-white/20 rounded-full flex items-center justify-center mt-1;
}

.feature-svg {
  @apply w-4 h-4 text-white;
}

.feature-text {
  @apply text-white/90;
}

.btn-cta-primary {
  @apply bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold
  hover:bg-primary-50 transition-colors text-center
  inline-flex items-center justify-center gap-2;
}

.btn-arrow {
  @apply text-xl transition-transform duration-200;
}

.btn-cta-primary:hover .btn-arrow {
  @apply translate-x-1;
}
</style>

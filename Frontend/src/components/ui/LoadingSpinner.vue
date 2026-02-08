<template>
  <div class="flex items-center justify-center">
    <div
        class="animate-spin rounded-full border-4 border-primary-200 border-t-primary-600"
        :class="sizeClasses"
    ></div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'LoadingSpinner',
  props: {
    size: {
      type: String,
      default: 'md',
      validator: (value) => ['sm', 'md', 'lg'].includes(value)
    }
  },
  setup(props) {
    const sizeClasses = computed(() => {
      const sizes = {
        sm: 'h-5 w-5',
        md: 'h-8 w-8',
        lg: 'h-12 w-12'
      }
      return sizes[props.size]
    })

    return { sizeClasses }
  }
}
</script>

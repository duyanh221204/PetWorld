<template>
  <div class="otp-container">
    <input
        v-for="(digit, index) in digits"
        :key="index"
        :ref="el => inputs[index] = el"
        type="text"
        maxlength="1"
        class="otp-input"
        :class="{ 'otp-input-error': hasError }"
        :value="digit"
        @input="handleInput($event, index)"
        @keydown="handleKeydown($event, index)"
        @paste="handlePaste"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  length: {
    type: Number,
    default: 6
  },
  modelValue: {
    type: String,
    default: ''
  },
  hasError: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const digits = ref(Array(props.length).fill(''))
const inputs = ref([])

watch(() => props.modelValue, (newVal) => {
  if (newVal === '')
    digits.value = Array(props.length).fill('')
})

const handleInput = (event, index) => {
  const value = event.target.value.replace(/\D/g, '')
  digits.value[index] = value

  const code = digits.value.join('')
  emit('update:modelValue', code)

  if (value && index < props.length - 1)
    inputs.value[index + 1]?.focus()
}

const handleKeydown = (event, index) => {
  if (event.key === 'Backspace' && !digits.value[index] && index > 0)
    inputs.value[index - 1]?.focus()
}

const handlePaste = (event) => {
  event.preventDefault()
  const pastedData = event.clipboardData.getData('text').replace(/\D/g, '').slice(0, props.length)

  pastedData.split('').forEach((char, index) => {
    if (index < props.length)
      digits.value[index] = char
  })

  const code = digits.value.join('')
  emit('update:modelValue', code)
}
</script>

<style scoped>
.otp-container {
  @apply flex justify-center space-x-3;
}

.otp-input {
  @apply w-14 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-lg
  focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent
  transition-all duration-200;
}

.otp-input-error {
  @apply border-red-500;
}
</style>

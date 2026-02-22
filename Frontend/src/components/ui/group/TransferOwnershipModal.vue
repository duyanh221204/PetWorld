<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click="emit('close')">
      <div class="modal-dialog" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Transfer Ownership</h3>
          <button @click="emit('close')" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="text-gray-700 mb-6">
            You must transfer ownership to another member before leaving the group. 
            Select a member to become the new owner:
          </p>

          <LoadingSpinner v-if="isLoading" size="md" class="my-8" />

          <template v-else>
            <div v-if="members.length === 0" class="empty-state">
              <p>No other members found</p>
            </div>

            <div v-else class="members-grid">
              <div
                v-for="member in members"
                :key="member.id"
                @click="member.role !== 'OWNER' && (selectedMember = member)"
                :class="['member-item', { 
                  selected: selectedMember?.id === member.id,
                  disabled: member.role === 'OWNER'
                }]"
              >
                <div class="member-info">
                  <img
                    v-if="member.userAvatar"
                    :src="member.userAvatar"
                    :alt="member.username"
                    class="avatar-img"
                    @error="(e) => e.target.src = defaultAvatar"
                  />
                  <img
                    v-else
                    :src="defaultAvatar"
                    :alt="member.username"
                    class="avatar-img"
                  />
                  <div class="member-details">
                    <span class="username">{{ member.username }}</span>
                    <span :class="['role-badge', member.role.toLowerCase()]">
                      {{ member.role }}
                    </span>
                  </div>
                </div>
                <div v-if="selectedMember?.id === member.id" class="check-icon">
                  <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                  </svg>
                </div>
              </div>
            </div>

            <div v-if="totalPages > 1" class="pagination">
              <button
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 0"
                class="pagination-btn"
              >
                Previous
              </button>
              <span class="pagination-info">
                Page {{ currentPage + 1 }} of {{ totalPages }}
              </span>
              <button
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage === totalPages - 1"
                class="pagination-btn"
              >
                Next
              </button>
            </div>
          </template>

          <div class="form-actions">
            <button type="button" @click="emit('close')" class="btn-secondary">Cancel</button>
            <button
              type="button"
              @click="handleTransfer"
              :disabled="!selectedMember || isTransferring"
              class="btn-primary"
            >
              {{ isTransferring ? 'Transferring...' : 'Transfer & Leave' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import { groupMembershipApi } from '@/api/groupMembership'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps({
  show: Boolean,
  groupId: Number
})

const emit = defineEmits(['close', 'transferred'])

const isLoading = ref(false)
const isTransferring = ref(false)
const members = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const selectedMember = ref(null)

const loadMembers = async (page = currentPage.value) => {
  if (!props.groupId)
    return
  
  isLoading.value = true
  try {
    const response = await groupMembershipApi.getGroupMembers(props.groupId, page, 100)
    if (response.data.status === 200) {
      const data = response.data.data
      members.value = data.content || []
      currentPage.value = data.number || 0
      totalPages.value = data.totalPages || 0
    }
  } catch (error) {
    console.error('Error loading members:', error)
    alert('Failed to load members. Please try again.')
  } finally {
    isLoading.value = false
  }
}

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    loadMembers(page)
    selectedMember.value = null
  }
}

const handleTransfer = async () => {
  if (!selectedMember.value)
    return
  
  isTransferring.value = true
  try {
    const response = await groupMembershipApi.transferOwnershipAndLeave(
      props.groupId,
      selectedMember.value.userId
    )
    
    if (response.data.status === 200) {
      emit('transferred')
      emit('close')
    } else
      alert(response.data.message || 'Failed to transfer ownership.')
  } catch (error) {
    console.error('Error transferring ownership:', error)
    alert(error.response?.data?.message || 'Failed to transfer ownership. Please try again.')
  } finally {
    isTransferring.value = false
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    currentPage.value = 0
    selectedMember.value = null
    loadMembers(0)
  }
})
</script>

<style scoped>
.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-xl font-bold text-gray-900;
}

.modal-close {
  @apply text-gray-400 hover:text-gray-600 transition-colors;
}

.modal-body {
  @apply flex-1 overflow-y-auto p-6;
}

.empty-state {
  @apply text-center py-8 text-gray-500;
}

.members-grid {
  @apply grid grid-cols-1 md:grid-cols-2 gap-3 mb-6;
}

.member-item {
  @apply flex items-center justify-between p-3 border-2 border-gray-200 rounded-lg cursor-pointer hover:border-primary-300 transition-colors;
}

.member-item.selected {
  @apply border-primary-600 bg-primary-50;
}

.member-item.disabled {
  @apply opacity-50 cursor-not-allowed hover:border-gray-200;
}

.member-info {
  @apply flex items-center gap-3 flex-1;
}

.avatar-img {
  @apply w-10 h-10 rounded-full object-cover flex-shrink-0;
}

.member-details {
  @apply flex items-center gap-2 min-w-0;
}

.username {
  @apply font-medium text-gray-900 truncate;
}

.role-badge {
  @apply px-3 py-1.5 rounded-full text-xs font-semibold uppercase flex-shrink-0;
}

.role-badge.owner {
  @apply bg-purple-100 text-purple-700;
}

.role-badge.admin {
  @apply bg-blue-100 text-blue-700;
}

.role-badge.member {
  @apply bg-gray-200 text-gray-700;
}

.check-icon {
  @apply text-primary-600;
}

.pagination {
  @apply flex items-center justify-center gap-4 mb-6 pb-6 border-b border-gray-200;
}

.pagination-btn {
  @apply px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.pagination-info {
  @apply text-sm text-gray-600;
}

.form-actions {
  @apply flex items-center justify-end gap-3;
}

.btn-secondary {
  @apply px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors;
}

.btn-primary {
  @apply px-6 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-dialog,
.modal-leave-active .modal-dialog {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.modal-enter-from .modal-dialog,
.modal-leave-to .modal-dialog {
  transform: scale(0.95);
  opacity: 0;
}
</style>

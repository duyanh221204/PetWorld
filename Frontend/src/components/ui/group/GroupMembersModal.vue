<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click="emit('close')">
      <div class="modal-dialog" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Group Members ({{ totalMembers }})</h3>
          <div class="header-actions">
            <button @click="loadMembers" class="btn-icon" title="Refresh">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
              </svg>
            </button>
            <button @click="emit('close')" class="modal-close">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="modal-body">
          <LoadingSpinner v-if="isLoading" size="md" class="my-8" />

          <template v-else>
            <div v-if="members.length === 0" class="empty-state">
              <p>No members found</p>
            </div>

            <div v-else class="members-grid">
              <div
                v-for="member in members"
                :key="member.id"
                class="member-item"
              >
                <div class="member-info" @click="goToProfile(member.userId)">
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
                  <span class="username">{{ member.username }}</span>
                </div>

                <div class="member-actions" @click.stop>
                  <div
                    v-if="!canInteractWithMember(member)"
                    :class="['role-badge', member.role.toLowerCase()]"
                  >
                    {{ member.role }}
                  </div>
                  <div
                    v-else
                    class="action-dropdown"
                  >
                    <button 
                      :class="['role-badge', 'clickable', member.role.toLowerCase()]"
                      @click.stop="toggleDropdown(member.id)"
                    >
                      {{ member.role }}
                      <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                      </svg>
                    </button>

                    <Transition name="dropdown">
                      <div v-if="activeDropdown === member.id" class="dropdown-menu" @click.stop>
                        <button
                          v-if="canUpdateRole(member.role)"
                          @click.stop="handleUpdateRole(member)"
                          class="dropdown-item"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                          </svg>
                          Update Role
                        </button>
                        <button
                          v-if="canDeleteMember(member.role)"
                          @click.stop="handleDeleteMember(member)"
                          class="dropdown-item text-red-600"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                          </svg>
                          Remove Member
                        </button>
                      </div>
                    </Transition>
                  </div>
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
        </div>
      </div>
    </div>
  </Transition>

  <Transition name="modal">
    <div v-if="showUpdateRoleModal" class="modal-overlay" @click="closeUpdateRoleModal">
      <div class="modal-dialog-sm" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Update Role</h3>
          <button @click="closeUpdateRoleModal" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="text-gray-700 mb-4">Update role for <strong>{{ editingMember?.username }}</strong></p>
          
          <div class="form-group">
            <label class="form-label">Select Role</label>
            <select v-model="selectedRole" class="form-select">
              <option value="ADMIN">Admin</option>
              <option value="MEMBER">Member</option>
            </select>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeUpdateRoleModal" class="btn-secondary">Cancel</button>
            <button type="button" @click="confirmUpdateRole" class="btn-primary">Update</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <Transition name="modal">
    <div v-if="showDeleteMemberModal" class="modal-overlay" @click="closeDeleteMemberModal">
      <div class="modal-dialog-sm" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Remove Member</h3>
          <button @click="closeDeleteMemberModal" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="text-gray-700 mb-6">
            Are you sure you want to remove <strong>{{ editingMember?.username }}</strong> from this group?
          </p>
          
          <div class="form-actions">
            <button type="button" @click="closeDeleteMemberModal" class="btn-secondary">No</button>
            <button type="button" @click="confirmDeleteMember" class="btn-danger">Yes</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { groupMembershipApi } from '@/api/groupMembership'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps({
  show: Boolean,
  groupId: Number,
  currentUserRole: String
})

const emit = defineEmits(['close', 'member-removed'])

const router = useRouter()
const auth = useAuth()
const currentUserId = computed(() => parseInt(auth.user.value?.id))

const isLoading = ref(false)
const members = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const totalMembers = ref(0)
const activeDropdown = ref(null)

const showUpdateRoleModal = ref(false)
const showDeleteMemberModal = ref(false)
const editingMember = ref(null)
const selectedRole = ref('MEMBER')

const isOwner = computed(() => props.currentUserRole === 'OWNER')
const isAdmin = computed(() => props.currentUserRole === 'ADMIN')

const canInteractWithMember = (member) => {
  if (member.userId === currentUserId.value)
    return false

  if (!isOwner.value && !isAdmin.value)
    return false

  return !(isAdmin.value && member.role !== 'MEMBER')
}

const canUpdateRole = (memberRole) => isOwner.value && memberRole !== 'OWNER'

const canDeleteMember = (memberRole) => {
  if (isOwner.value && memberRole !== 'OWNER')
    return true

  return isAdmin.value && memberRole === 'MEMBER'
}

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
      totalMembers.value = data.totalElements || 0
    }
  } catch (error) {
    console.error('Error loading members:', error)
    alert('Failed to load members. Please try again.')
  } finally {
    isLoading.value = false
  }
}

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value)
    loadMembers(page)
}

const toggleDropdown = (memberId) => activeDropdown.value = activeDropdown.value === memberId ? null : memberId

const goToProfile = (userId) => {
  emit('close')
  router.push({ name: 'Profile', params: { userId } })
}

const handleUpdateRole = (member) => {
  editingMember.value = member
  selectedRole.value = member.role === 'ADMIN' ? 'MEMBER' : 'ADMIN'
  showUpdateRoleModal.value = true
  activeDropdown.value = null
}

const closeUpdateRoleModal = () => {
  showUpdateRoleModal.value = false
  editingMember.value = null
  selectedRole.value = 'MEMBER'
}

const confirmUpdateRole = async () => {
  if (!editingMember.value)
    return
  
  try {
    const response = await groupMembershipApi.updateGroupMembership(
      props.groupId,
      editingMember.value.userId,
      { role: selectedRole.value }
    )
    
    if (response.data.status === 200) {
      closeUpdateRoleModal()
      await loadMembers(currentPage.value)
    } else
      alert(response.data.message || 'Failed to update role.')
  } catch (error) {
    console.error('Error updating role:', error)
    alert(error.response?.data?.message || 'Failed to update role. Please try again.')
  }
}

const handleDeleteMember = (member) => {
  editingMember.value = member
  showDeleteMemberModal.value = true
  activeDropdown.value = null
}

const closeDeleteMemberModal = () => {
  showDeleteMemberModal.value = false
  editingMember.value = null
}

const confirmDeleteMember = async () => {
  if (!editingMember.value)
    return
  
  try {
    const response = await groupMembershipApi.deleteGroupMembership(
      props.groupId,
      editingMember.value.userId
    )
    
    if (response.data.status === 200) {
      closeDeleteMemberModal()
      await loadMembers(currentPage.value)
      emit('member-removed')
    } else
      alert(response.data.message || 'Failed to remove member.')
  } catch (error) {
    console.error('Error removing member:', error)
    alert(error.response?.data?.message || 'Failed to remove member. Please try again.')
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    currentPage.value = 0
    loadMembers(0)
  } else
    activeDropdown.value = null
})

watch(activeDropdown, (newVal) => {
  if (newVal !== null) {
    const closeOnClick = () => {
      activeDropdown.value = null
      document.removeEventListener('click', closeOnClick)
    }
    setTimeout(() => document.addEventListener('click', closeOnClick), 0)
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

.modal-dialog-sm {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[90vh] overflow-hidden flex flex-col;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-xl font-bold text-gray-900;
}

.header-actions {
  @apply flex items-center gap-2;
}

.btn-icon {
  @apply p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-100 rounded-lg transition-colors;
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
  @apply grid grid-cols-1 md:grid-cols-2 gap-4;
}

.member-item {
  @apply flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors;
}

.member-info {
  @apply flex items-center gap-3 flex-1 cursor-pointer;
}

.avatar-img {
  @apply w-10 h-10 rounded-full object-cover;
}

.username {
  @apply font-medium text-gray-900;
}

.member-actions {
  @apply relative;
}

.action-dropdown {
  @apply relative;
}

.role-badge {
  @apply px-3 py-1.5 rounded-full text-xs font-semibold uppercase;
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

.role-badge.clickable {
  @apply cursor-pointer hover:opacity-80 transition-opacity flex items-center;
}

.dropdown-menu {
  @apply absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50;
}

.dropdown-item {
  @apply w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2;
}

.pagination {
  @apply flex items-center justify-center gap-4 mt-6 pt-6 border-t border-gray-200;
}

.pagination-btn {
  @apply px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.pagination-info {
  @apply text-sm text-gray-600;
}

.form-group {
  @apply space-y-2 mb-4;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.form-select {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent;
}

.form-actions {
  @apply flex items-center justify-end gap-3 mt-6;
}

.btn-secondary {
  @apply px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors;
}

.btn-primary {
  @apply px-6 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors;
}

.btn-danger {
  @apply px-6 py-2 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 transition-colors;
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
.modal-enter-active .modal-dialog-sm,
.modal-leave-active .modal-dialog,
.modal-leave-active .modal-dialog-sm {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.modal-enter-from .modal-dialog,
.modal-enter-from .modal-dialog-sm,
.modal-leave-to .modal-dialog,
.modal-leave-to .modal-dialog-sm {
  transform: scale(0.95);
  opacity: 0;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>

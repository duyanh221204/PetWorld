<template>
  <aside class="group-right-sidebar">
    <div class="sidebar-card">
      <div v-if="currentGroup?.description" class="sidebar-section">
        <h3 class="sidebar-title">About</h3>
        <p class="sidebar-text">{{ currentGroup.description }}</p>
      </div>

      <div v-if="isOwnerOrAdmin" class="sidebar-section">
        <button @click="goToJoinRequests" class="join-requests-button">
          <div class="flex items-center justify-between flex-1">
            <span class="font-medium">Join Requests</span>
            <span v-if="joinRequestsCount > 0" class="count-badge">{{ joinRequestsCount }}</span>
          </div>
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>

      <div v-if="currentGroup?.currentUserRole" class="sidebar-section">
        <p class="role-text">
          You are {{ currentGroup.currentUserRole.toLowerCase() }} of the group
        </p>
        <button @click="handleLeaveGroup" class="action-btn leave-btn">
          Leave Group
        </button>
      </div>

      <div v-else class="sidebar-section">
        <button
          v-if="!group?.isRequestedToJoin"
          @click="handleJoinGroup"
          class="action-btn join-btn"
        >
          Join Group
        </button>
        <button
          v-else
          @click="handleCancelRequest"
          class="action-btn cancel-btn"
        >
          Cancel Join Request
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { groupApi } from '@/api/group.js'
import { groupJoinRequestApi } from '@/api/groupJoinRequest.js'

const props = defineProps({
  groupId: {
    type: Number,
    required: true
  },
  group: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['join-group', 'cancel-request', 'leave-group'])

const router = useRouter()
const route = useRoute()

const localGroup = ref(null)
const joinRequestsCount = ref(0)

const currentGroup = computed(() => props.group || localGroup.value)

const isOwnerOrAdmin = computed(() =>
  currentGroup.value?.currentUserRole === 'OWNER' || currentGroup.value?.currentUserRole === 'ADMIN'
)

const loadGroup = async () => {
  if (props.group)
    return
  
  try {
    const response = await groupApi.getGroupById(props.groupId)
    if (response.data.status === 200)
      localGroup.value = response.data.data
  } catch (error) {
    console.error('Error loading group:', error)
  }
}

const loadJoinRequestsCount = async () => {
  if (!isOwnerOrAdmin.value)
    return
  
  try {
    const response = await groupJoinRequestApi.countGroupJoinRequests(props.groupId)
    if (response.data.status === 200)
      joinRequestsCount.value = response.data.data
  } catch (error) {
    console.error('Error loading join requests count:', error)
  }
}

watch(() => props.group, (newGroup) => {
  if (newGroup && isOwnerOrAdmin.value)
    loadJoinRequestsCount()
}, { immediate: true })

watch(() => props.groupId, async (newGroupId) => {
  if (newGroupId && !props.group) {
    await loadGroup()
    if (isOwnerOrAdmin.value)
      await loadJoinRequestsCount()
  }
}, { immediate: true })

const goToJoinRequests = () => router.push({ name: 'GroupJoinRequests', params: { groupId: props.groupId } })

const handleJoinGroup = () => {
  if (route.name === 'GroupDetail')
    emit('join-group')
  else
    router.push({ name: 'GroupDetail', params: { groupId: props.groupId } })
}

const handleCancelRequest = () => {
  if (route.name === 'GroupDetail')
    emit('cancel-request')
  else
    router.push({ name: 'GroupDetail', params: { groupId: props.groupId } })
}

const handleLeaveGroup = () => {
  if (route.name === 'GroupDetail')
    emit('leave-group')
  else
    alert('Leave group feature is not yet available. Please wait for backend implementation.')
}

defineExpose({ loadJoinRequestsCount })
</script>

<style scoped>
.group-right-sidebar {
  @apply hidden xl:block fixed right-4 top-20 w-80;
  max-width: 320px;
}

.sidebar-card {
  @apply bg-white rounded-xl shadow-sm p-6 sticky top-20 space-y-6;
}

.sidebar-section {
  @apply space-y-3;
}

.join-requests-button {
  @apply w-full flex items-center gap-3 px-4 py-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors text-left;
}

.count-badge {
  @apply px-2.5 py-1 bg-red-500 text-white text-xs font-bold rounded-full;
}

.sidebar-title {
  @apply text-lg font-bold text-gray-900;
}

.sidebar-text {
  @apply text-gray-700 leading-relaxed;
}

.role-text {
  @apply text-gray-700;
}

.action-btn {
  @apply w-full px-4 py-2.5 rounded-lg font-medium transition-colors;
}

.join-btn {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}

.cancel-btn {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}

.leave-btn {
  @apply bg-red-600 text-white hover:bg-red-700;
}
</style>

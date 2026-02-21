<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click="closeModal">
      <div class="modal-dialog" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Group Settings</h3>
          <button @click="closeModal" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="tabs">
            <button
              v-for="tab in availableTabs"
              :key="tab.value"
              @click="activeTab = tab.value"
              :class="['tab', { active: activeTab === tab.value }]"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="tab-content">
            <div v-if="activeTab === 'general'" class="tab-panel">
              <form @submit.prevent="handleUpdateGroup">
                <div class="form-group">
                  <label class="form-label">Group Name *</label>
                  <input
                    v-model="formData.name"
                    type="text"
                    placeholder="Enter group name (5-100 characters)"
                    class="form-input"
                    :class="{ 'input-error': formErrors.name }"
                    maxlength="100"
                  />
                  <p v-if="formErrors.name" class="error-text">{{ formErrors.name }}</p>
                </div>

                <div class="form-group">
                  <label class="form-label">Description</label>
                  <textarea
                    v-model="formData.description"
                    placeholder="Enter group description (optional)"
                    rows="4"
                    class="form-textarea"
                  ></textarea>
                </div>

                <div class="form-group">
                  <label class="form-label">Cover Image</label>
                  <div class="image-upload-section">
                    <div v-if="coverImagePreview" class="image-preview">
                      <img :src="coverImagePreview" alt="Cover preview" class="preview-img" />
                      <button @click="removeCoverImage" type="button" class="remove-image-btn">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                      </button>
                    </div>
                    <div v-else class="upload-placeholder">
                      <input
                        ref="coverImageInput"
                        type="file"
                        accept="image/*"
                        @change="handleCoverImageChange"
                        class="hidden-file-input"
                      />
                      <button @click="$refs.coverImageInput.click()" type="button" class="upload-btn">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                        </svg>
                        <span class="upload-text">Click to upload cover image</span>
                      </button>
                    </div>
                    <p v-if="uploadError" class="error-text">{{ uploadError }}</p>
                  </div>
                </div>

                <div v-if="updateError" class="error-message">{{ updateError }}</div>

                <div class="form-actions">
                  <button type="button" @click="closeModal" class="btn-secondary">Cancel</button>
                  <button type="submit" :disabled="isUpdating || isUploading" class="btn-primary">
                    {{ isUploading ? 'Uploading...' : isUpdating ? 'Updating...' : 'Update Group' }}
                  </button>
                </div>
              </form>
            </div>

            <div v-if="activeTab === 'joinForms'" class="tab-panel">
              <div class="join-forms-section">
                <div class="section-header">
                  <h4 class="section-title">Join Forms</h4>
                  <button v-if="isOwner" @click="showCreateFormModal = true" class="btn-create">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                    </svg>
                    Create Form
                  </button>
                </div>

                <LoadingSpinner v-if="isLoadingForms" size="md" class="my-4" />

                <div v-else-if="joinForms.length === 0" class="empty-state">
                  <p>No join forms created yet</p>
                </div>

                <div v-else class="forms-list">
                  <div
                    v-for="form in joinForms"
                    :key="form.id"
                    :class="['form-item', { active: form.isActive }]"
                  >
                    <div class="form-info">
                      <h5 class="form-title">{{ form.title }}</h5>
                      <span :class="['form-status', { active: form.isActive }]">
                        {{ form.isActive ? 'Active' : 'Inactive' }}
                      </span>
                    </div>
                    <div class="form-actions-row">
                      <button @click="viewFormQuestions(form)" class="btn-icon" title="View Questions">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                        </svg>
                      </button>
                      <button
                        v-if="isOwner && !form.isActive"
                        @click="activateForm(form.id)"
                        class="btn-icon"
                        title="Activate"
                      >
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                      </button>
                      <button v-if="isOwner" @click="editForm(form)" class="btn-icon" title="Edit">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                        </svg>
                      </button>
                      <button v-if="isOwner" @click="showDeleteFormModal(form.id)" class="btn-icon text-red-600" title="Delete">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="activeTab === 'danger'" class="tab-panel">
              <div class="danger-zone">
                <h4 class="danger-title">Danger Zone</h4>
                <p class="danger-description">
                  Once you delete a group, there is no going back. Please be certain.
                </p>
                <button @click="confirmDeleteGroup" class="btn-danger">
                  Delete This Group
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <Transition name="modal">
    <div v-if="showCreateFormModal || editingForm" class="modal-overlay" @click="closeFormModal">
      <div class="modal-dialog-sm" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">{{ editingForm ? 'Edit Form' : 'Create Join Form' }}</h3>
          <button @click="closeFormModal" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="handleSaveForm">
            <div class="form-group">
              <label class="form-label">Title *</label>
              <input
                v-model="formFormData.title"
                type="text"
                placeholder="Enter form title"
                class="form-input"
                required
              />
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input v-model="formFormData.isActive" type="checkbox" class="checkbox" />
                <span>Set as active form</span>
              </label>
            </div>

            <div v-if="formError" class="error-message">{{ formError }}</div>

            <div class="form-actions">
              <button type="button" @click="closeFormModal" class="btn-secondary">Cancel</button>
              <button type="submit" :disabled="isSubmittingForm" class="btn-primary">
                {{ isSubmittingForm ? 'Saving...' : editingForm ? 'Update' : 'Create' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>

  <Transition name="modal">
    <div v-if="deletingFormId" class="modal-overlay" @click="closeDeleteFormModal">
      <div class="modal-dialog-sm" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Delete Form</h3>
          <button @click="closeDeleteFormModal" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="text-gray-700 mb-6">Are you sure you want to delete this form? This action cannot be undone.</p>
          
          <div class="form-actions">
            <button type="button" @click="closeDeleteFormModal" class="btn-secondary">Cancel</button>
            <button type="button" @click="deleteForm" class="btn-danger">Delete</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <Transition name="modal">
    <div v-if="showDeleteGroupModal" class="modal-overlay" @click="closeDeleteGroupModal">
      <div class="modal-dialog-sm" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">Delete Group</h3>
          <button @click="closeDeleteGroupModal" class="modal-close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="text-gray-700 mb-6">Are you sure you want to delete this group? This action cannot be undone.</p>
          
          <div class="form-actions">
            <button type="button" @click="closeDeleteGroupModal" class="btn-secondary">No</button>
            <button type="button" @click="handleDeleteGroup" class="btn-danger">Yes</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { groupApi } from '@/api/group.js'
import { groupJoinFormApi } from '@/api/groupJoinForm.js'
import { uploadApi } from '@/api/upload.js'

const props = defineProps({
  show: Boolean,
  group: Object
})

const emit = defineEmits(['close', 'updated', 'deleted'])
const router = useRouter()

const activeTab = ref('general')
const isUpdating = ref(false)
const isUploading = ref(false)
const updateError = ref('')
const uploadError = ref('')
const coverImageInput = ref(null)
const coverImagePreview = ref('')
const coverImageFile = ref(null)

const formData = ref({
  name: '',
  description: '',
  coverImageUrl: ''
})

const formErrors = ref({ name: '' })

const isLoadingForms = ref(false)
const joinForms = ref([])
const showCreateFormModal = ref(false)
const editingForm = ref(null)
const isSubmittingForm = ref(false)
const formError = ref('')

const formFormData = ref({
  title: '',
  isActive: false
})

const deletingFormId = ref(null)
const showDeleteGroupModal = ref(false)

const isOwner = computed(() => props.group?.currentUserRole === 'OWNER')
const isAdmin = computed(() => props.group?.currentUserRole === 'ADMIN')
const isOwnerOrAdmin = computed(() => isOwner.value || isAdmin.value)

const availableTabs = computed(() => {
  const tabs = [{ value: 'general', label: 'General' }]

  if (isOwnerOrAdmin.value)
    tabs.push({ value: 'joinForms', label: 'Join Forms' })

  if (isOwner.value)
    tabs.push({ value: 'danger', label: 'Danger Zone' })
  
  return tabs
})

watch(() => props.show, (newVal) => {
  if (newVal && props.group) {
    formData.value.name = props.group.name || ''
    formData.value.description = props.group.description || ''
    formData.value.coverImageUrl = props.group.coverImageUrl || ''
    coverImagePreview.value = props.group.coverImageUrl || ''
    coverImageFile.value = null
    formErrors.value.name = ''
    updateError.value = ''
    uploadError.value = ''
    activeTab.value = 'general'

    if (isOwnerOrAdmin.value)
      loadJoinForms()
  }
})

const handleCoverImageChange = (event) => {
  const file = event.target.files[0]
  if (!file)
    return

  uploadError.value = ''

  if (!file.type.startsWith('image/')) {
    uploadError.value = 'Please select an image file'
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    uploadError.value = 'Image size must be less than 5MB'
    return
  }

  coverImageFile.value = file

  const reader = new FileReader()
  reader.onload = (e) => coverImagePreview.value = e.target.result
  reader.readAsDataURL(file)
}

const removeCoverImage = () => {
  coverImageFile.value = null
  coverImagePreview.value = ''
  uploadError.value = ''
  if (coverImageInput.value)
    coverImageInput.value.value = ''
}

const handleUpdateGroup = async () => {
  formErrors.value.name = ''
  updateError.value = ''
  uploadError.value = ''

  const name = formData.value.name.trim()
  if (name.length < 5 || name.length > 100) {
    formErrors.value.name = 'Group name must be between 5 and 100 characters'
    return
  }

  isUpdating.value = true

  try {
    let coverImageUrl = formData.value.coverImageUrl

    if (coverImageFile.value) {
      isUploading.value = true
      try {
        const uploadResponse = await uploadApi.uploadFile(coverImageFile.value)
        if (uploadResponse.data.status === 200)
          coverImageUrl = uploadResponse.data.data
        else
          throw new Error('Failed to upload image')
      } catch (uploadErr) {
        console.error('Error uploading image:', uploadErr)
        uploadError.value = 'Failed to upload cover image. Please try again.'
        return
      } finally {
        isUploading.value = false
      }
    }

    const payload = {
      name: formData.value.name.trim(),
      description: formData.value.description.trim() || null,
      coverImageUrl: coverImageUrl
    }

    const response = await groupApi.updateGroup(props.group.id, payload)

    if (response.data.status === 200) {
      const updatedGroup = response.data.data
      emit('updated', updatedGroup)
      if (props.group)
        Object.assign(props.group, updatedGroup)
      closeModal()
    } else
      updateError.value = response.data.message || 'Failed to update group'
  } catch (error) {
    console.error('Error updating group:', error)
    updateError.value = error.response?.data?.message || 'Failed to update group. Please try again.'
  } finally {
    isUpdating.value = false
  }
}

const confirmDeleteGroup = () => showDeleteGroupModal.value = true

const closeDeleteGroupModal = () => showDeleteGroupModal.value = false

const handleDeleteGroup = async () => {
  try {
    const response = await groupApi.deleteGroup(props.group.id)
    if (response.data.status === 200) {
      emit('deleted')
      closeModal()
      closeDeleteGroupModal()
      await router.push({name: 'Groups'})
    } else
      alert(response.data.message || 'Failed to delete group.')
  } catch (error) {
    console.error('Error deleting group:', error)
    alert(error.response?.data?.message || 'Failed to delete group. Please try again.')
  }
}

const loadJoinForms = async () => {
  if (!props.group?.id)
    return

  isLoadingForms.value = true
  try {
    const response = await groupJoinFormApi.getGroupJoinForms(props.group.id)
    if (response.data.status === 200)
      joinForms.value = response.data.data
  } catch (error) {
    console.error('Error loading join forms:', error)
  } finally {
    isLoadingForms.value = false
  }
}

const editForm = (form) => {
  editingForm.value = form
  formFormData.value.title = form.title
  formFormData.value.isActive = form.isActive
}

const closeFormModal = () => {
  showCreateFormModal.value = false
  editingForm.value = null
  formFormData.value = { title: '', isActive: false }
  formError.value = ''
}

const handleSaveForm = async () => {
  formError.value = ''
  isSubmittingForm.value = true

  try {
    let response
    if (editingForm.value)
      response = await groupJoinFormApi.updateGroupJoinForm(
        props.group.id,
        editingForm.value.id,
        formFormData.value
      )
    else
      response = await groupJoinFormApi.createGroupJoinForm(
        props.group.id,
        formFormData.value
      )

    if (response.data.status === 200) {
      await loadJoinForms()
      closeFormModal()
    } else
      formError.value = response.data.message || 'Failed to save form'
  } catch (error) {
    console.error('Error saving form:', error)
    formError.value = error.response?.data?.message || 'Failed to save form. Please try again.'
  } finally {
    isSubmittingForm.value = false
  }
}

const activateForm = async (formId) => {
  try {
    const response = await groupJoinFormApi.activateGroupJoinForm(props.group.id, formId)
    if (response.data.status === 200)
      await loadJoinForms()
  } catch (error) {
    console.error('Error activating form:', error)
    alert('Failed to activate form. Please try again.')
  }
}

const showDeleteFormModal = (formId) => deletingFormId.value = formId

const closeDeleteFormModal = () => deletingFormId.value = null

const deleteForm = async () => {
  if (!deletingFormId.value)
    return
  
  try {
    const response = await groupJoinFormApi.deleteGroupJoinForm(props.group.id, deletingFormId.value)
    if (response.data.status === 200) {
      await loadJoinForms()
      closeDeleteFormModal()
    } else
      alert(response.data.message || 'Failed to delete form.')
  } catch (error) {
    console.error('Error deleting form:', error)
    alert(error.response?.data?.message || 'Failed to delete form. Please try again.')
  }
}

const viewFormQuestions = (form) => {
  if (!props.group?.id) {
    alert('Invalid group. Please try again.')
    return
  }
  
  if (!form?.id) {
    alert('Invalid form. Please try again.')
    return
  }

  emit('close')

  router.push({
    name: 'GroupJoinFormQuestions',
    params: { 
      groupId: String(props.group.id), 
      formId: String(form.id) 
    }
  })
}

const closeModal = () => emit('close')
</script>

<style scoped>
.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.modal-dialog {
  @apply bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col;
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

.modal-close {
  @apply text-gray-400 hover:text-gray-600 transition-colors;
}

.modal-body {
  @apply flex-1 overflow-y-auto p-6;
}

.tabs {
  @apply flex border-b border-gray-200 mb-4;
}

.tab {
  @apply px-4 py-2 text-gray-600 font-medium transition-colors border-b-2 border-transparent hover:text-primary-600 hover:border-primary-200;
}

.tab.active {
  @apply text-primary-600 border-primary-600;
}

.tab-content {
  @apply mt-0;
}

.tab-panel {
  @apply space-y-4;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.form-input,
.form-textarea {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-shadow;
}

.form-input.input-error {
  @apply border-red-500 focus:ring-red-500;
}

.form-textarea {
  @apply resize-none;
}

.error-text {
  @apply text-xs text-red-600 mt-1;
}

.error-message {
  @apply bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm;
}

.form-actions {
  @apply flex items-center justify-end gap-3 mt-6;
}

.btn-secondary {
  @apply px-6 py-2 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors;
}

.btn-primary {
  @apply px-6 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors;
}

.image-upload-section {
  @apply space-y-2;
}

.image-preview {
  @apply relative rounded-lg overflow-hidden border-2 border-gray-300;
}

.preview-img {
  @apply w-full h-48 object-cover;
}

.remove-image-btn {
  @apply absolute top-2 right-2 bg-red-500 hover:bg-red-600 text-white p-2 rounded-full transition-colors shadow-lg;
}

.upload-placeholder {
  @apply border-2 border-dashed border-gray-300 rounded-lg p-8 hover:border-primary-400 transition-colors;
}

.hidden-file-input {
  @apply hidden;
}

.upload-btn {
  @apply w-full flex flex-col items-center justify-center gap-2 text-gray-600 hover:text-primary-600 transition-colors;
}

.upload-text {
  @apply text-sm font-medium text-center;
}

.join-forms-section {
  @apply space-y-4;
}

.section-header {
  @apply flex items-center justify-between mb-4;
}

.section-title {
  @apply text-lg font-bold text-gray-900;
}

.btn-create {
  @apply flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium;
}

.empty-state {
  @apply text-center py-8 text-gray-500;
}

.forms-list {
  @apply space-y-3;
}

.form-item {
  @apply flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-primary-300 transition-colors;
}

.form-item.active {
  @apply border-primary-500 bg-primary-50;
}

.form-info {
  @apply flex items-center gap-3;
}

.form-title {
  @apply font-semibold text-gray-900;
}

.form-status {
  @apply px-2.5 py-1 rounded-full text-xs font-semibold uppercase bg-gray-100 text-gray-600;
}

.form-status.active {
  @apply bg-green-100 text-green-700;
}

.form-actions-row {
  @apply flex items-center gap-2;
}

.btn-icon {
  @apply p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-100 rounded-lg transition-colors;
}

.checkbox-label {
  @apply flex items-center gap-2 cursor-pointer;
}

.checkbox {
  @apply w-4 h-4 text-primary-600 rounded focus:ring-2 focus:ring-primary-500;
}

.danger-zone {
  @apply p-6 border-2 border-red-200 rounded-lg bg-red-50;
}

.danger-title {
  @apply text-lg font-bold text-red-900 mb-2;
}

.danger-description {
  @apply text-sm text-red-700 mb-4;
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
</style>

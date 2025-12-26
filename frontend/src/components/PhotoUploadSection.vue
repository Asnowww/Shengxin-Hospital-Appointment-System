<template>
  <div class="form-group">
    <label class="form-label">
      证件照片
      <span class="required">*</span>
    </label>

    <!-- 预览 -->
    <div v-if="preview" class="preview-container">
      <div class="preview-wrapper">
        <img :src="preview" alt="证件照片预览" class="preview-img" />
        <div class="preview-overlay">
          <svg xmlns="https://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>照片已上传</span>
        </div>
      </div>
      <button type="button" @click="removePhoto" :disabled="loading" class="remove-btn">
        移除照片
      </button>
    </div>

    <!-- 上传区域 -->
    <div v-else class="upload-area" :class="{ loading }">
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        @change="handleFileChange"
        class="file-input"
        :disabled="loading"
      />
      <input
        ref="cameraInputRef"
        type="file"
        accept="image/*"
        capture="environment"
        @change="handleCameraCapture"
        class="file-input"
        :disabled="loading"
      />

      <div class="upload-content">
        <div class="upload-icon">
          <!-- 图标 -->
        </div>
        <div class="upload-text">
          <h3>点击上传证件照片</h3>
          <p>支持 JPG、PNG 格式，最大 5MB</p>
        </div>
        <div class="upload-buttons">
          <button type="button" @click="triggerCamera" :disabled="loading" class="upload-action-btn camera-btn">
            现场拍照
          </button>
          <button type="button" @click="triggerFileUpload" :disabled="loading" class="upload-action-btn upload-btn">
            选择照片
          </button>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">{{ error }}</div>
    <div v-else-if="!preview" class="hint-text">
      请上传清晰的学生证或工牌照片，包含完整的证件信息
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  file: File,
  preview: String,
  error: String,
  loading: Boolean
})
const emit = defineEmits(['update:file', 'update:preview'])

const fileInputRef = ref(null)
const cameraInputRef = ref(null)

// 上传或拍照后处理图片
const handlePhoto = (file) => {
  if (!file) return
  if (!file.type.startsWith('image/')) return
  if (file.size > 5 * 1024 * 1024) return

  const reader = new FileReader()
  reader.onload = (e) => {
    emit('update:file', file)
    emit('update:preview', e.target.result)
  }
  reader.readAsDataURL(file)
}

const handleFileChange = (e) => handlePhoto(e.target.files?.[0])
const handleCameraCapture = (e) => handlePhoto(e.target.files?.[0])

const triggerFileUpload = () => fileInputRef.value?.click()
const triggerCamera = () => cameraInputRef.value?.click()

const removePhoto = () => {
  emit('update:file', null)
  emit('update:preview', null)
  if (fileInputRef.value) fileInputRef.value.value = ''
  if (cameraInputRef.value) cameraInputRef.value.value = ''
}
</script>


<style scoped>
.form-group {
  width: 100%;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #4a5568;
  font-weight: 600;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.form-label svg {
  color: #667eea;
  flex-shrink: 0;
}

.required {
  color: #e53e3e;
}

/* 上传区域 */
.upload-area {
  position: relative;
  border: 2px dashed #cbd5e0;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.02) 0%, rgba(118, 75, 162, 0.02) 100%);
}

.upload-area:hover:not(.loading) {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
}

.upload-area.loading {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.upload-content {
  pointer-events: none;
}

.upload-icon {
  margin-bottom: 1rem;
}

.upload-icon svg {
  color: #667eea;
  display: block;
  margin: 0 auto;
}

.upload-text h3 {
  margin: 0 0 0.25rem 0;
  color: #2d3748;
  font-size: 1rem;
  font-weight: 600;
}

.upload-text p {
  margin: 0 0 1rem 0;
  color: #718096;
  font-size: 0.85rem;
}

.upload-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  pointer-events: auto;
}

.upload-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.upload-action-btn:hover:not(:disabled) {
  border-color: #667eea;
  background: #f7fafc;
}

.upload-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.upload-action-btn svg {
  width: 16px;
  height: 16px;
}

.camera-btn {
  color: #667eea;
}

.upload-btn {
  color: #667eea;
}

/* 预览容器 */
.preview-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preview-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #f7fafc;
  aspect-ratio: 4/3;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.preview-wrapper:hover .preview-overlay {
  opacity: 1;
}

.preview-overlay svg {
  color: white;
  width: 32px;
  height: 32px;
}

.preview-overlay span {
  color: white;
  font-size: 0.9rem;
  font-weight: 500;
}

.remove-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem 1rem;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.remove-btn:hover:not(:disabled) {
  background: #fca5a5;
  color: #991b1b;
}

.remove-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.remove-btn svg {
  width: 16px;
  height: 16px;
}

/* 错误提示 */
.error-message {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #e53e3e;
  font-size: 0.85rem;
  margin-top: 0.75rem;
  padding: 0.5rem 0.75rem;
  background: #fee2e2;
  border-radius: 6px;
}

.error-message svg {
  flex-shrink: 0;
  color: #dc2626;
}

/* 提示文本 */
.hint-text {
  display: flex;
  align-items: flex-start;
  gap: 0.375rem;
  color: #718096;
  font-size: 0.8rem;
  margin-top: 0.75rem;
}

.hint-text svg {
  flex-shrink: 0;
  color: #a0aec0;
  margin-top: 0.125rem;
}

@media (max-width: 768px) {
  .upload-area {
    padding: 1.5rem;
  }

  .upload-text h3 {
    font-size: 0.95rem;
  }

  .upload-buttons {
    grid-template-columns: 1fr;
  }
}
</style>
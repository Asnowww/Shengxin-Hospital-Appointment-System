<template>
  <div class="searchable-select">
    <div class="select-trigger" @click="toggleDropdown">
      <input
        v-model="searchText"
        type="text"
        class="select-input"
        :placeholder="placeholder"
        @input="handleSearch"
        @click.stop="openDropdown"
        @keydown="handleKeydown"
        @focus="openDropdown"
      />
      <svg
        :class="['dropdown-icon', { open: isOpen }]"
        xmlns="http://www.w3.org/2000/svg"
        width="20"
        height="20"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>

    <div v-if="isOpen" class="dropdown-overlay" @click="closeDropdown"></div>

    <div v-if="isOpen" class="dropdown-menu">
      <div class="dropdown-options">
        <div
          v-if="filteredOptions.length === 0"
          class="no-options"
        >
          暂无匹配的医生
        </div>

        <div
          v-for="(option, index) in filteredOptions"
          :key="option.doctorId"
          :class="['option-item', { highlighted: highlightedIndex === index }]"
          @click="selectOption(option)"
          @mouseover="highlightedIndex = index"
        >
          <div class="option-name">{{ option.doctorName }}</div>
          <div class="option-title">{{ option.title }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '搜索医生姓名'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const searchText = ref('')
const highlightedIndex = ref(0)

const filteredOptions = computed(() => {
  if (!searchText.value.trim()) return props.options
  const keyword = searchText.value.trim()
  return props.options.filter(d =>
    d.doctorName.includes(keyword) || d.title.includes(keyword)
  )
})

function toggleDropdown() {
  isOpen.value ? closeDropdown() : openDropdown()
}

function openDropdown() {
  isOpen.value = true
  highlightedIndex.value = 0
}

function closeDropdown() {
  isOpen.value = false
  highlightedIndex.value = 0
}

function handleSearch(event) {
  searchText.value = event.target.value
  highlightedIndex.value = 0
}

function selectOption(option) {
  searchText.value = `${option.doctorName} - ${option.title}`
  emit('update:modelValue', option.doctorId)
  emit('change', option.doctorId)
  closeDropdown()
}

function handleKeydown(event) {
  if (!isOpen.value) {
    if (event.key === 'Enter' || event.key === 'ArrowDown') {
      openDropdown()
      event.preventDefault()
    }
    return
  }

  switch (event.key) {
    case 'ArrowDown':
      highlightedIndex.value = Math.min(highlightedIndex.value + 1, filteredOptions.value.length - 1)
      event.preventDefault()
      break
    case 'ArrowUp':
      highlightedIndex.value = Math.max(highlightedIndex.value - 1, 0)
      event.preventDefault()
      break
    case 'Enter':
      if (filteredOptions.value.length > 0) {
        selectOption(filteredOptions.value[highlightedIndex.value])
      }
      event.preventDefault()
      break
    case 'Escape':
      closeDropdown()
      event.preventDefault()
      break
  }
}
</script>

<style scoped>
.searchable-select {
  position: relative;
  width: 100%;
}

.select-trigger {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.select-input {
  width: 100%;
  padding: 0.625rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.select-input:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.dropdown-icon {
  position: absolute;
  right: 0.625rem;
  color: #718096;
  pointer-events: none;
  transition: transform 0.3s ease;
}

.dropdown-icon.open {
  transform: rotate(180deg);
}

.dropdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 2px solid #e2e8f0;
  border-top: none;
  border-radius: 0 0 8px 8px;
  margin-top: -2px;
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dropdown-options {
  display: flex;
  flex-direction: column;
}

.no-options {
  padding: 1rem;
  text-align: center;
  color: #a0aec0;
  font-size: 0.9rem;
}

.option-item {
  padding: 0.75rem 0.625rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid #f0f0f0;
}

.option-item:last-child {
  border-bottom: none;
}

.option-item:hover,
.option-item.highlighted {
  background-color: #f7fafc;
}

.option-item.highlighted {
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(245, 87, 108, 0.1) 100%);
}

.option-name {
  font-weight: 600;
  color: #2d3748;
  font-size: 0.95rem;
}

.option-title {
  font-size: 0.8rem;
  color: #718096;
  margin-top: 0.25rem;
}

/* 滚动条样式 */
.dropdown-menu::-webkit-scrollbar {
  width: 6px;
}

.dropdown-menu::-webkit-scrollbar-track {
  background: transparent;
}

.dropdown-menu::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

.dropdown-menu::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}
</style>
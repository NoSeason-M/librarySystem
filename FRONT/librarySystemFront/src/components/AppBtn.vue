<script setup lang="ts">
defineOptions({ name: 'AppBtn' })

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  disabled?: boolean
  loading?: boolean
  icon?: string
  type?: 'button' | 'submit'
}>(), {
  variant: 'primary',
  disabled: false,
  loading: false,
  type: 'button',
})

const emit = defineEmits<{
  click: [e: MouseEvent]
}>()

function handleClick(e: MouseEvent) {
  if (!props.disabled && !props.loading) {
    emit('click', e)
  }
}
</script>

<template>
  <button
    :class="['app-btn', `app-btn--${variant}`, { 'app-btn--loading': loading }]"
    :disabled="disabled || loading"
    :type="type"
    @click="handleClick"
  >
    <span v-if="loading" class="app-btn__spinner" />
    <span v-if="icon && !loading" class="app-btn__icon">{{ icon }}</span>
    <span v-if="$slots.default" class="app-btn__text">
      <slot />
    </span>
  </button>
</template>

<style scoped>
.app-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: var(--button-radius, 10px);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s, background 0.2s, box-shadow 0.2s;
  white-space: nowrap;
  line-height: 1;
}
.app-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.app-btn:hover:not(:disabled) {
  opacity: 0.85;
}

/* Primary */
.app-btn--primary {
  background: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFFFFF);
  font-weight: 600;
}

/* Secondary */
.app-btn--secondary {
  background: var(--bg-primary, #FFFFFF);
  color: var(--text-secondary, #666666);
  font-weight: 500;
  border: 1.5px solid var(--border, #E5E7EB);
}

/* Danger */
.app-btn--danger {
  background: var(--danger, #F87171);
  color: var(--text-inverse, #FFFFFF);
  font-weight: 600;
}

/* Ghost */
.app-btn--ghost {
  background: transparent;
  color: var(--accent, #4A9FD8);
  font-weight: 500;
}
.app-btn--ghost:hover:not(:disabled) {
  background: var(--accent-light, #E8F4FD);
}

/* Icon */
.app-btn__icon {
  font-size: 16px;
  line-height: 1;
}

/* Spinner */
.app-btn__spinner {
  width: 18px;
  height: 18px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: app-btn-spin 0.6s linear infinite;
}
@keyframes app-btn-spin {
  to { transform: rotate(360deg); }
}
</style>

<script setup lang="ts">
defineOptions({ name: 'AppBadge' })

withDefaults(defineProps<{
  variant?: 'success' | 'warning' | 'danger' | 'accent' | 'default'
}>(), {
  variant: 'default',
})
</script>

<template>
  <span :class="['app-badge', `app-badge--${variant}`]">
    <slot />
  </span>
</template>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 500;
  color: var(--text-inverse, #FFFFFF);
  white-space: nowrap;
  line-height: 1.3;
}

.app-badge--success { background: var(--success, #34D399); }
.app-badge--warning { background: var(--warning, #FBBF24); }
.app-badge--danger  { background: var(--danger, #F87171); }
.app-badge--accent  { background: var(--accent, #4A9FD8); }
.app-badge--default { background: var(--text-muted, #888888); }
</style>

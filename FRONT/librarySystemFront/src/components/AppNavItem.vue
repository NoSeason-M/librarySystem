<script setup lang="ts">
defineOptions({ name: 'AppNavItem' })

withDefaults(defineProps<{
  icon?: string
  activeIcon?: string
  active?: boolean
  label: string
}>(), {
  icon: '○',
  activeIcon: '●',
  active: false,
})

const emit = defineEmits<{
  click: [e: MouseEvent]
}>()
</script>

<template>
  <div
    :class="['app-nav-item', { 'app-nav-item--active': active }]"
    @click="emit('click', $event)"
  >
    <span class="app-nav-item__icon">{{ active ? activeIcon : icon }}</span>
    <span class="app-nav-item__label">{{ label }}</span>
  </div>
</template>

<style scoped>
.app-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  user-select: none;
  width: 200px;
}

.app-nav-item:hover:not(.app-nav-item--active) {
  background: color-mix(in srgb, var(--accent, #4A9FD8) 8%, transparent);
}

.app-nav-item__icon {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 18px;
  color: var(--text-secondary, #666666);
  width: 20px;
  text-align: center;
  line-height: 1;
}

.app-nav-item__label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-secondary, #666666);
  font-weight: 400;
}

/* Active state */
.app-nav-item--active {
  background: var(--accent, #4A9FD8);
}
.app-nav-item--active .app-nav-item__icon {
  color: var(--text-inverse, #FFFFFF);
}
.app-nav-item--active .app-nav-item__label {
  color: var(--text-inverse, #FFFFFF);
  font-weight: 600;
}
</style>

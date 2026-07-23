<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const realName = ref(localStorage.getItem('realName') || 'Admin')
const userInitials = ref(realName.value.charAt(0).toUpperCase())

const adminNav = [
  { icon: '📊', label: 'Dashboard', route: '/admin' },
  { icon: '📖', label: 'Borrow/Return', route: '/admin/borrow-return' },
  { icon: '📚', label: 'Books', route: '/admin/books' },
  { icon: '👥', label: 'Readers', route: '/admin/readers' },
  { icon: '📈', label: 'Statistics', route: '/admin/statistics' },
  { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings', route: '/admin/settings' },
]

function navigateTo(item: { label: string; route?: string }) {
  if (item.route) router.push(item.route)
}

function isActive(item: { label: string; route?: string }): boolean {
  return item.route ? route.path.startsWith(item.route) : false
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar__logo"><span>📚</span><span class="sidebar__logo-text">LibraryOS</span></div>
      <nav class="sidebar__nav">
        <div
          v-for="item in adminNav"
          :key="item.label"
          :class="['sidebar__item', { 'sidebar__item--active': isActive(item) }]"
          @click="navigateTo(item)"
        >
          <span class="sidebar__item-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </div>
      </nav>
      <div class="sidebar__bottom">
        <div class="sidebar__avatar">{{ userInitials }}</div>
        <span>{{ realName }}</span>
      </div>
    </aside>
    <router-view class="admin-content" />
  </div>
</template>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; background: var(--bg-secondary, #F7F8FA); }
.admin-content { flex: 1; display: flex; min-width: 0; }
.sidebar { width: 240px; background: var(--bg-inverse, #0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; position: sticky; top: 0; height: 100vh; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse, #FFF); }
.sidebar__logo-text { font-family: var(--font-sans, Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-muted, #888); transition: background 0.15s; }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item--active { background: rgba(74,159,216,0.12); color: var(--accent, #4A9FD8); font-weight: 600; }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-inverse, #FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light, #E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent, #4A9FD8); flex-shrink: 0; }
</style>

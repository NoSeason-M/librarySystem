<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getMyProfile } from '../api/readers'
import { getNotifications, markNotifRead, markAllNotifRead } from '../api/notifications'
import type { NotifItem } from '../api/notifications'

const router = useRouter()
const route = useRoute()
const realName = ref(localStorage.getItem('realName') || '读者')
const userInitials = ref(realName.value.charAt(0).toUpperCase())
const readerNo = ref(localStorage.getItem('readerNo') || '')
const readerType = ref(localStorage.getItem('readerType') || '读者')

// Notifications
const notifDropdownOpen = ref(false)
const unreadCount = ref(0)
const recentNotifs = ref<NotifItem[]>([])
const notifLoading = ref(false)

const sidebarLinks = [
  { icon: '📖', label: '当前借阅', route: '/reader' },
  { icon: '⏱', label: '借阅历史', route: '/reader/history' },
  { icon: '📌', label: '我的预约', route: '/reader/reservations' },
  { icon: '⭐', label: '我的收藏', route: '/reader/favorites' },
  { icon: '💰', label: '我的罚款', route: '/reader/fines' },
  { icon: '⚙️', label: '设置', route: '/reader/settings' },
]

onMounted(async () => {
  if (!localStorage.getItem('readerNo')) {
    try {
      const profile = await getMyProfile()
      localStorage.setItem('readerNo', profile.readerNo || '')
      localStorage.setItem('readerType', profile.readerTypeName || '读者')
      localStorage.setItem('realName', profile.realName || realName.value)
      readerNo.value = profile.readerNo || ''
      readerType.value = profile.readerTypeName || '读者'
      realName.value = profile.realName || realName.value
      userInitials.value = (realName.value || '?').charAt(0)
    } catch { /* use defaults */ }
  } else {
    readerNo.value = localStorage.getItem('readerNo') || ''
    readerType.value = localStorage.getItem('readerType') || '读者'
  }
  loadUnreadCount()
})

function isActive(item: { route: string }): boolean {
  if (item.route === '/reader') return route.path === '/reader'
  return route.path.startsWith(item.route)
}

function getInitials(name: string): string {
  return (name || '?').charAt(0)
}

// ===== Notification Bell =====
async function loadUnreadCount() {
  try {
    const { getUnreadCount } = await import('../api/notifications')
    unreadCount.value = await getUnreadCount()
  } catch { unreadCount.value = 0 }
}

async function toggleNotifDropdown() {
  notifDropdownOpen.value = !notifDropdownOpen.value
  if (notifDropdownOpen.value && recentNotifs.value.length === 0) {
    notifLoading.value = true
    try {
      const result = await getNotifications(1, 5)
      recentNotifs.value = result.records || []
    } catch { recentNotifs.value = [] }
    finally { notifLoading.value = false }
  }
}

async function handleMarkRead(id: number) {
  try {
    await markNotifRead(id)
    const n = recentNotifs.value.find(n => n.id === id)
    if (n) n.readFlag = true
    loadUnreadCount()
  } catch { /* ignore */ }
}

async function handleMarkAllRead() {
  try {
    await markAllNotifRead()
    recentNotifs.value.forEach(n => n.readFlag = true)
    unreadCount.value = 0
  } catch { /* ignore */ }
}

function goToNotifPage() {
  notifDropdownOpen.value = false
  router.push('/reader/notifications')
}

function notifIcon(type: string): string {
  const icons: Record<string, string> = {
    overdue_due: '🔴', due_soon: '🟡', arrival: '🟢',
    cancel: '⚪', fine: '💰', system: '🔵',
  }
  return icons[type] || '🔔'
}

// Click outside to close dropdown
function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.notif-bell-area')) {
    notifDropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="reader-layout">
    <!-- Nav Bar -->
    <nav class="nav">
      <span class="nav__logo" @click="router.push('/home')">📚 LibraryOS</span>
      <div class="nav__links">
        <span class="nav__link" @click="router.push('/home')">首页</span>
        <span class="nav__link" @click="router.push('/books')">浏览</span>
        <span class="nav__link nav__link--active">我的</span>
        <span class="nav__link" @click="router.push('/reader')">个人中心</span>
      </div>
      <div class="nav__user">
        <!-- Bell Icon -->
        <div class="notif-bell-area" @click.stop>
          <div class="bell-wrap" @click="toggleNotifDropdown">
            <span class="bell-icon">🔔</span>
            <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>

          <!-- Dropdown Panel -->
          <transition name="fade">
            <div v-if="notifDropdownOpen" class="notif-dropdown">
              <div class="notif-dropdown__header">
                <span class="notif-dropdown__title">🔔 通知</span>
                <span v-if="unreadCount > 0" class="notif-dropdown__markall" @click="handleMarkAllRead">全部已读</span>
              </div>
              <div class="notif-dropdown__divider"></div>

              <div v-if="notifLoading" class="notif-dropdown__empty">加载中...</div>
              <div v-else-if="recentNotifs.length === 0" class="notif-dropdown__empty">暂无通知</div>

              <div v-for="n in recentNotifs" :key="n.id" class="notif-dropdown__item" @click="handleMarkRead(n.id)">
                <span class="notif-item-icon">{{ notifIcon(n.type) }}</span>
                <div class="notif-item-content">
                  <div class="notif-item-top">
                    <span v-if="!n.readFlag" class="notif-item-dot"></span>
                    <span class="notif-item-title">{{ n.title }}</span>
                    <span class="notif-item-time">{{ n.createTime?.slice(5, 16) }}</span>
                  </div>
                  <p class="notif-item-desc">{{ n.content }}</p>
                </div>
              </div>

              <div class="notif-dropdown__divider"></div>
              <div class="notif-dropdown__footer" @click="goToNotifPage">查看全部通知 →</div>
            </div>
          </transition>
        </div>

        <span class="nav__username">{{ realName }}</span>
        <div class="nav__avatar" @click="router.push('/reader')">{{ getInitials(realName) }}</div>
      </div>
    </nav>

    <!-- Main -->
    <main class="main">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="profile-card" @click="router.push('/reader')">
          <div class="profile-avatar">{{ getInitials(realName) }}</div>
          <h2 class="profile-name">{{ realName }}</h2>
          <p class="profile-no">{{ readerNo || '—' }}</p>
          <div class="profile-badge">{{ readerType }}</div>
        </div>

        <div class="quick-links">
          <div v-for="item in sidebarLinks" :key="item.label"
            :class="['quick-link', { 'quick-link--active': isActive(item) }]"
            @click="router.push(item.route)">
            <span class="quick-link__icon">{{ item.icon }}</span>
            <span class="quick-link__label">{{ item.label }}</span>
          </div>
        </div>
      </aside>

      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.reader-layout { width: 100%; min-height: 100vh; display: flex; flex-direction: column; background: var(--bg-secondary,#F7F8FA); }

.nav { display: flex; align-items: center; gap: 32px; padding: 16px 40px; background: var(--bg-primary,#FFF); height: 68px; flex-shrink: 0; }
.nav__logo { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 700; color: var(--text-primary,#1A1A1A); cursor: pointer; }
.nav__links { display: flex; gap: 24px; flex: 1; }
.nav__link { font-family: var(--font-sans,Inter); font-size: 14px; color: var(--text-secondary,#666); cursor: pointer; transition: color 0.15s; }
.nav__link:hover, .nav__link--active { color: var(--accent,#4A9FD8); font-weight: 600; }
.nav__user { display: flex; align-items: center; gap: 10px; position: relative; }
.nav__username { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-secondary,#666); }
.nav__avatar { width: 36px; height: 36px; border-radius: 999px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; color: var(--accent,#4A9FD8); flex-shrink: 0; cursor: pointer; }

/* ===== Notification Bell ===== */
.notif-bell-area { position: relative; }
.bell-wrap { position: relative; cursor: pointer; padding: 6px; border-radius: 8px; transition: background 0.15s; display: flex; align-items: center; }
.bell-wrap:hover { background: var(--bg-secondary,#F7F8FA); }
.bell-icon { font-size: 18px; line-height: 1; }
.bell-badge { position: absolute; top: 0; right: -4px; min-width: 16px; height: 16px; border-radius: 999px; background: var(--danger,#F87171); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 700; display: flex; align-items: center; justify-content: center; padding: 0 4px; line-height: 1; }

/* Dropdown */
.notif-dropdown { position: absolute; top: calc(100% + 8px); right: 0; width: 380px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); box-shadow: 0 8px 24px rgba(0,0,0,0.1); z-index: 1000; display: flex; flex-direction: column; max-height: 480px; overflow-y: auto; }
.notif-dropdown__header { display: flex; justify-content: space-between; align-items: center; padding: 16px 16px 12px; }
.notif-dropdown__title { font-family: var(--font-sans,Inter); font-size: 16px; font-weight: 600; color: var(--text-primary,#1A1A1A); }
.notif-dropdown__markall { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--accent,#4A9FD8); cursor: pointer; }
.notif-dropdown__markall:hover { opacity: 0.8; }
.notif-dropdown__divider { height: 1px; background: var(--border,#E5E7EB); margin: 0; }
.notif-dropdown__empty { padding: 32px 16px; text-align: center; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); }
.notif-dropdown__item { display: flex; gap: 10px; padding: 12px 16px; cursor: pointer; transition: background 0.1s; align-items: flex-start; }
.notif-dropdown__item:hover { background: var(--bg-secondary,#F7F8FA); }
.notif-dropdown__footer { padding: 12px 16px; text-align: center; font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--accent,#4A9FD8); cursor: pointer; }
.notif-dropdown__footer:hover { opacity: 0.8; }

.notif-item-icon { font-size: 14px; line-height: 1.3; flex-shrink: 0; }
.notif-item-content { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.notif-item-top { display: flex; align-items: center; gap: 6px; }
.notif-item-dot { width: 8px; height: 8px; border-radius: 999px; background: var(--danger,#F87171); flex-shrink: 0; }
.notif-item-title { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; color: var(--text-primary,#1A1A1A); flex: 1; }
.notif-item-time { font-family: var(--font-sans,Inter); font-size: 11px; color: var(--text-muted,#888); flex-shrink: 0; }
.notif-item-desc { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ===== Main ===== */
.main { display: flex; gap: 32px; padding: 40px 80px; flex: 1; overflow-y: auto; }
.sidebar { width: 280px; display: flex; flex-direction: column; gap: 16px; flex-shrink: 0; }
.profile-card { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 24px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); cursor: pointer; transition: box-shadow 0.15s; }
.profile-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.profile-avatar { width: 72px; height: 72px; border-radius: 999px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 28px; font-weight: 600; color: var(--accent,#4A9FD8); }
.profile-name { font-family: var(--font-sans,Inter); font-size: 18px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.profile-no { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 12px; color: var(--text-muted,#888); margin: 0; }
.profile-badge { padding: 6px 14px; border-radius: 999px; background: var(--accent-light,#E8F4FD); font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--accent,#4A9FD8); }
.quick-links { display: flex; flex-direction: column; gap: 4px; padding: 12px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }
.quick-link { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-radius: 8px; cursor: pointer; transition: background 0.15s; }
.quick-link:hover { background: var(--accent-light,#E8F4FD); }
.quick-link--active { background: var(--accent-light,#E8F4FD); }
.quick-link__icon { font-family: var(--font-sans,Inter); font-size: 16px; color: var(--text-secondary,#666); width: 20px; text-align: center; }
.quick-link__label { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.quick-link--active .quick-link__label { color: var(--accent,#4A9FD8); font-weight: 600; }
.content { flex: 1; display: flex; flex-direction: column; gap: 24px; min-width: 0; }
</style>

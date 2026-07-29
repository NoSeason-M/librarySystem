<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getMyProfile } from '../api/readers'

const router = useRouter()
const route = useRoute()
const realName = ref(localStorage.getItem('realName') || '读者')
const userInitials = ref(realName.value.charAt(0).toUpperCase())
const readerNo = ref(localStorage.getItem('readerNo') || '')
const readerType = ref(localStorage.getItem('readerType') || '读者')

onMounted(async () => {
  // Ensure reader info is in localStorage for child pages
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
})

const sidebarLinks = [
  { icon: '📖', label: '当前借阅', route: '/reader' },
  { icon: '⏱', label: '借阅历史', route: '/reader/history' },
  { icon: '📌', label: '我的预约', route: '/reader/reservations' },
  { icon: '⭐', label: '我的收藏', route: '/reader/favorites' },
  { icon: '💰', label: '我的罚款', route: '/reader/fines' },
  { icon: '⚙️', label: '设置', route: '/reader/settings' },
]

function isActive(item: { route: string }): boolean {
  if (item.route === '/reader') {
    return route.path === '/reader'
  }
  return route.path.startsWith(item.route)
}

function getInitials(name: string): string {
  return (name || '?').charAt(0)
}
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
        <span class="nav__username">{{ realName }}</span>
        <div class="nav__avatar" @click="router.push('/reader')">{{ getInitials(realName) }}</div>
      </div>
    </nav>

    <!-- Main -->
    <main class="main">
      <!-- Sidebar -->
      <aside class="sidebar">
        <!-- Profile Card -->
        <div class="profile-card" @click="router.push('/reader')">
          <div class="profile-avatar">{{ getInitials(realName) }}</div>
          <h2 class="profile-name">{{ realName }}</h2>
          <p class="profile-no">{{ readerNo || '—' }}</p>
          <div class="profile-badge">{{ readerType }}</div>
        </div>

        <!-- Quick Links -->
        <div class="quick-links">
          <div
            v-for="item in sidebarLinks"
            :key="item.label"
            :class="['quick-link', { 'quick-link--active': isActive(item) }]"
            @click="router.push(item.route)"
          >
            <span class="quick-link__icon">{{ item.icon }}</span>
            <span class="quick-link__label">{{ item.label }}</span>
          </div>
        </div>
      </aside>

      <!-- Content -->
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.reader-layout {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary, #F7F8FA);
}

/* ===== Nav Bar ===== */
.nav {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 40px;
  background: var(--bg-primary, #FFFFFF);
  height: 68px;
  flex-shrink: 0;
}

.nav__logo {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  cursor: pointer;
}

.nav__links {
  display: flex;
  gap: 24px;
  flex: 1;
}

.nav__link {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-secondary, #666666);
  cursor: pointer;
  transition: color 0.15s;
}

.nav__link:hover,
.nav__link--active {
  color: var(--accent, #4A9FD8);
  font-weight: 600;
}

.nav__user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav__username {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary, #666);
}

.nav__avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  color: var(--accent, #4A9FD8);
  flex-shrink: 0;
  cursor: pointer;
}

/* ===== Main ===== */
.main {
  display: flex;
  gap: 32px;
  padding: 40px 80px;
  flex: 1;
  overflow-y: auto;
}

/* ===== Sidebar ===== */
.sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex-shrink: 0;
}

.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: var(--bg-primary, #FFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  cursor: pointer;
  transition: box-shadow 0.15s;
}

.profile-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 28px;
  font-weight: 600;
  color: var(--accent, #4A9FD8);
}

.profile-name {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}

.profile-no {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 12px;
  color: var(--text-muted, #888);
  margin: 0;
}

.profile-badge {
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 500;
  color: var(--accent, #4A9FD8);
}

/* Quick Links */
.quick-links {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: var(--bg-primary, #FFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.quick-link:hover {
  background: var(--accent-light, #E8F4FD);
}

.quick-link--active {
  background: var(--accent-light, #E8F4FD);
}

.quick-link__icon {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 16px;
  color: var(--text-secondary, #666);
  width: 20px;
  text-align: center;
}

.quick-link__label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
}

.quick-link--active .quick-link__label {
  color: var(--accent, #4A9FD8);
  font-weight: 600;
}

/* ===== Content ===== */
.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}
</style>

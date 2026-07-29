<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNotifications, markNotifRead, markAllNotifRead, deleteNotif, type NotifItem } from '../api/notifications'

const loading = ref(true)
const notifs = ref<NotifItem[]>([])
const unreadCount = ref(0)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const totalPages = ref(0)
const activeFilter = ref<number | undefined>(undefined)

const filterTabs = [
  { value: undefined, label: '全部' },
  { value: 0, label: '未读' },
]

onMounted(loadNotifs)

async function loadNotifs() {
  loading.value = true
  try {
    const r = await getNotifications(currentPage.value, pageSize, activeFilter.value)
    notifs.value = r.records || []
    total.value = r.total || 0
    totalPages.value = r.pages || 1
    unreadCount.value = r.unreadCount || 0
  } catch {
    notifs.value = []
  } finally {
    loading.value = false
  }
}

function switchFilter(v: number | undefined) {
  activeFilter.value = v
  currentPage.value = 1
  loadNotifs()
}

async function handleMarkRead(n: NotifItem) {
  if (n.readFlag) return
  try {
    await markNotifRead(n.id)
    n.readFlag = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch { /* ignore */ }
}

async function handleMarkAllRead() {
  try {
    await markAllNotifRead()
    notifs.value.forEach(n => n.readFlag = true)
    unreadCount.value = 0
  } catch { /* ignore */ }
}

async function handleDelete(n: NotifItem) {
  if (!confirm('确认删除该通知？')) return
  try {
    await deleteNotif(n.id)
    notifs.value = notifs.value.filter(item => item.id !== n.id)
    total.value--
  } catch { /* ignore */ }
}

function notifIcon(type: string): string {
  const icons: Record<string, string> = {
    overdue_due: '🔴', due_soon: '🟡', arrival: '🟢',
    cancel: '⚪', fine: '💰', system: '🔵',
  }
  return icons[type] || '🔔'
}

function goToPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadNotifs()
}

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  const tp = totalPages.value
  if (tp <= 5) { for (let i = 1; i <= tp; i++) pages.push(i) }
  else {
    pages.push(1); const s = Math.max(2, currentPage.value - 1); const e = Math.min(tp - 1, currentPage.value + 1)
    if (s > 2) pages.push('...'); for (let i = s; i <= e; i++) pages.push(i); if (e < tp - 1) pages.push('...'); pages.push(tp)
  }
  return pages
})

import { computed } from 'vue'
</script>

<template>
  <div class="notif-page">
    <div class="page-header">
      <div class="page-header-row">
        <h1 class="page-title">通知</h1>
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }} 条未读</span>
      </div>
      <button v-if="unreadCount > 0" class="btn-mark-all" @click="handleMarkAllRead">全部标记已读</button>
    </div>

    <!-- Filter Tabs -->
    <div class="tabs-bar">
      <div v-for="f in filterTabs" :key="String(f.value)"
        :class="['tab', { 'tab--active': activeFilter === f.value }]"
        @click="switchFilter(f.value)">
        {{ f.label }}
      </div>
    </div>

    <div v-if="loading" class="loading-msg">加载中...</div>

    <template v-if="!loading">
      <div v-if="notifs.length === 0" class="empty-state">暂无通知</div>

      <div v-for="n in notifs" :key="n.id" class="notif-card" @click="handleMarkRead(n)">
        <div class="notif-card__left">
          <span class="notif-card__icon">{{ notifIcon(n.type) }}</span>
          <div v-if="!n.readFlag" class="notif-card__dot"></div>
        </div>
        <div class="notif-card__content">
          <div class="notif-card__top">
            <span class="notif-card__title">{{ n.title }}</span>
            <span class="notif-card__time">{{ n.createTime?.slice(0, 16) }}</span>
          </div>
          <p class="notif-card__desc">{{ n.content }}</p>
          <span class="notif-card__type">{{ n.typeLabel }}</span>
        </div>
        <button class="notif-card__del" @click.stop="handleDelete(n)">✕</button>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <span class="page-info">共 {{ total }} 条</span>
        <div class="page-buttons">
          <span class="page-prev" :class="{ 'page--disabled': currentPage <= 1 }" @click="goToPage(currentPage - 1)">←</span>
          <template v-for="p in visiblePages" :key="p">
            <div v-if="typeof p === 'number'" :class="['page-num', { 'page-num--active': p === currentPage }]" @click="goToPage(p)">{{ p }}</div>
            <span v-else class="page-ellipsis">...</span>
          </template>
          <span class="page-next" :class="{ 'page--disabled': currentPage >= totalPages }" @click="goToPage(currentPage + 1)">→</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.notif-page { display: flex; flex-direction: column; gap: 28px; padding: 8px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header-row { display: flex; align-items: center; gap: 12px; }
.page-title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.unread-badge { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-inverse,#FFF); background: var(--danger,#F87171); padding: 4px 10px; border-radius: 999px; }
.btn-mark-all { padding: 8px 16px; border-radius: 8px; border: none; background: transparent; font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--accent,#4A9FD8); cursor: pointer; }
.btn-mark-all:hover { opacity: 0.8; }

.tabs-bar { display: flex; gap: 4px; background: var(--bg-primary,#FFF); border-radius: 10px; border: 1px solid var(--border,#E5E7EB); padding: 3px; width: fit-content; }
.tab { padding: 6px 16px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; transition: all 0.15s; }
.tab--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }

.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
.empty-state { padding: 48px; text-align: center; color: var(--text-muted,#888); font-size: 13px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }

.notif-card { display: flex; gap: 14px; padding: 16px 20px; background: var(--bg-primary,#FFF); border-radius: 12px; border: 1px solid var(--border,#E5E7EB); align-items: flex-start; cursor: pointer; transition: box-shadow 0.15s; }
.notif-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.notif-card__left { display: flex; flex-direction: column; align-items: center; gap: 4px; width: 20px; flex-shrink: 0; padding-top: 2px; }
.notif-card__icon { font-size: 16px; line-height: 1; }
.notif-card__dot { width: 8px; height: 8px; border-radius: 999px; background: var(--danger,#F87171); }
.notif-card__content { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.notif-card__top { display: flex; align-items: center; gap: 12px; }
.notif-card__title { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; color: var(--text-primary,#1A1A1A); }
.notif-card__time { font-family: var(--font-sans,Inter); font-size: 11px; color: var(--text-muted,#888); flex-shrink: 0; }
.notif-card__desc { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-secondary,#666); margin: 0; line-height: 1.5; }
.notif-card__type { font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; color: var(--accent,#4A9FD8); background: var(--accent-light,#E8F4FD); padding: 2px 8px; border-radius: 999px; width: fit-content; }
.notif-card__del { width: 24px; height: 24px; border-radius: 6px; border: none; background: transparent; color: var(--text-muted,#888); cursor: pointer; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.notif-card__del:hover { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--accent,#4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted,#888); cursor: default; }
.page-num { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); cursor: pointer; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); }
.page-num--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; border-color: var(--accent,#4A9FD8); }
.page-ellipsis { font-size: 12px; color: var(--text-muted,#888); padding: 0 4px; }
</style>

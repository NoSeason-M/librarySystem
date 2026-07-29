<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markNotifRead, markAllNotifRead, getUnreadCount, deleteNotif } from '../api/notifications'
import type { NotifItem } from '../api/notifications'

const router = useRouter()
const unreadCount = ref(0)
const recentNotifs = ref<NotifItem[]>([])
const notifLoading = ref(false)
const dropdownOpen = ref(false)

// Full list modal
const showFullModal = ref(false)
const fullNotifs = ref<NotifItem[]>([])
const fullTotal = ref(0)
const fullPage = ref(1)
const fullPages = ref(1)
const fullFilter = ref<number | undefined>(undefined)
const fullLoading = ref(false)

onMounted(() => {
  loadUnreadCount()
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function loadUnreadCount() {
  try { unreadCount.value = await getUnreadCount() } catch { unreadCount.value = 0 }
}

async function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
  if (dropdownOpen.value && recentNotifs.value.length === 0) {
    notifLoading.value = true
    try {
      const result = await getNotifications(1, 5)
      recentNotifs.value = result.records || []
    } catch { recentNotifs.value = [] }
    finally { notifLoading.value = false }
  }
}

function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.notif-bell-area') && !target.closest('.modal-overlay')) {
    dropdownOpen.value = false
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
    fullNotifs.value.forEach(n => n.readFlag = true)
    unreadCount.value = 0
  } catch { /* ignore */ }
}

// ===== Full List Modal =====
async function openFullList() {
  dropdownOpen.value = false
  showFullModal.value = true
  fullPage.value = 1
  await loadFullList()
}

async function loadFullList() {
  fullLoading.value = true
  try {
    const r = await getNotifications(fullPage.value, 10, fullFilter.value)
    fullNotifs.value = r.records || []
    fullTotal.value = r.total || 0
    fullPages.value = r.pages || 1
    if (r.unreadCount !== undefined) unreadCount.value = r.unreadCount
  } catch { fullNotifs.value = [] }
  finally { fullLoading.value = false }
}

function switchFilter(v: number | undefined) {
  fullFilter.value = v
  fullPage.value = 1
  loadFullList()
}

async function handleFullMarkRead(n: NotifItem) {
  if (n.readFlag) return
  try {
    await markNotifRead(n.id)
    n.readFlag = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch { /* ignore */ }
}

async function handleFullDelete(n: NotifItem) {
  if (!confirm('确认删除该通知？')) return
  try {
    await deleteNotif(n.id)
    fullNotifs.value = fullNotifs.value.filter(item => item.id !== n.id)
    fullTotal.value--
  } catch { /* ignore */ }
}

function goFullPage(p: number) {
  if (p < 1 || p > fullPages.value) return
  fullPage.value = p
  loadFullList()
}

const visiblePages = computed(() => {
  const p: (number | string)[] = []
  const tp = fullPages.value
  if (tp <= 5) { for (let i = 1; i <= tp; i++) p.push(i) }
  else {
    p.push(1); const s = Math.max(2, fullPage.value - 1); const e = Math.min(tp - 1, fullPage.value + 1)
    if (s > 2) p.push('...'); for (let i = s; i <= e; i++) p.push(i); if (e < tp - 1) p.push('...'); p.push(tp)
  }
  return p
})

function notifIcon(type: string): string {
  const icons: Record<string, string> = {
    overdue_due: '🔴', due_soon: '🟡', arrival: '🟢',
    cancel: '⚪', fine: '💰', system: '🔵',
  }
  return icons[type] || '🔔'
}
</script>

<template>
  <div class="notif-bell-area">
    <div class="bell-wrap" @click="toggleDropdown">
      <span class="bell-icon">🔔</span>
      <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>

    <transition name="fade">
      <div v-if="dropdownOpen" class="notif-dropdown">
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
        <div class="notif-dropdown__footer" @click="openFullList">查看全部通知 →</div>
      </div>
    </transition>

    <!-- Full List Modal -->
    <div v-if="showFullModal" class="modal-overlay" @click.self="showFullModal = false">
      <div class="modal">
        <div class="modal__header">
          <h2 class="modal__title">通知</h2>
          <div class="modal__header-right">
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }} 条未读</span>
            <button v-if="unreadCount > 0" class="mark-all-btn" @click="handleMarkAllRead">全部已读</button>
            <button class="modal__close" @click="showFullModal = false">✕</button>
          </div>
        </div>

        <!-- Filter tabs -->
        <div class="modal__tabs">
          <div :class="['tab', { 'tab--active': fullFilter === undefined }]" @click="switchFilter(undefined)">全部</div>
          <div :class="['tab', { 'tab--active': fullFilter === 0 }]" @click="switchFilter(0)">未读</div>
        </div>

        <div class="modal__body">
          <div v-if="fullLoading" class="loading-msg">加载中...</div>
          <div v-else-if="fullNotifs.length === 0" class="empty-msg">暂无通知</div>

          <div v-for="n in fullNotifs" :key="n.id" class="notif-row" @click="handleFullMarkRead(n)">
            <div class="notif-row__left">
              <span class="notif-row__icon">{{ notifIcon(n.type) }}</span>
              <span v-if="!n.readFlag" class="notif-row__dot"></span>
            </div>
            <div class="notif-row__content">
              <div class="notif-row__top">
                <span class="notif-row__title">{{ n.title }}</span>
                <span class="notif-row__time">{{ n.createTime?.slice(0, 16) }}</span>
              </div>
              <p class="notif-row__desc">{{ n.content }}</p>
              <span class="notif-row__type">{{ n.typeLabel }}</span>
            </div>
            <button class="notif-row__del" @click.stop="handleFullDelete(n)">✕</button>
          </div>

          <!-- Pagination -->
          <div v-if="fullPages > 1" class="pagination">
            <span class="page-info">共 {{ fullTotal }} 条</span>
            <div class="page-buttons">
              <span class="page-prev" :class="{ 'page--disabled': fullPage <= 1 }" @click="goFullPage(fullPage - 1)">←</span>
              <template v-for="p in visiblePages" :key="p">
                <div v-if="typeof p === 'number'" :class="['page-num', { 'page-num--active': p === fullPage }]" @click="goFullPage(p)">{{ p }}</div>
                <span v-else class="page-ellipsis">...</span>
              </template>
              <span class="page-next" :class="{ 'page--disabled': fullPage >= fullPages }" @click="goFullPage(fullPage + 1)">→</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notif-bell-area { position: relative; }
.bell-wrap { position: relative; cursor: pointer; padding: 6px; border-radius: 8px; transition: background 0.15s; display: flex; align-items: center; }
.bell-wrap:hover { background: var(--bg-secondary,#F7F8FA); }
.bell-icon { font-size: 18px; line-height: 1; }
.bell-badge { position: absolute; top: 0; right: -4px; min-width: 16px; height: 16px; border-radius: 999px; background: var(--danger,#F87171); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 700; display: flex; align-items: center; justify-content: center; padding: 0 4px; line-height: 1; }

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

/* ===== Modal ===== */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 2000; padding: 24px; }
.modal { width: 100%; max-width: 640px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; max-height: 80vh; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__header-right { display: flex; align-items: center; gap: 12px; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.modal__close:hover { background: var(--border,#E5E7EB); }
.modal__tabs { display: flex; gap: 4px; padding: 16px 28px 0; }
.tab { padding: 6px 16px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; }
.tab--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }
.modal__body { padding: 16px 28px 24px; display: flex; flex-direction: column; gap: 8px; overflow-y: auto; }

.unread-badge { font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; color: var(--text-inverse,#FFF); background: var(--danger,#F87171); padding: 3px 8px; border-radius: 999px; }
.mark-all-btn { padding: 6px 12px; border-radius: 6px; border: none; background: transparent; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--accent,#4A9FD8); cursor: pointer; }
.mark-all-btn:hover { opacity: 0.8; }

.loading-msg, .empty-msg { padding: 32px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }

.notif-row { display: flex; gap: 12px; padding: 14px; background: var(--bg-primary,#FFF); border-radius: 10px; border: 1px solid var(--border,#E5E7EB); align-items: flex-start; cursor: pointer; transition: box-shadow 0.1s; }
.notif-row:hover { box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.notif-row__left { display: flex; flex-direction: column; align-items: center; gap: 4px; width: 20px; flex-shrink: 0; padding-top: 2px; }
.notif-row__icon { font-size: 14px; line-height: 1; }
.notif-row__dot { width: 8px; height: 8px; border-radius: 999px; background: var(--danger,#F87171); }
.notif-row__content { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.notif-row__top { display: flex; align-items: center; gap: 12px; }
.notif-row__title { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; color: var(--text-primary,#1A1A1A); }
.notif-row__time { font-family: var(--font-sans,Inter); font-size: 11px; color: var(--text-muted,#888); flex-shrink: 0; }
.notif-row__desc { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); margin: 0; line-height: 1.4; }
.notif-row__type { font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; color: var(--accent,#4A9FD8); background: var(--accent-light,#E8F4FD); padding: 2px 8px; border-radius: 999px; width: fit-content; }
.notif-row__del { width: 24px; height: 24px; border-radius: 6px; border: none; background: transparent; color: var(--text-muted,#888); cursor: pointer; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.notif-row__del:hover { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans,Inter); font-size: 11px; color: var(--text-muted,#888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans,Inter); font-size: 11px; color: var(--accent,#4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted,#888); cursor: default; }
.page-num { width: 28px; height: 28px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 11px; color: var(--text-secondary,#666); cursor: pointer; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); }
.page-num--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; border-color: var(--accent,#4A9FD8); }
.page-ellipsis { font-size: 11px; color: var(--text-muted,#888); padding: 0 4px; }
</style>

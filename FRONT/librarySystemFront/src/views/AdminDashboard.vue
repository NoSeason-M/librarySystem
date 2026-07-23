<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCirculationStats, getCollectionStats, getReaderStats, getRecentActivity } from '../api/statistics'

const router = useRouter()

const adminNav = [
  { icon: '📊', label: 'Dashboard' },
  { icon: '📖', label: 'Borrow/Return' },
  { icon: '📚', label: 'Books' },
  { icon: '👥', label: 'Readers' },
  { icon: '📈', label: 'Statistics' },
  { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings' },
]

const stats = reactive({
  totalBooks: { value: '—', change: '' },
  activeReaders: { value: '—', change: '' },
  borrowedToday: { value: '—', change: '' },
  overdue: { value: '—', change: '' },
})

const categoryData = ref<{ name: string; count: number; percentage: number }[]>([])
const activities = ref<{ user: string; action: string; time: string; color: string }[]>([])
const todayStr = ref('')
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  todayStr.value = new Date().toLocaleDateString('en-US', {
    weekday: 'long', year: 'numeric', month: 'short', day: 'numeric',
  })
  try {
    const [circulation, collection, readerStat, activity] = await Promise.all([
      getCirculationStats().catch(() => null),
      getCollectionStats().catch(() => null),
      getReaderStats().catch(() => null),
      getRecentActivity().catch(() => []),
    ])
    if (circulation) {
      stats.borrowedToday.value = String(circulation.today?.borrowCount ?? '0')
      stats.borrowedToday.change = '+0 today'
      stats.overdue.value = String(circulation.overall?.overdueRate ?? '0') + '%'
      stats.overdue.change = 'overdue rate'
    }
    if (collection) {
      stats.totalBooks.value = String(collection.totalBooks ?? '0')
      stats.totalBooks.change = `${collection.totalCopies ?? 0} total copies`
      categoryData.value = collection.categoryDistribution ?? []
    }
    if (readerStat) {
      stats.activeReaders.value = String(readerStat.totalReaders ?? '0')
      stats.activeReaders.change = `${readerStat.activeReaders ?? 0} active`
    }
    activities.value = activity
  } catch {
    error.value = 'Failed to load dashboard data'
  } finally {
    loading.value = false
  }
})

function navigate(label: string) {
  if (label === 'Dashboard') return
  router.push('/admin')
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar__logo">
        <span>📚</span><span class="sidebar__logo-text">LibraryOS</span>
      </div>
      <nav class="sidebar__nav">
        <div v-for="item in adminNav" :key="item.label"
          class="sidebar__item"
          :class="{ 'sidebar__item--active': item.label === 'Dashboard' }"
          @click="navigate(item.label)">
          <span class="sidebar__item-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </div>
      </nav>
      <div class="sidebar__bottom">
        <div class="sidebar__avatar">SA</div>
        <span>System Admin</span>
      </div>
    </aside>

    <main class="main">
      <header class="header">
        <h1 class="header__title">Dashboard</h1>
        <div class="header__date">{{ todayStr }}</div>
      </header>

      <div v-if="error" class="error-msg">{{ error }}</div>
      <div v-if="loading" class="loading-msg">Loading dashboard...</div>

      <div v-if="!loading" class="stats-row">
        <div class="stat-card">
          <span class="stat-label">Total Books</span>
          <span class="stat-value">{{ stats.totalBooks.value }}</span>
          <span class="stat-trend" style="color:#4A9FD8">{{ stats.totalBooks.change }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Active Readers</span>
          <span class="stat-value">{{ stats.activeReaders.value }}</span>
          <span class="stat-trend" style="color:#34D399">{{ stats.activeReaders.change }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Borrowed Today</span>
          <span class="stat-value">{{ stats.borrowedToday.value }}</span>
          <span class="stat-trend" style="color:#FBBF24">{{ stats.borrowedToday.change }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Overdue</span>
          <span class="stat-value">{{ stats.overdue.value }}</span>
          <span class="stat-trend" style="color:#F87171">{{ stats.overdue.change }}</span>
        </div>
      </div>

      <div v-if="!loading" class="two-col">
        <div class="col-left">
          <h2 class="section-title">📋 Recent Activity</h2>
          <div class="activity-list">
            <div v-if="activities.length === 0" class="empty-state">No recent activity</div>
            <div v-for="(act, i) in activities" :key="i" class="activity-item">
              <span class="activity-dot" :style="{ background: act.color }"></span>
              <div class="activity-info">
                <p class="activity-desc">{{ act.user }} {{ act.action }}</p>
                <p class="activity-time">{{ act.time }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="col-right">
          <h2 class="section-title">⚡ Quick Actions</h2>
          <div class="quick-actions">
            <div v-for="btn in [
              { i:'📕', l:'Borrow Book' }, { i:'📗', l:'Return Book' },
              { i:'➕', l:'Add New Book' }, { i:'👤', l:'Register Reader' }
            ]" :key="btn.l" class="action-btn">
              <span>{{ btn.i }}</span>
              <span class="action-btn__label">{{ btn.l }}</span>
            </div>
          </div>

          <h2 class="section-title">📊 Collection by Category</h2>
          <div class="category-chart">
            <div v-if="categoryData.length === 0" class="empty-state">No category data</div>
            <div v-for="cat in categoryData" :key="cat.name" class="cat-row">
              <span class="cat-name">{{ cat.name }}</span>
              <div class="cat-bar-bg">
                <div class="cat-bar-fill" :style="{ width: Math.max(cat.percentage * 2.8, 6) + 'px' }"></div>
              </div>
              <span class="cat-pct">{{ cat.percentage }}%</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; background: var(--bg-secondary, #F7F8FA); }

/* Sidebar */
.sidebar { width: 240px; background: var(--bg-inverse, #0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse,#FFF); }
.sidebar__logo-text { font-family: var(--font-sans,Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); transition: background 0.15s; }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-inverse,#FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent,#4A9FD8); flex-shrink: 0; }

/* Main */
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.header__date { padding: 8px 14px; border-radius: 999px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); font-size: 12px; color: var(--text-secondary,#666); }

.error-msg { padding: 12px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.loading-msg { padding: 20px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }

/* Stats */
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 8px; }
.stat-label { font-size: 12px; color: var(--text-muted,#888); }
.stat-value { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 28px; font-weight: 700; color: var(--text-primary,#1A1A1A); line-height: 1.2; }
.stat-trend { font-size: 12px; }

/* Two column */
.two-col { display: flex; gap: 20px; flex: 1; }
.col-left { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.col-right { width: 360px; display: flex; flex-direction: column; gap: 16px; flex-shrink: 0; }
.section-title { font-family: var(--font-sans,Inter); font-size: 16px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }

/* Activity */
.activity-list { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); padding: 8px; display: flex; flex-direction: column; gap: 2px; }
.activity-item { display: flex; gap: 12px; align-items: center; padding: 10px 12px; }
.activity-dot { width: 8px; height: 8px; border-radius: 999px; flex-shrink: 0; }
.activity-info { flex: 1; }
.activity-desc { font-size: 13px; color: var(--text-primary,#1A1A1A); margin: 0; }
.activity-time { font-size: 11px; color: var(--text-muted,#888); margin: 0; margin-top: 2px; }

/* Quick Actions */
.quick-actions { display: flex; flex-direction: column; gap: 8px; }
.action-btn { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 12px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; transition: box-shadow 0.15s; font-size: 20px; }
.action-btn:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.action-btn__label { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; color: var(--text-primary,#1A1A1A); }

/* Category Chart */
.category-chart { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.cat-row { display: flex; gap: 8px; align-items: center; }
.cat-name { font-size: 12px; color: var(--text-secondary,#666); width: 100px; flex-shrink: 0; }
.cat-bar-bg { width: 140px; height: 8px; border-radius: 999px; background: var(--bg-secondary,#F7F8FA); flex-shrink: 0; }
.cat-bar-fill { height: 8px; border-radius: 999px; background: var(--accent,#4A9FD8); transition: width 0.5s ease; }
.cat-pct { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 11px; color: var(--text-muted,#888); }

.empty-state { font-size: 13px; color: var(--text-muted,#888); padding: 20px; text-align: center; }
</style>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCirculationStats, getCollectionStats, getReaderStats, getRecentActivity } from '../api/statistics'

const router = useRouter()

const adminNav = [
  { icon: '📊', label: '工作台' },
  { icon: '📖', label: '借还管理' },
  { icon: '📚', label: '图书管理' },
  { icon: '👥', label: '读者管理' },
  { icon: '📈', label: '统计分析' },
  { icon: '💰', label: '罚款管理' },
  { icon: '⚙️', label: '系统设置' },
]

const dayjs = ref('')

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

const categoryNames = computed(() => categoryData.value.slice(0, 5))

onMounted(async () => {
  todayStr.value = new Date().toLocaleDateString('zh-CN', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
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
      stats.borrowedToday.change = '今日新增'
      stats.overdue.value = String(circulation.overall?.overdueRate ?? '0') + '%'
      stats.overdue.change = '逾期率'
    }
    if (collection) {
      stats.totalBooks.value = String(collection.totalBooks ?? '0')
      stats.totalBooks.change = `${collection.totalCopies ?? 0} 本总副本`
      categoryData.value = collection.categoryDistribution ?? []
    }
    if (readerStat) {
      stats.activeReaders.value = String(readerStat.totalReaders ?? '0')
      stats.activeReaders.change = `${readerStat.activeReaders ?? 0} 活跃`
    }
    activities.value = activity
  } catch {
    error.value = '加载数据失败'
  } finally {
    loading.value = false
  }
})

function navigate(label: string) {
  if (label === '工作台') return
  if (label === '图书管理') { router.push('/admin/books'); return }
  if (label === '读者管理') { router.push('/admin/readers'); return }
  if (label === '统计分析') { router.push('/admin/statistics'); return }
  if (label === '系统设置') { router.push('/admin/settings'); return }
  router.push('/admin')
}

function quickAction(label: string) {
  const routes: Record<string, string> = {
    '借书': '/admin/borrow',
    '还书': '/admin/return',
    '新增图书': '/admin/books',
    '注册读者': '/admin/readers',
  }
  const path = routes[label]
  if (path) router.push(path)
}
</script>

<template>
  <div class="admin-layout">

    <main class="main">
      <header class="header">
        <h1 class="header__title">工作台</h1>
        <div class="header__date">{{ todayStr }}</div>
      </header>

      <div v-if="error" class="error-msg">{{ error }}</div>
      <div v-if="loading" class="loading-msg">加载中...</div>

      <div v-if="!loading" class="stats-row">
        <div class="stat-card">
          <span class="stat-label">总藏书量</span>
          <span class="stat-value">{{ stats.totalBooks.value }}</span>
          <span class="stat-trend" style="color:#4A9FD8">{{ stats.totalBooks.change }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">活跃读者</span>
          <span class="stat-value">{{ stats.activeReaders.value }}</span>
          <span class="stat-trend" style="color:#34D399">{{ stats.activeReaders.change }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">今日借出</span>
          <span class="stat-value">{{ stats.borrowedToday.value }}</span>
          <span class="stat-trend" style="color:#FBBF24">{{ stats.borrowedToday.change }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">逾期率</span>
          <span class="stat-value">{{ stats.overdue.value }}</span>
          <span class="stat-trend" style="color:#F87171">{{ stats.overdue.change }}</span>
        </div>
      </div>

      <div v-if="!loading" class="two-col">
        <div class="col-left">
          <h2 class="section-title">📋 最近活动</h2>
          <div class="activity-list">
            <div v-if="activities.length === 0" class="empty-state">暂无最近活动</div>
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
          <h2 class="section-title">⚡ 快捷操作</h2>
          <div class="quick-actions">
            <div v-for="btn in [
              { i:'📕', l:'借书' }, { i:'📗', l:'还书' },
              { i:'➕', l:'新增图书' }, { i:'👤', l:'注册读者' }
            ]" :key="btn.l" class="action-btn" @click="quickAction(btn.l)">
              <span>{{ btn.i }}</span>
              <span class="action-btn__label">{{ btn.l }}</span>
            </div>
          </div>

          <h2 class="section-title">📊 馆藏分类</h2>
          <div class="category-chart">
            <div v-if="categoryData.length === 0" class="empty-state">暂无分类数据</div>
            <div v-for="cat in categoryNames" :key="cat.name" class="cat-row">
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
.admin-layout { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary, #F7F8FA); }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.header__date { padding: 8px 14px; border-radius: 999px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); font-size: 12px; color: var(--text-secondary,#666); }
.error-msg { padding: 12px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.loading-msg { padding: 20px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 8px; }
.stat-label { font-size: 12px; color: var(--text-muted,#888); }
.stat-value { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 28px; font-weight: 700; color: var(--text-primary,#1A1A1A); line-height: 1.2; }
.stat-trend { font-size: 12px; }
.two-col { display: flex; gap: 20px; flex: 1; }
.col-left { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.col-right { width: 360px; display: flex; flex-direction: column; gap: 16px; flex-shrink: 0; }
.section-title { font-family: var(--font-sans,Inter); font-size: 16px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.activity-list { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); padding: 8px; display: flex; flex-direction: column; gap: 2px; }
.activity-item { display: flex; gap: 12px; align-items: center; padding: 10px 12px; }
.activity-dot { width: 8px; height: 8px; border-radius: 999px; flex-shrink: 0; }
.activity-info { flex: 1; }
.activity-desc { font-size: 13px; color: var(--text-primary,#1A1A1A); margin: 0; }
.activity-time { font-size: 11px; color: var(--text-muted,#888); margin: 0; margin-top: 2px; }
.quick-actions { display: flex; flex-direction: column; gap: 8px; }
.action-btn { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 12px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; transition: box-shadow 0.15s; font-size: 20px; }
.action-btn:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.action-btn__label { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.category-chart { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.cat-row { display: flex; gap: 8px; align-items: center; }
.cat-name { font-size: 12px; color: var(--text-secondary,#666); width: 100px; flex-shrink: 0; }
.cat-bar-bg { width: 140px; height: 8px; border-radius: 999px; background: var(--bg-secondary,#F7F8FA); flex-shrink: 0; }
.cat-bar-fill { height: 8px; border-radius: 999px; background: var(--accent,#4A9FD8); transition: width 0.5s ease; }
.cat-pct { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 11px; color: var(--text-muted,#888); }
.empty-state { font-size: 13px; color: var(--text-muted,#888); padding: 20px; text-align: center; }
</style>

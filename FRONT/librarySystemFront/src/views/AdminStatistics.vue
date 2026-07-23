<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

// ===== Tree-shaken ECharts (reduces bundle from 1.1MB to ~200KB) =====
import { init, use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
use([BarChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])
import type { ECharts } from 'echarts/core'

import { getBorrowStats, getHotBooks, getCollectionStats, getReaderStats } from '../api/statistics'

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

const loading = ref(true)
const granularity = ref('month')

const borrowChartRef = ref<HTMLDivElement>()
const categoryChartRef = ref<HTMLDivElement>()
const readerChartRef = ref<HTMLDivElement>()
let borrowChart: ECharts | null = null
let categoryChart: ECharts | null = null
let readerChart: ECharts | null = null

const CATEGORY_COLORS = ['#4A9FD8', '#34D399', '#FBBF24', '#F87171', '#A78BFA', '#F472B6', '#F97316', '#06B6D4']

const borrowSummary = ref({ totalBorrow: 0, totalReturn: 0, avgDailyBorrow: 0, peakDay: '' })
const borrowChartData = ref<{ date: string; borrowCount: number; returnCount: number }[]>([])
const hotBooks = ref<any[]>([])
const collectionStats = ref({ totalBooks: 0, totalCopies: 0 })
const categoryData = ref<{ name: string; count: number; percentage: number }[]>([])
const readerStats = ref({ totalReaders: 0, activeReaders: 0, activeRate: 0 })
const readerTypeDist = ref<{ name: string; count: number; percentage: number }[]>([])

function navigateTo(label: string) {
  if (label === 'Dashboard') router.push('/admin')
  else if (label === 'Books') router.push('/admin/books')
  else if (label === 'Readers') router.push('/admin/readers')
}

function initBorrowChart() {
  if (!borrowChartRef.value || borrowChartData.value.length === 0) return
  borrowChart = init(borrowChartRef.value)
  borrowChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['Borrow', 'Return'], bottom: 0, textStyle: { fontSize: 11, color: '#888' } },
    grid: { left: 44, right: 16, top: 16, bottom: 36 },
    xAxis: { type: 'category', data: borrowChartData.value.map(d => d.date), axisLabel: { fontSize: 10, color: '#888' }, axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F0F0F0' } }, axisLabel: { fontSize: 10, color: '#888' } },
    series: [
      { name: 'Borrow', type: 'bar', data: borrowChartData.value.map(d => d.borrowCount), itemStyle: { color: '#4A9FD8', borderRadius: [4, 4, 0, 0] }, barWidth: 10 },
      { name: 'Return', type: 'bar', data: borrowChartData.value.map(d => d.returnCount), itemStyle: { color: '#34D399', borderRadius: [4, 4, 0, 0] }, barWidth: 10 },
    ],
  })
}

function initCategoryChart() {
  if (!categoryChartRef.value || categoryData.value.length === 0) return
  categoryChart = init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['40%', '68%'], avoidLabelOverlap: true, padAngle: 2,
      itemStyle: { borderRadius: 6 }, label: { show: false }, labelLine: { show: false },
      data: categoryData.value.map((d, i) => ({ name: d.name, value: d.count, itemStyle: { color: CATEGORY_COLORS[i % CATEGORY_COLORS.length] } })),
    }],
  })
}

function initReaderChart() {
  if (!readerChartRef.value || readerTypeDist.value.length === 0) return
  readerChart = init(readerChartRef.value)
  readerChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['40%', '68%'], avoidLabelOverlap: true, padAngle: 2,
      itemStyle: { borderRadius: 6 }, label: { show: false }, labelLine: { show: false },
      data: readerTypeDist.value.map((d, i) => ({ name: d.name, value: d.count, itemStyle: { color: CATEGORY_COLORS[i % CATEGORY_COLORS.length] } })),
    }],
  })
}

function initAllCharts() { nextTick(() => { initBorrowChart(); initCategoryChart(); initReaderChart() }) }

function handleResize() { borrowChart?.resize(); categoryChart?.resize(); readerChart?.resize() }

function switchGranularity(type: string) { granularity.value = type; loadData() }

async function loadData() {
  loading.value = true
  try {
    const [borrow, hot, collection, readers] = await Promise.all([
      getBorrowStats(undefined, undefined, granularity.value).catch(() => ({ chartData: [], summary: {} })),
      getHotBooks('month', 5).catch(() => []),
      getCollectionStats().catch(() => ({ totalBooks: 0, totalCopies: 0, categoryDistribution: [] })),
      getReaderStats().catch(() => ({ totalReaders: 0, activeReaders: 0, activeRate: 0, typeDistribution: [] })),
    ])
    borrowChartData.value = borrow.chartData || []
    borrowSummary.value = borrow.summary as any || { totalBorrow: 0, totalReturn: 0, avgDailyBorrow: 0, peakDay: '' }
    hotBooks.value = hot || []
    collectionStats.value = { totalBooks: collection.totalBooks || 0, totalCopies: collection.totalCopies || 0 }
    categoryData.value = collection.categoryDistribution || []
    readerStats.value = { totalReaders: readers.totalReaders || 0, activeReaders: readers.activeReaders || 0, activeRate: readers.activeRate || 0 }
    readerTypeDist.value = readers.typeDistribution || []
    nextTick(() => { borrowChart?.dispose(); categoryChart?.dispose(); readerChart?.dispose(); borrowChart = null; categoryChart = null; readerChart = null; initAllCharts() })
  } catch {} finally { loading.value = false }
}

onMounted(() => { loadData(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { borrowChart?.dispose(); categoryChart?.dispose(); readerChart?.dispose(); window.removeEventListener('resize', handleResize) })
</script>

<template>
  <div class="admin-stats">

    <main class="main">
      <header class="header"><h1 class="header__title">Statistics</h1><div class="header__tabs"><button :class="['tab-btn', { 'tab-btn--active': granularity === 'month' }]" @click="switchGranularity('month')">Monthly</button><button :class="['tab-btn', { 'tab-btn--active': granularity === 'week' }]" @click="switchGranularity('week')">Weekly</button><button :class="['tab-btn', { 'tab-btn--active': granularity === 'day' }]" @click="switchGranularity('day')">Daily</button></div></header>
      <div v-if="loading" class="loading-msg">Loading statistics...</div>
      <template v-if="!loading">
        <div class="row">
          <div class="card card--lg"><div class="card__header"><h2 class="card__title">📊 Borrow Statistics</h2></div><div class="summary-row"><div class="summary-item"><span class="summary-label">Total Borrow</span><span class="summary-value">{{ borrowSummary.totalBorrow }}</span></div><div class="summary-item"><span class="summary-label">Total Return</span><span class="summary-value">{{ borrowSummary.totalReturn }}</span></div><div class="summary-item"><span class="summary-label">Avg Daily</span><span class="summary-value">{{ borrowSummary.avgDailyBorrow }}</span></div><div class="summary-item"><span class="summary-label">Peak Day</span><span class="summary-value" style="color:var(--accent,#4A9FD8)">{{ borrowSummary.peakDay }}</span></div></div><div ref="borrowChartRef" class="chart-borrow"></div></div>
          <div class="card card--sm"><div class="card__header"><h2 class="card__title">🔥 Hot Books</h2></div><div class="hot-list"><div v-for="(book, i) in hotBooks" :key="book.id" class="hot-item"><div :class="['hot-rank', { 'hot-rank--top': i < 3 }]">{{ book.rank }}</div><div class="hot-info"><span class="hot-title">{{ book.title }}</span><span class="hot-author">{{ book.author }}</span></div><span class="hot-count">{{ book.borrowCount }}</span></div><div v-if="hotBooks.length === 0" class="empty-msg">No data yet</div></div></div>
        </div>
        <div class="row">
          <div class="card card--lg"><div class="card__header"><h2 class="card__title">📚 Collection Stats</h2></div><div class="summary-row"><div class="summary-item"><span class="summary-label">Total Books</span><span class="summary-value">{{ collectionStats.totalBooks }}</span></div><div class="summary-item"><span class="summary-label">Total Copies</span><span class="summary-value">{{ collectionStats.totalCopies }}</span></div></div><div class="chart-row"><div class="donut-wrapper"><div ref="categoryChartRef" class="chart-donut"></div></div><div class="chart-legend-col"><div v-for="(d, i) in categoryData" :key="d.name" class="legend-item"><span class="legend-dot" :style="{ background: CATEGORY_COLORS[i % CATEGORY_COLORS.length] }"></span><span class="legend-label">{{ d.name }}</span><span class="legend-value">{{ d.percentage }}%</span></div></div></div></div>
          <div class="card card--sm"><div class="card__header"><h2 class="card__title">👥 Reader Stats</h2></div><div class="reader-metrics"><div class="reader-metric"><span class="metric-label">Total Registered</span><span class="metric-value">{{ readerStats.totalReaders }}</span></div><div class="reader-metric"><span class="metric-label">Active (30 days)</span><span class="metric-value" style="color:var(--success,#34D399)">{{ readerStats.activeReaders }}</span></div><div class="reader-metric"><span class="metric-label">Active Rate</span><span class="metric-value" style="color:var(--accent,#4A9FD8)">{{ readerStats.activeRate }}%</span></div></div><div class="chart-row chart-row--compact"><div class="donut-wrapper donut-wrapper--sm"><div ref="readerChartRef" class="chart-donut reader-chart"></div></div><div class="chart-legend-col"><div v-for="(d, i) in readerTypeDist" :key="d.name" class="legend-item"><span class="legend-dot" :style="{ background: CATEGORY_COLORS[i % CATEGORY_COLORS.length] }"></span><span class="legend-label">{{ d.name }}</span><span class="legend-value">{{ d.percentage }}%</span></div></div></div></div>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.admin-stats { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary, #F7F8FA); }
.sidebar { width: 240px; background: var(--bg-inverse, #0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse, #FFF); }
.sidebar__logo-text { font-family: var(--font-sans, Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-muted, #888); transition: background 0.15s; }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item--active { background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-weight: 600; }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-inverse, #FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light, #E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent, #4A9FD8); flex-shrink: 0; }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans, Inter); font-size: 24px; font-weight: 700; color: var(--text-primary, #1A1A1A); margin: 0; }
.header__tabs { display: flex; gap: 4px; background: var(--bg-primary, #FFF); border-radius: 10px; border: 1px solid var(--border, #E5E7EB); padding: 3px; }
.tab-btn { padding: 6px 16px; border-radius: 8px; border: none; background: transparent; font-family: var(--font-sans, Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary, #666); cursor: pointer; transition: all 0.15s; }
.tab-btn--active { background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted, #888); font-size: 14px; }
.empty-msg { padding: 20px; text-align: center; color: var(--text-muted, #888); font-size: 12px; }
.row { display: flex; gap: 20px; }
.card { background: var(--bg-primary, #FFF); border-radius: var(--card-radius, 16px); border: 1px solid var(--border, #E5E7EB); padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.card--lg { flex: 1; }
.card--sm { width: 340px; flex-shrink: 0; }
.card__header { display: flex; justify-content: space-between; align-items: center; }
.card__title { font-family: var(--font-sans, Inter); font-size: 16px; font-weight: 600; color: var(--text-primary, #1A1A1A); margin: 0; }
.summary-row { display: flex; gap: 12px; }
.summary-item { flex: 1; padding: 12px; background: var(--bg-secondary, #F7F8FA); border-radius: 10px; display: flex; flex-direction: column; align-items: center; gap: 4px; }
.summary-label { font-family: var(--font-sans, Inter); font-size: 11px; color: var(--text-muted, #888); }
.summary-value { font-family: var(--font-mono, 'Geist Mono', monospace); font-size: 22px; font-weight: 700; color: var(--text-primary, #1A1A1A); }
.chart-borrow { width: 100%; height: 260px; }
.chart-row { display: flex; align-items: center; gap: 20px; justify-content: center; }
.chart-row--compact { gap: 12px; }
.donut-wrapper { display: flex; align-items: center; justify-content: center; width: 220px; height: 220px; }
.donut-wrapper--sm { width: 180px; height: 180px; }
.chart-donut { width: 100%; height: 100%; }
.chart-legend-col { display: flex; flex-direction: column; gap: 8px; flex: 1; max-width: 200px; }
.legend-item { display: flex; align-items: center; gap: 8px; }
.legend-dot { width: 10px; height: 10px; border-radius: 999px; flex-shrink: 0; }
.legend-label { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-secondary, #666); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.legend-value { font-family: var(--font-mono, 'Geist Mono', monospace); font-size: 12px; font-weight: 600; color: var(--text-primary, #1A1A1A); }
.hot-list { display: flex; flex-direction: column; gap: 8px; }
.hot-item { display: flex; align-items: center; gap: 10px; }
.hot-rank { width: 24px; height: 24px; border-radius: 999px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans, Inter); font-size: 11px; font-weight: 500; color: var(--text-secondary, #666); background: var(--bg-secondary, #F7F8FA); flex-shrink: 0; }
.hot-rank--top { background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-weight: 600; }
.hot-info { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.hot-title { font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 500; color: var(--text-primary, #1A1A1A); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-author { font-family: var(--font-sans, Inter); font-size: 11px; color: var(--text-muted, #888); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-count { font-family: var(--font-mono, 'Geist Mono', monospace); font-size: 13px; font-weight: 600; color: var(--accent, #4A9FD8); white-space: nowrap; }
.reader-metrics { display: flex; flex-direction: column; gap: 8px; }
.reader-metric { padding: 14px; background: var(--bg-secondary, #F7F8FA); border-radius: 10px; display: flex; flex-direction: column; align-items: center; gap: 4px; }
.metric-label { font-family: var(--font-sans, Inter); font-size: 11px; color: var(--text-muted, #888); }
.metric-value { font-family: var(--font-mono, 'Geist Mono', monospace); font-size: 24px; font-weight: 700; color: var(--text-primary, #1A1A1A); }
</style>

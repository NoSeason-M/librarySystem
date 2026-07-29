<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBorrowHistory } from '../api/borrow'

const loading = ref(true)
const activeFilter = ref('30')
const startDate = ref('')
const endDate = ref('')
const historyList = ref<any[]>([])

const timeFilters = [
  { key: '7', label: '近7天' },
  { key: '30', label: '近30天' },
  { key: '90', label: '近90天' },
  { key: 'all', label: '全部' },
]

onMounted(async () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  startDate.value = start.toISOString().slice(0, 10)
  endDate.value = end.toISOString().slice(0, 10)

  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    historyList.value = await getBorrowHistory(readerNo, startDate.value, endDate.value)
  } catch {
    historyList.value = getDemoHistory()
  } finally {
    loading.value = false
  }
})

function getDemoHistory() {
  return [
    { bookTitle: '三体', barcode: '9787536692930-002', borrowDate: '2026-06-01', dueDate: '2026-06-30', returnDate: '2026-06-25', status: '已归还' },
    { bookTitle: '围城', barcode: '9787020024759-001', borrowDate: '2026-05-10', dueDate: '2026-06-09', returnDate: '2026-06-12', status: '逾期归还' },
    { bookTitle: '人类简史', barcode: '9787508660752-002', borrowDate: '2026-04-01', dueDate: '2026-04-30', returnDate: '2026-04-28', status: '已归还' },
    { bookTitle: '深入理解Java虚拟机', barcode: '9787111641247-003', borrowDate: '2026-03-15', dueDate: '2026-04-14', returnDate: '2026-04-10', status: '已归还' },
  ]
}

function selectFilter(key: string) {
  activeFilter.value = key
}

function statusColor(status: string): string {
  if (status === '已归还') return 'var(--success,#34D399)'
  if (status === '逾期归还') return 'var(--warning,#FBBF24)'
  return 'var(--text-secondary,#666)'
}
</script>

<template>
  <div class="history-page">
    <div class="page-header">
      <h1 class="page-title">借阅历史</h1>
    </div>

    <div v-if="loading" class="loading-msg">加载中...</div>

    <template v-if="!loading">
      <!-- Filter -->
      <div class="filter-row">
        <span class="filter-label">时间范围：</span>
        <div class="filter-tabs">
          <div v-for="f in timeFilters" :key="f.key"
            :class="['filter-tab', { 'filter-tab--active': activeFilter === f.key }]"
            @click="selectFilter(f.key)">
            {{ f.label }}
          </div>
        </div>
        <div class="date-range">
          <div class="date-box">
            <input v-model="startDate" type="date" class="date-input" />
          </div>
          <span class="date-sep">—</span>
          <div class="date-box">
            <input v-model="endDate" type="date" class="date-input" />
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-label">总借阅</span>
          <span class="stat-value stat-value--accent">15<span class="stat-unit">本</span></span>
        </div>
        <div class="stat-card">
          <span class="stat-label">总归还</span>
          <span class="stat-value stat-value--success">14<span class="stat-unit">本</span></span>
        </div>
        <div class="stat-card">
          <span class="stat-label">总逾期</span>
          <span class="stat-value stat-value--danger">1<span class="stat-unit">次</span></span>
        </div>
      </div>

      <!-- Table -->
      <div class="table">
        <div class="table-head">
          <span class="th" style="width:200px">书名</span>
          <span class="th" style="width:160px">条码号</span>
          <span class="th" style="width:120px">借出日期</span>
          <span class="th" style="width:120px">应还日期</span>
          <span class="th" style="width:120px">归还日期</span>
          <span class="th" style="width:100px">状态</span>
        </div>

        <div v-if="historyList.length === 0" class="table-empty">暂无借阅记录</div>
        <div v-for="(item, i) in historyList" :key="i" class="table-row">
          <span class="td td--title" style="width:200px">{{ item.bookTitle }}</span>
          <span class="td td--mono" style="width:160px">{{ item.barcode }}</span>
          <span class="td" style="width:120px">{{ item.borrowDate }}</span>
          <span class="td" style="width:120px">{{ item.dueDate }}</span>
          <span class="td" style="width:120px">{{ item.returnDate }}</span>
          <span class="td" style="width:100px" :style="{ color: statusColor(item.status), fontWeight: 500 }">{{ item.status }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.history-page { display: flex; flex-direction: column; gap: 28px; padding: 8px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
.filter-row { display: flex; align-items: center; gap: 12px; }
.filter-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-secondary,#666); }
.filter-tabs { display: flex; gap: 4px; background: var(--bg-primary,#FFF); border-radius: 10px; border: 1px solid var(--border,#E5E7EB); padding: 3px; }
.filter-tab { padding: 8px 14px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; transition: all 0.15s; }
.filter-tab--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }
.date-range { display: flex; align-items: center; gap: 8px; margin-left: auto; }
.date-box { padding: 8px 12px; border-radius: var(--input-radius,12px); background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); }
.date-input { background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); width: 110px; }
.date-sep { color: var(--text-muted,#888); font-size: 14px; }
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-label { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); }
.stat-value { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 28px; font-weight: 700; }
.stat-value--accent { color: var(--accent,#4A9FD8); }
.stat-value--danger { color: var(--danger,#F87171); }
.stat-value--success { color: var(--success,#34D399); }
.stat-unit { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 400; margin-left: 2px; }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; padding: 12px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.table-row { display: flex; padding: 12px 20px; align-items: center; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); flex-shrink: 0; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
</style>

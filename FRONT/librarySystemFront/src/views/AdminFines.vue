<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { listAdminFines, getFinesSummary, payFine, waiveFine, batchPayFines, batchWaiveFines, type FineItem } from '../api/fines'

const loading = ref(true)
const keyword = ref('')
const selectedFineType = ref('')
const selectedPaid = ref<number | undefined>(undefined)

const fines = ref<FineItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

const summary = ref({ unpaidCount: 0, unpaidAmount: 0, thisMonthCount: 0, paidThisMonth: 0 })
const selectedIds = ref<Set<number>>(new Set())

const fineTypeOptions = [
  { value: '', label: '全部类型' },
  { value: 'overdue', label: '逾期罚款' },
  { value: 'damage', label: '损坏赔偿' },
  { value: 'lost', label: '丢失赔偿' },
]
const paidOptions = [
  { value: undefined, label: '全部状态' },
  { value: 0, label: '未缴' },
  { value: 1, label: '已缴' },
]

const todayStr = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long',
})

const fineTypeDropdownOpen = ref(false)
const paidDropdownOpen = ref(false)

onMounted(async () => {
  await Promise.all([loadFines(), loadSummary()])
})

async function loadFines() {
  loading.value = true
  try {
    const result = await listAdminFines({
      keyword: keyword.value || undefined,
      fineType: selectedFineType.value || undefined,
      paid: selectedPaid.value,
      page: currentPage.value,
      size: pageSize.value,
    })
    fines.value = result.records || []
    total.value = result.total || 0
    totalPages.value = result.pages || 1
  } catch {
    fines.value = []
  } finally {
    loading.value = false
  }
}

async function loadSummary() {
  try {
    summary.value = await getFinesSummary()
  } catch { /* ignore */ }
}

function onSearch() {
  currentPage.value = 1
  loadFines()
}

function goToPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadFines()
}

function toggleSelect(id: number) {
  const s = new Set(selectedIds.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  selectedIds.value = s
}

function toggleSelectAll() {
  if (selectedIds.value.size === fines.value.length) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(fines.value.map(f => f.id))
  }
}

async function handlePay(id: number) {
  try {
    await payFine(id)
    loadFines()
    loadSummary()
  } catch (err: any) {
    alert(err.message || '缴纳失败')
  }
}

async function handleWaive(id: number) {
  if (!confirm('确认豁免该罚款？')) return
  try {
    await waiveFine(id, '管理员豁免')
    loadFines()
    loadSummary()
  } catch (err: any) {
    alert(err.message || '豁免失败')
  }
}

async function handleBatchPay() {
  const ids = Array.from(selectedIds.value)
  if (ids.length === 0) { alert('请先选择罚款项'); return }
  if (!confirm(`确认批量缴纳 ${ids.length} 条罚款？`)) return
  try {
    await batchPayFines(ids)
    selectedIds.value = new Set()
    loadFines()
    loadSummary()
  } catch (err: any) {
    alert(err.message || '批量缴纳失败')
  }
}

async function handleBatchWaive() {
  const ids = Array.from(selectedIds.value)
  if (ids.length === 0) { alert('请先选择罚款项'); return }
  if (!confirm(`确认批量豁免 ${ids.length} 条罚款？`)) return
  try {
    await batchWaiveFines(ids, '管理员批量豁免')
    selectedIds.value = new Set()
    loadFines()
    loadSummary()
  } catch (err: any) {
    alert(err.message || '批量豁免失败')
  }
}

const visiblePages = computed(() => {
  const p: (number | string)[] = []
  const tp = totalPages.value
  if (tp <= 5) { for (let i = 1; i <= tp; i++) p.push(i) }
  else {
    p.push(1); const s = Math.max(2, currentPage.value - 1); const e = Math.min(tp - 1, currentPage.value + 1)
    if (s > 2) p.push('...'); for (let i = s; i <= e; i++) p.push(i); if (e < tp - 1) p.push('...'); p.push(tp)
  }
  return p
})

function fineAmountColor(f: FineItem): string {
  if (f.paid || f.waive) return 'var(--success,#34D399)'
  return 'var(--danger,#F87171)'
}

function fineStatus(f: FineItem): string {
  if (f.paid) return '已缴纳'
  if (f.waive) return '已豁免'
  return '未缴'
}
</script>

<template>
  <div class="admin-fines">
    <main class="main">
      <!-- Header -->
      <header class="header">
        <h1 class="header__title">罚款管理</h1>
        <span class="header__date">{{ todayStr }}</span>
      </header>

      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-label">未缴罚款</span>
          <span class="stat-value stat-value--danger">{{ summary.unpaidCount }}<span class="stat-unit">笔</span></span>
        </div>
        <div class="stat-card">
          <span class="stat-label">未缴总额</span>
          <span class="stat-value stat-value--danger">¥{{ summary.unpaidAmount.toFixed(2) }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">本月新增</span>
          <span class="stat-value stat-value--accent">{{ summary.thisMonthCount }}<span class="stat-unit">笔</span></span>
        </div>
        <div class="stat-card">
          <span class="stat-label">已缴本月</span>
          <span class="stat-value stat-value--success">¥{{ summary.paidThisMonth.toFixed(2) }}</span>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input v-model="keyword" class="search-input" placeholder="搜索读者证号或姓名..." @keyup.enter="onSearch" />
          <button class="search-btn" @click="onSearch">搜索</button>
        </div>

        <div class="filter-dropdown" @click="fineTypeDropdownOpen = !fineTypeDropdownOpen; paidDropdownOpen = false">
          <span>{{ fineTypeOptions.find(o => o.value === selectedFineType)?.label || '全部类型' }}</span>
          <span class="filter-arrow">▼</span>
          <div v-if="fineTypeDropdownOpen" class="dropdown-menu">
            <div v-for="opt in fineTypeOptions" :key="opt.value"
              class="dropdown-item" :class="{ 'dropdown-item--active': selectedFineType === opt.value }"
              @click.stop="selectedFineType = opt.value; fineTypeDropdownOpen = false; onSearch()">{{ opt.label }}</div>
          </div>
        </div>

        <div class="filter-dropdown" @click="paidDropdownOpen = !paidDropdownOpen; fineTypeDropdownOpen = false">
          <span>{{ paidOptions.find(o => o.value === selectedPaid)?.label || '全部状态' }}</span>
          <span class="filter-arrow">▼</span>
          <div v-if="paidDropdownOpen" class="dropdown-menu">
            <div v-for="opt in paidOptions" :key="String(opt.value)"
              class="dropdown-item" :class="{ 'dropdown-item--active': selectedPaid === opt.value }"
              @click.stop="selectedPaid = opt.value; paidDropdownOpen = false; onSearch()">{{ opt.label }}</div>
          </div>
        </div>
      </div>

      <!-- Batch Actions -->
      <div class="batch-bar">
        <span class="batch-label">批量操作：</span>
        <button class="batch-btn batch-btn--primary" @click="handleBatchPay" :disabled="selectedIds.size === 0">批量缴纳</button>
        <button class="batch-btn batch-btn--secondary" @click="handleBatchWaive" :disabled="selectedIds.size === 0">批量豁免</button>
      </div>

      <!-- Table -->
      <div class="table">
        <div class="table-head">
          <span class="th th--checkbox" @click="toggleSelectAll">
            <span class="checkbox">{{ selectedIds.size === fines.length && fines.length > 0 ? '☑' : '☐' }}</span>
          </span>
          <span class="th" style="width:110px">读者证号</span>
          <span class="th" style="width:100px">读者姓名</span>
          <span class="th" style="width:210px">书名</span>
          <span class="th" style="width:100px">罚款类型</span>
          <span class="th" style="width:80px">金额</span>
          <span class="th" style="width:80px">逾期天数</span>
          <span class="th" style="width:110px">生成时间</span>
          <span class="th-spacer"></span>
          <span class="th th--right" style="width:150px">操作</span>
        </div>

        <div v-if="loading" class="table-empty">加载中...</div>
        <div v-if="!loading && fines.length === 0" class="table-empty">暂无罚款记录</div>

        <div v-for="f in fines" :key="f.id" class="table-row">
          <div class="td td--checkbox">
            <span class="checkbox" @click="toggleSelect(f.id)">{{ selectedIds.has(f.id) ? '☑' : '☐' }}</span>
          </div>
          <span class="td td--mono" style="width:110px">{{ f.readerNo || '—' }}</span>
          <span class="td td--name" style="width:100px">{{ f.readerName || '—' }}</span>
          <span class="td td--name" style="width:210px">{{ f.bookTitle || '—' }}</span>
          <span class="td" style="width:100px">{{ f.fineType }}</span>
          <span class="td td--mono" style="width:80px" :style="{ color: fineAmountColor(f), fontWeight: 600 }">¥{{ f.amount.toFixed(2) }}</span>
          <span class="td" style="width:80px">{{ f.overdueDays ? f.overdueDays + ' 天' : '—' }}</span>
          <span class="td td--muted" style="width:110px">{{ f.createTime }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:150px">
            <template v-if="f.paid">
              <span class="fine-status fine-status--paid">已缴纳</span>
            </template>
            <template v-else-if="f.waive">
              <span class="fine-status fine-status--waived">已豁免</span>
            </template>
            <template v-else>
              <button class="btn-pay" @click="handlePay(f.id)">缴纳</button>
              <button class="btn-waive" @click="handleWaive(f.id)">豁免</button>
            </template>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination">
        <span class="page-info">显示 {{ fines.length }} 条，共 {{ total }} 条</span>
        <div class="page-buttons">
          <span class="page-prev" :class="{ 'page--disabled': currentPage <= 1 }" @click="goToPage(currentPage - 1)">←</span>
          <template v-for="p in visiblePages" :key="p">
            <div v-if="typeof p === 'number'" :class="['page-num', { 'page-num--active': p === currentPage }]" @click="goToPage(p)">{{ p }}</div>
            <span v-else class="page-ellipsis">...</span>
          </template>
          <span class="page-next" :class="{ 'page--disabled': currentPage >= totalPages }" @click="goToPage(currentPage + 1)">→</span>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-fines { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary, #F7F8FA); }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }

.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.header__date { padding: 8px 14px; border-radius: 999px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); font-size: 12px; color: var(--text-secondary,#666); }

.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; align-items: center; gap: 6px; }
.stat-label { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); }
.stat-value { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 28px; font-weight: 700; }
.stat-value--danger { color: var(--danger,#F87171); }
.stat-value--accent { color: var(--accent,#4A9FD8); }
.stat-value--success { color: var(--success,#34D399); }
.stat-unit { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 400; margin-left: 2px; }

.toolbar { display: flex; gap: 12px; align-items: center; position: relative; }
.search-bar { display: flex; align-items: center; gap: 8px; width: 320px; padding: 4px 6px 4px 20px; border-radius: 999px; background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); }
.search-icon { font-size: 14px; color: var(--text-muted,#888); }
.search-input { flex: 1; height: 36px; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.search-input::placeholder { color: var(--text-muted,#888); }
.search-btn { padding: 10px 20px; border-radius: 999px; background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; border: none; cursor: pointer; white-space: nowrap; }
.filter-dropdown { position: relative; display: flex; align-items: center; gap: 24px; padding: 10px 14px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; user-select: none; }
.filter-arrow { font-size: 10px; color: var(--text-muted,#888); }
.dropdown-menu { position: absolute; top: calc(100% + 4px); left: 0; width: 160px; background: var(--bg-primary,#FFF); border-radius: 12px; border: 1px solid var(--border,#E5E7EB); box-shadow: 0 4px 16px rgba(0,0,0,0.08); z-index: 100; padding: 4px; }
.dropdown-item { padding: 8px 14px; border-radius: 8px; font-size: 13px; color: var(--text-secondary,#666); cursor: pointer; }
.dropdown-item:hover { background: var(--bg-secondary,#F7F8FA); }
.dropdown-item--active { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); font-weight: 500; }

.batch-bar { display: flex; align-items: center; gap: 12px; }
.batch-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-secondary,#666); }
.batch-btn { padding: 8px 16px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; }
.batch-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.batch-btn--primary { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); }
.batch-btn--secondary { background: transparent; color: var(--text-secondary,#666); border: 1px solid var(--border,#E5E7EB); }

.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); align-items: center; }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; padding: 12px 20px; align-items: center; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); flex-shrink: 0; display: flex; align-items: center; }
.td--name { font-weight: 500; color: var(--text-primary,#1A1A1A); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }

.th--checkbox, .td--checkbox { width: 32px; flex-shrink: 0; }
.checkbox { font-size: 14px; cursor: pointer; user-select: none; }

.btn-pay { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); transition: opacity 0.15s; }
.btn-pay:hover { opacity: 0.85; }
.btn-waive { padding: 5px 10px; border-radius: 6px; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; background: transparent; color: var(--text-secondary,#666); border: 1px solid var(--border,#E5E7EB); transition: background 0.15s; }
.btn-waive:hover { background: var(--bg-secondary,#F7F8FA); }

.fine-status { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; }
.fine-status--paid { color: var(--success,#34D399); }
.fine-status--waived { color: var(--text-muted,#888); }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--accent,#4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted,#888); cursor: default; pointer-events: none; }
.page-num { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); cursor: pointer; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); }
.page-num--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; border-color: var(--accent,#4A9FD8); }
.page-ellipsis { font-size: 12px; color: var(--text-muted,#888); padding: 0 4px; }
</style>

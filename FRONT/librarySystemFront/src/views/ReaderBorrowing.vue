<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentBorrowing, getBorrowSummary, renewBook } from '../api/borrow'

const router = useRouter()
const loading = ref(true)
const keyword = ref('')
const currentBorrowings = ref<any[]>([])
const summary = ref({ currentBorrowed: 0, overdueCount: 0 })

onMounted(async () => {
  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    const [list, sum] = await Promise.all([
      getCurrentBorrowing(readerNo).catch(() => []),
      getBorrowSummary(readerNo).catch(() => ({ currentBorrowed: 0, overdueCount: 0 })),
    ])
    currentBorrowings.value = list.length > 0 ? list : getDemoBorrowings()
    summary.value = sum
  } catch {
    currentBorrowings.value = getDemoBorrowings()
  } finally {
    loading.value = false
  }
})

function getDemoBorrowings() {
  return [
    { id: 1, bookTitle: '三体', bookAuthor: '刘慈欣', barcode: '9787536692930-001', borrowDate: '2026-07-01', dueDate: '2026-07-15', remainingDays: 0, overdue: true, overdueDays: 19, canRenew: false },
    { id: 2, bookTitle: '百年孤独', bookAuthor: '加西亚·马尔克斯', barcode: '9787544253994-001', borrowDate: '2026-07-01', dueDate: '2026-07-31', remainingDays: 12, overdue: false, overdueDays: 0, canRenew: true },
    { id: 3, bookTitle: '人类简史', bookAuthor: '尤瓦尔·赫拉利', barcode: '9787508660752-001', borrowDate: '2026-07-06', dueDate: '2026-08-05', remainingDays: 28, overdue: false, overdueDays: 0, canRenew: true },
  ]
}

const borrowedCount = computed(() => currentBorrowings.value.length)
const overdueCount = computed(() => currentBorrowings.value.filter(b => b.overdue).length)
const returnedMonth = computed(() => 5) // placeholder

const filtered = computed(() => {
  if (!keyword.value) return currentBorrowings.value
  const kw = keyword.value.toLowerCase()
  return currentBorrowings.value.filter(b =>
    b.bookTitle?.toLowerCase().includes(kw) ||
    b.barcode?.toLowerCase().includes(kw)
  )
})

function goToBookDetail(bookInfoId: number) {
  if (bookInfoId) router.push('/books/' + bookInfoId)
}

async function handleRenew(recordId: number, event: Event) {
  event.stopPropagation()
  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    const result = await renewBook(recordId, readerNo)
    alert(`续借成功！原应还：${result.oldDueDate} → 新应还：${result.newDueDate}`)
    // Refresh list
    const list = await getCurrentBorrowing(readerNo).catch(() => [])
    currentBorrowings.value = list.length > 0 ? list : getDemoBorrowings()
  } catch (err: any) {
    alert(err.message || '续借失败')
  }
}
</script>

<template>
  <div class="borrowing-page">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">当前借阅</h1>
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input v-model="keyword" class="search-input" placeholder="搜索图书..." @keyup.enter="() => {}" />
        <button class="search-btn">搜索</button>
      </div>
    </div>

    <div v-if="loading" class="loading-msg">加载中...</div>

    <template v-if="!loading">
      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-label">当前在借</span>
          <span class="stat-value stat-value--accent">{{ borrowedCount }}<span class="stat-unit">本</span></span>
        </div>
        <div class="stat-card">
          <span class="stat-label">逾期</span>
          <span class="stat-value stat-value--danger">{{ overdueCount }}<span class="stat-unit">本</span></span>
        </div>
        <div class="stat-card">
          <span class="stat-label">已还本月</span>
          <span class="stat-value stat-value--success">{{ returnedMonth }}<span class="stat-unit">本</span></span>
        </div>
      </div>

      <!-- Table -->
      <div class="table">
        <div class="table-head">
          <span class="th" style="width:200px">书名</span>
          <span class="th" style="width:160px">条码号</span>
          <span class="th" style="width:120px">借出日期</span>
          <span class="th" style="width:120px">应还日期</span>
          <span class="th" style="width:100px">状态</span>
          <span class="th" style="width:80px">操作</span>
        </div>

        <div v-if="currentBorrowings.length === 0" class="table-empty">暂无借阅记录</div>
        <div v-for="book in filtered" :key="book.id" class="table-row" @click="goToBookDetail(book.bookInfoId)">
          <span class="td td--title" style="width:200px">{{ book.bookTitle }}</span>
          <span class="td td--mono" style="width:160px">{{ book.barcode || '—' }}</span>
          <span class="td" style="width:120px">{{ book.borrowDate || '—' }}</span>
          <span :class="['td', { 'td--overdue': book.overdue }]" style="width:120px">
            {{ book.dueDate || '—' }}
          </span>
          <div class="td" style="width:100px">
            <span :class="['status-badge', book.overdue ? 'status-badge--danger' : 'status-badge--success']">
              {{ book.overdue ? `逾期${book.overdueDays}天` : `剩余${book.remainingDays}天` }}
            </span>
          </div>
          <div class="td" style="width:80px">
            <span v-if="book.canRenew" class="action-link" @click.stop="handleRenew(book.id, $event)">续借</span>
            <span v-else class="action-link action-link--disabled">—</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.borrowing-page { display: flex; flex-direction: column; gap: 28px; padding: 8px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.search-bar { display: flex; align-items: center; gap: 8px; padding: 4px 6px 4px 20px; border-radius: 999px; background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); }
.search-icon { font-size: 14px; color: var(--text-muted,#888); }
.search-input { width: 160px; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.search-input::placeholder { color: var(--text-muted,#888); }
.search-btn { padding: 10px 20px; border-radius: 999px; background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; border: none; cursor: pointer; }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
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
.table-row { display: flex; padding: 12px 20px; align-items: center; border-top: 0.5px solid var(--border,#E5E7EB); cursor: pointer; transition: background 0.1s; }
.table-row:hover { background: var(--accent-light,#E8F4FD); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); flex-shrink: 0; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--overdue { color: var(--danger,#F87171); font-weight: 600; }
.status-badge { display: inline-block; padding: 4px 10px; border-radius: 999px; font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; }
.status-badge--success { background: rgba(52,211,153,0.12); color: var(--success,#34D399); }
.status-badge--danger { background: rgba(248,113,113,0.12); color: var(--danger,#F87171); }
.action-link { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--accent,#4A9FD8); cursor: pointer; }
.action-link--disabled { color: var(--text-muted,#888); cursor: default; }
</style>

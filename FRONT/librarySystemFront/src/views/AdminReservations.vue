<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { listAdminReservations, pickupReservation } from '../api/borrow'

const loading = ref(true)
const reservations = ref<any[]>([])
const activeFilter = ref('')

const statusFilters = [
  { value: '', label: '全部' },
  { value: 'waiting', label: '等待中' },
  { value: 'ready', label: '待取书' },
  { value: 'fulfilled', label: '已完成' },
  { value: 'cancelled', label: '已取消' },
  { value: 'expired', label: '已过期' },
]

onMounted(loadReservations)

async function loadReservations() {
  loading.value = true
  try {
    reservations.value = await listAdminReservations(activeFilter.value || undefined)
  } catch {
    reservations.value = []
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  if (!activeFilter.value) return reservations.value
  return reservations.value.filter(r => r.status === activeFilter.value)
})

async function handlePickup(id: number) {
  if (!confirm('确认取书？系统将自动创建借阅记录。')) return
  try {
    await pickupReservation(id, 1)
    loadReservations()
  } catch (err: any) {
    alert(err.message || '取书失败')
  }
}

function statusColor(status: string): string {
  const colors: Record<string, string> = {
    waiting: 'var(--accent,#4A9FD8)',
    ready: 'var(--success,#34D399)',
    fulfilled: 'var(--text-muted,#888)',
    cancelled: 'var(--danger,#F87171)',
    expired: 'var(--text-muted,#888)',
  }
  return colors[status] || 'var(--text-muted,#888)'
}

function statusLabel(status: string): string {
  const labels: Record<string, string> = {
    waiting: '等待中', ready: '待取书', fulfilled: '已完成',
    cancelled: '已取消', expired: '已过期',
  }
  return labels[status] || status
}
</script>

<template>
  <div class="admin-reservations">
    <main class="main">
      <header class="header">
        <h1 class="header__title">预约管理</h1>
      </header>

      <div class="toolbar">
        <div class="filter-tabs">
          <div v-for="f in statusFilters" :key="f.value"
            :class="['filter-tab', { 'filter-tab--active': activeFilter === f.value }]"
            @click="activeFilter = f.value; loadReservations()">
            {{ f.label }}
          </div>
        </div>
      </div>

      <div class="table">
        <div class="table-head">
          <span class="th" style="width:60px">ID</span>
          <span class="th" style="width:130px">读者证号</span>
          <span class="th" style="width:110px">读者姓名</span>
          <span class="th" style="width:210px">书名</span>
          <span class="th" style="width:150px">预约时间</span>
          <span class="th" style="width:100px">状态</span>
          <span class="th" style="width:120px">取书地点</span>
          <span class="th-spacer"></span>
          <span class="th th--right" style="width:120px">操作</span>
        </div>

        <div v-if="loading" class="table-empty">加载中...</div>
        <div v-if="!loading && filtered.length === 0" class="table-empty">暂无预约记录</div>

        <div v-for="r in filtered" :key="r.id" class="table-row">
          <span class="td td--mono td--muted" style="width:60px">{{ r.id }}</span>
          <span class="td td--mono" style="width:130px">{{ r.readerNo || '—' }}</span>
          <span class="td td--name" style="width:110px">{{ r.readerName || '—' }}</span>
          <span class="td td--name" style="width:210px">{{ r.bookTitle || '—' }}</span>
          <span class="td td--muted" style="width:150px">{{ r.reserveDate?.slice(0, 16) || '—' }}</span>
          <div class="td" style="width:100px">
            <span class="status-tag" :style="{ background: statusColor(r.status) + '1A', color: statusColor(r.status) }">{{ statusLabel(r.status) }}</span>
          </div>
          <span class="td td--muted" style="width:120px">{{ r.pickLocationName || '—' }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:120px">
            <button v-if="r.status === 'ready'" class="btn-pickup" @click="handlePickup(r.id)">取书确认</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-reservations { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary,#F7F8FA); }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.toolbar { display: flex; gap: 12px; align-items: center; }
.filter-tabs { display: flex; gap: 4px; background: var(--bg-primary,#FFF); border-radius: 10px; border: 1px solid var(--border,#E5E7EB); padding: 3px; }
.filter-tab { padding: 6px 14px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; transition: all 0.15s; }
.filter-tab--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }

.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); align-items: center; }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; padding: 12px 20px; align-items: center; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); flex-shrink: 0; }
.td--name { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.status-tag { display: inline-block; padding: 3px 10px; border-radius: 999px; font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; }
.btn-pickup { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 500; background: var(--success,#34D399); color: var(--text-inverse,#FFF); transition: opacity 0.15s; }
.btn-pickup:hover { opacity: 0.85; }
</style>

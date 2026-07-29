<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCurrentReservations } from '../api/borrow'

const loading = ref(true)
const activeTab = ref('进行中')
const reservations = ref<any[]>([])

const tabs = ['进行中', '待取书', '历史记录']

onMounted(async () => {
  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    reservations.value = await getCurrentReservations(readerNo)
  } catch {
    reservations.value = getDemoReservations()
  } finally {
    loading.value = false
  }
})

function getDemoReservations() {
  return [
    { bookTitle: '深入理解Java虚拟机', author: '周志明', reserveDate: '2026-07-20', status: '等待中', queuePosition: 1 },
    { bookTitle: '算法导论', author: 'Thomas H.Cormen', reserveDate: '2026-07-25', status: '等待中', queuePosition: 2 },
  ]
}

function statusColor(status: string): { bg: string; text: string } {
  switch (status) {
    case '等待中': return { bg: 'var(--accent-light,#E8F4FD)', text: 'var(--accent,#4A9FD8)' }
    case '待取书': return { bg: 'rgba(52,211,153,0.12)', text: 'var(--success,#34D399)' }
    case '已完成': return { bg: 'var(--bg-secondary,#F7F8FA)', text: 'var(--text-muted,#888)' }
    case '已取消': return { bg: 'rgba(248,113,113,0.12)', text: 'var(--danger,#F87171)' }
    case '已过期': return { bg: 'var(--bg-secondary,#F7F8FA)', text: 'var(--text-muted,#888)' }
    default: return { bg: 'var(--bg-secondary,#F7F8FA)', text: 'var(--text-muted,#888)' }
  }
}
</script>

<template>
  <div class="reservations-page">
    <div class="page-header">
      <h1 class="page-title">我的预约</h1>
      <span class="page-hint">最大同时预约 3 本书</span>
    </div>

    <div v-if="loading" class="loading-msg">加载中...</div>

    <template v-if="!loading">
      <!-- Tabs -->
      <div class="tabs-bar">
        <div v-for="tab in tabs" :key="tab"
          :class="['tab', { 'tab--active': activeTab === tab }]"
          @click="activeTab = tab">
          {{ tab }}
        </div>
      </div>

      <!-- Reservation Cards -->
      <div v-if="reservations.length === 0" class="empty-state">暂无预约记录</div>
      <div v-for="item in reservations" :key="item.bookTitle" class="reserve-card">
        <div class="reserve-info">
          <h3 class="reserve-title">{{ item.bookTitle }}</h3>
          <p class="reserve-author">{{ item.author }}</p>
          <p class="reserve-date">预约时间：{{ item.reserveDate }}</p>
        </div>
        <div class="reserve-actions">
          <span class="reserve-badge" :style="{ background: statusColor(item.status).bg, color: statusColor(item.status).text }">
            {{ item.status }}
          </span>
          <span class="reserve-cancel">取消预约</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.reservations-page { display: flex; flex-direction: column; gap: 28px; padding: 8px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.page-hint { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
.empty-state { padding: 48px; text-align: center; color: var(--text-muted,#888); font-size: 13px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }

.tabs-bar { display: flex; gap: 4px; background: var(--bg-primary,#FFF); border-radius: 10px; border: 1px solid var(--border,#E5E7EB); padding: 3px; width: fit-content; }
.tab { padding: 6px 16px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; transition: all 0.15s; }
.tab--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }

.reserve-card { display: flex; align-items: center; gap: 20px; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }
.reserve-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.reserve-title { font-family: var(--font-sans,Inter); font-size: 16px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.reserve-author { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-secondary,#666); margin: 0; }
.reserve-date { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); margin: 0; }
.reserve-actions { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.reserve-badge { padding: 6px 14px; border-radius: 999px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; }
.reserve-cancel { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--danger,#F87171); cursor: pointer; }
.reserve-cancel:hover { opacity: 0.8; }
</style>

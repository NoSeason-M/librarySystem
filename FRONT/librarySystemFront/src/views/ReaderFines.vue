<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getReaderFines } from '../api/fines'

const loading = ref(true)
const activeTab = ref('未缴罚款')
const fines = ref<any[]>([])

const tabs = ['未缴罚款', '已缴记录']

onMounted(async () => {
  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    fines.value = await getReaderFines(readerNo)
  } catch {
    fines.value = getDemoFines()
  } finally {
    loading.value = false
  }
})

function getDemoFines() {
  return [
    { bookTitle: '三体', fineType: '逾期罚款', overdueDays: 9, amount: 4.50, createTime: '2026-07-17', paid: false },
    { bookTitle: '深入理解Java虚拟机', fineType: '逾期罚款', overdueDays: 14, amount: 7.00, createTime: '2026-07-14', paid: false },
  ]
}

const totalAmount = computed(() => {
  return fines.value
    .filter(f => !f.paid)
    .reduce((sum, f) => sum + f.amount, 0)
    .toFixed(2)
})

function payFine(_id: number) {
  // TODO: POST /fines/{id}/pay
}
</script>

<template>
  <div class="fines-page">
    <div class="page-header">
      <h1 class="page-title">我的罚款</h1>
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

      <div v-if="fines.length === 0" class="empty-state">暂无罚款记录</div>

      <!-- Fine Cards -->
      <div v-for="item in fines.filter(f => activeTab === '未缴罚款' ? !f.paid : f.paid)" :key="item.bookTitle" class="fine-card">
        <div class="fine-info">
          <h3 class="fine-title">{{ item.bookTitle }}</h3>
          <p class="fine-detail">{{ item.fineType }} · 逾期 {{ item.overdueDays }} 天</p>
          <p class="fine-date">生成时间：{{ item.createTime }}</p>
        </div>
        <div class="fine-amount">¥{{ item.amount.toFixed(2) }}</div>
        <button v-if="!item.paid" class="fine-pay-btn" @click="payFine(item.id)">立即缴纳</button>
        <span v-else class="fine-paid">已缴纳</span>
      </div>

      <!-- Total -->
      <div v-if="activeTab === '未缴罚款' && fines.filter(f => !f.paid).length > 0" class="total-row">
        <span class="total-label">合计：</span>
        <span class="total-amount">¥{{ totalAmount }}</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.fines-page { display: flex; flex-direction: column; gap: 28px; padding: 8px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
.empty-state { padding: 48px; text-align: center; color: var(--text-muted,#888); font-size: 13px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }

.tabs-bar { display: flex; gap: 4px; background: var(--bg-primary,#FFF); border-radius: 10px; border: 1px solid var(--border,#E5E7EB); padding: 3px; width: fit-content; }
.tab { padding: 6px 16px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--text-secondary,#666); cursor: pointer; transition: all 0.15s; }
.tab--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; }

.fine-card { display: flex; align-items: center; gap: 20px; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }
.fine-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.fine-title { font-family: var(--font-sans,Inter); font-size: 16px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.fine-detail { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-secondary,#666); margin: 0; }
.fine-date { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); margin: 0; }
.fine-amount { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 22px; font-weight: 700; color: var(--danger,#F87171); white-space: nowrap; }
.fine-pay-btn { padding: 10px 20px; border-radius: var(--button-radius,10px); background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; border: none; cursor: pointer; white-space: nowrap; flex-shrink: 0; }
.fine-pay-btn:hover { opacity: 0.9; }
.fine-paid { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--success,#34D399); }

.total-row { display: flex; justify-content: flex-end; align-items: center; gap: 16px; padding: 8px 0; }
.total-label { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; color: var(--text-secondary,#666); }
.total-amount { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 20px; font-weight: 700; color: var(--danger,#F87171); }
</style>

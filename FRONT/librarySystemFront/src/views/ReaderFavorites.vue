<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, removeFavorite } from '../api/favorites'
import { getMyProfile } from '../api/readers'

const router = useRouter()
const loading = ref(true)
const favorites = ref<any[]>([])
const errorMsg = ref('')

const rows = computed(() => {
  const result: any[][] = []
  for (let i = 0; i < favorites.value.length; i += 2) {
    result.push(favorites.value.slice(i, i + 2))
  }
  return result
})

onMounted(async () => {
  let readerNo = localStorage.getItem('readerNo') || ''
  if (!readerNo) {
    try {
      const profile = await getMyProfile()
      readerNo = profile.readerNo || 'RD20260001'
      localStorage.setItem('readerNo', readerNo)
    } catch {
      readerNo = 'RD20260001'
    }
  }
  try {
    const result = await getFavorites(readerNo)
    favorites.value = result.records || []
  } catch (e: any) {
    errorMsg.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function goToDetail(bookInfoId: number) {
  if (bookInfoId) router.push(`/books/${bookInfoId}`)
}

async function removeFav(rowIdx: number, colIdx: number, item: any) {
  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    await removeFavorite(item.bookInfoId, readerNo)
    favorites.value.splice(rowIdx * 2 + colIdx, 1)
  } catch {
    alert('取消收藏失败')
  }
}
</script>

<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <span class="page-count">共 {{ favorites.length }} 本收藏</span>
    </div>

    <div v-if="loading" class="loading-msg">加载中...</div>
    <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

    <template v-if="!loading && !errorMsg">
      <div v-if="favorites.length === 0" class="empty-state">暂无收藏</div>

      <div v-for="(row, ri) in rows" :key="ri" class="grid-row">
        <div v-for="(item, ci) in row" :key="item.id"
          class="fav-card" @click="goToDetail(item.bookInfoId)">
          <div class="fav-cover">📖</div>
          <div class="fav-info">
            <h3 class="fav-title">{{ item.title }}</h3>
            <p class="fav-author">{{ item.author }}</p>
          </div>
          <div class="fav-actions">
            <span class="fav-remove" @click.stop="removeFav(ri, ci, item)">取消收藏</span>
          </div>
        </div>
        <!-- Empty filler for single item row -->
        <div v-if="row.length === 1" class="fav-card fav-card--empty"></div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.page-count { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }
.empty-state { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); }
.error-msg { padding: 12px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }

.grid-row { display: flex; gap: 16px; }
.fav-card { flex: 1; display: flex; flex-direction: column; gap: 10px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); overflow: hidden; cursor: pointer; transition: box-shadow 0.15s, transform 0.15s; }
.fav-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); transform: translateY(-1px); }
.fav-card--empty { visibility: hidden; }

.fav-cover { height: 140px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 36px; }
.fav-info { padding: 0 14px; display: flex; flex-direction: column; gap: 4px; }
.fav-title { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fav-author { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fav-actions { padding: 0 14px 14px; display: flex; justify-content: flex-end; }
.fav-remove { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 500; color: var(--danger,#F87171); cursor: pointer; }
.fav-remove:hover { opacity: 0.8; }
</style>

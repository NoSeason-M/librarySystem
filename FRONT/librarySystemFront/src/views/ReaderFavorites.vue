<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites } from '../api/favorites'

const router = useRouter()
const loading = ref(true)
const favorites = ref<any[]>([])

onMounted(async () => {
  const readerNo = localStorage.getItem('readerNo') || 'RD20260001'
  try {
    const result = await getFavorites(readerNo)
    favorites.value = result.records || []
  } catch {
    favorites.value = getDemoFavorites()
  } finally {
    loading.value = false
  }
})

function getDemoFavorites() {
  return [
    { id: 1, bookInfoId: 1, title: '三体', author: '刘慈欣' },
    { id: 2, bookInfoId: 2, title: '百年孤独', author: '加西亚·马尔克斯' },
    { id: 3, bookInfoId: 6, title: '围城', author: '钱钟书' },
    { id: 4, bookInfoId: 5, title: '人类简史', author: '尤瓦尔·赫拉利' },
  ]
}

function goToDetail(bookInfoId: number) {
  if (bookInfoId) router.push(`/books/${bookInfoId}`)
}

function removeFav(index: number) {
  favorites.value.splice(index, 1)
}
</script>

<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <span class="page-count">共 {{ favorites.length }} 本收藏</span>
    </div>

    <div v-if="loading" class="loading-msg">加载中...</div>

    <template v-if="!loading">
      <div v-if="favorites.length === 0" class="empty-state">暂无收藏</div>

      <div v-for="ri in Math.ceil(favorites.length / 2)" :key="ri" class="grid-row">
        <div v-for="(item, ci) in favorites.slice(ri * 2, ri * 2 + 2)" :key="item.id"
          class="fav-card" @click="goToDetail(item.bookInfoId)">
          <div class="fav-cover">📖</div>
          <div class="fav-info">
            <h3 class="fav-title">{{ item.title }}</h3>
            <p class="fav-author">{{ item.author }}</p>
          </div>
          <div class="fav-actions">
            <span class="fav-remove" @click.stop="removeFav(ri * 2 + ci)">取消收藏</span>
          </div>
        </div>
        <!-- Fill empty slot -->
        <div v-if="favorites.length % 2 !== 0 && ri === Math.ceil(favorites.length / 2) - 1" class="fav-card fav-card--empty"></div>
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

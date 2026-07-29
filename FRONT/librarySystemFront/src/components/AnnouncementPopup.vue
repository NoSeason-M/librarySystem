<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/index'

const announcements = ref<any[]>([])
const currentIndex = ref(0)
const showModal = ref(false)

onMounted(async () => {
  // Get current user roles
  let roles: string[] = []
  try { roles = JSON.parse(localStorage.getItem('roles') || '[]') } catch { return }
  if (roles.length === 0) return

  const isAdmin = roles.some(r => ['ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER'].includes(r))
  const isReader = roles.includes('ROLE_READER')

  // Load published announcements
  try {
    const list: any[] = await http.get('/announcements', { params: { page: 1, size: 10 } })
    // Filter by targetRoles
    const filtered = list.filter((a: any) => {
      if (a.targetRoles === 'all') return true
      if (a.targetRoles === 'reader' && isReader) return true
      if (a.targetRoles === 'admin' && isAdmin) return true
      return false
    })
    if (filtered.length === 0) return

    // Remove already-seen ones
    const seenIds = JSON.parse(localStorage.getItem('seenAnnouncements') || '[]')
    const unseen = filtered.filter((a: any) => !seenIds.includes(a.id))
    announcements.value = unseen.length > 0 ? unseen : filtered
    showModal.value = true
  } catch { /* ignore */ }
})

function closeAnnouncement() {
  if (announcements.value[currentIndex.value]) {
    const seenIds = JSON.parse(localStorage.getItem('seenAnnouncements') || '[]')
    seenIds.push(announcements.value[currentIndex.value].id)
    localStorage.setItem('seenAnnouncements', JSON.stringify(seenIds))
  }
  showModal.value = false
}

function nextAnnouncement() {
  const seenIds = JSON.parse(localStorage.getItem('seenAnnouncements') || '[]')
  seenIds.push(announcements.value[currentIndex.value].id)
  localStorage.setItem('seenAnnouncements', JSON.stringify(seenIds))
  if (currentIndex.value < announcements.value.length - 1) {
    currentIndex.value++
  } else {
    showModal.value = false
    currentIndex.value = 0
  }
}
</script>

<template>
  <div v-if="showModal && announcements.length > 0" class="modal-overlay" @click.self="closeAnnouncement">
    <div class="modal">
      <div class="modal__header">
        <span class="modal__icon">📢</span>
        <span class="modal__title">系统公告</span>
        <button class="modal__close" @click="closeAnnouncement">✕</button>
      </div>
      <div class="modal__body">
        <h2 class="modal__heading">{{ announcements[currentIndex]?.title }}</h2>
        <p class="modal__time" v-if="announcements[currentIndex]?.publishTime">
          {{ announcements[currentIndex].publishTime.slice(0, 10) }}
        </p>
        <div class="modal__content">{{ announcements[currentIndex]?.content }}</div>
      </div>
      <div class="modal__footer">
        <span v-if="announcements.length > 1" class="modal__counter">
          {{ currentIndex + 1 }} / {{ announcements.length }}
        </span>
        <div class="modal__footer-right">
          <button class="btn-close" @click="closeAnnouncement">关闭</button>
          <button class="btn-next" @click="nextAnnouncement">
            {{ currentIndex < announcements.length - 1 ? '下一条' : '我知道了' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 3000; padding: 24px;
}
.modal {
  width: 100%; max-width: 520px;
  background: var(--bg-primary,#FFF);
  border-radius: var(--card-radius,16px);
  display: flex; flex-direction: column;
  border: 1px solid var(--border,#E5E7EB);
  overflow: hidden;
}
.modal__header {
  display: flex; align-items: center; gap: 10px;
  padding: 20px 24px 0;
}
.modal__icon { font-size: 24px; line-height: 1; }
.modal__title {
  font-family: var(--font-sans,Inter);
  font-size: 16px; font-weight: 600;
  color: var(--text-primary,#1A1A1A);
  flex: 1;
}
.modal__close {
  width: 32px; height: 32px; border-radius: 8px;
  background: var(--bg-secondary,#F7F8FA); border: none;
  font-size: 14px; color: var(--text-muted,#888);
  cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.modal__close:hover { background: var(--border,#E5E7EB); }
.modal__body {
  padding: 16px 24px;
  display: flex; flex-direction: column; gap: 8px;
}
.modal__heading {
  font-family: var(--font-sans,Inter);
  font-size: 20px; font-weight: 700;
  color: var(--text-primary,#1A1A1A);
  margin: 0; line-height: 1.3;
}
.modal__time {
  font-family: var(--font-sans,Inter);
  font-size: 12px; color: var(--text-muted,#888);
  margin: 0;
}
.modal__content {
  font-family: var(--font-sans,Inter);
  font-size: 14px; line-height: 1.7;
  color: var(--text-secondary,#666);
  white-space: pre-wrap;
  max-height: 300px; overflow-y: auto;
}
.modal__footer {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 24px 20px;
}
.modal__counter {
  font-family: var(--font-sans,Inter);
  font-size: 12px; color: var(--text-muted,#888);
}
.modal__footer-right { display: flex; gap: 10px; }
.btn-close {
  padding: 10px 20px; border-radius: var(--button-radius,10px);
  background: var(--bg-primary,#FFF); color: var(--text-secondary,#666);
  font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500;
  border: 1.5px solid var(--border,#E5E7EB); cursor: pointer;
}
.btn-close:hover { background: var(--bg-secondary,#F7F8FA); }
.btn-next {
  padding: 10px 24px; border-radius: var(--button-radius,10px);
  background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF);
  font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600;
  border: none; cursor: pointer;
}
.btn-next:hover { opacity: 0.9; }
</style>

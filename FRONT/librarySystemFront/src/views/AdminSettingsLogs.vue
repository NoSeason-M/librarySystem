<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listLogs } from '../api/system'

const router = useRouter()
const adminNav = [
  { icon: '📊', label: 'Dashboard' }, { icon: '📖', label: 'Borrow/Return' }, { icon: '📚', label: 'Books' },
  { icon: '👥', label: 'Readers' }, { icon: '📈', label: 'Statistics' }, { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings' },
]
function navigateTo(l: string) {
  if (l === 'Dashboard') router.push('/admin'); else if (l === 'Books') router.push('/admin/books')
  else if (l === 'Readers') router.push('/admin/readers'); else if (l === 'Statistics') router.push('/admin/statistics')
  else if (l === 'Settings') router.push('/admin/settings')
}
function goBack() { router.push('/admin/settings') }

const keyword = ref('')
const moduleFilter = ref('')
const logs = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const loading = ref(false)
const modules = ['', 'auth', 'book', 'borrow', 'reader', 'system']

const visiblePages = computed(() => { const p: (number|string)[] = []; if (totalPages.value <= 5) { for(let i=1;i<=totalPages.value;i++) p.push(i) } else { p.push(1); const s=Math.max(2,currentPage.value-1);const e=Math.min(totalPages.value-1,currentPage.value+1); if(s>2) p.push('...'); for(let i=s;i<=e;i++) p.push(i); if(e<totalPages.value-1) p.push('...'); p.push(totalPages.value) } return p })

async function loadLogs() {
  loading.value = true
  try {
    const r = await listLogs(keyword.value || undefined, moduleFilter.value || undefined, currentPage.value, 15)
    logs.value = r.records; total.value = r.total; totalPages.value = r.pages || 1
  } catch { logs.value = [] } finally { loading.value = false }
}
function onSearch() { currentPage.value = 1; loadLogs() }

onMounted(() => { loadLogs() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header"><div class="header-left"><button class="btn-back" @click="goBack">←</button><h1 class="header__title">Operation Logs</h1></div></header>
      <div class="toolbar">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="keyword" placeholder="Search username..." @keyup.enter="onSearch" /></div>
        <select v-model="moduleFilter" class="filter-select" @change="onSearch">
          <option value="">All Modules</option>
          <option v-for="m in modules.filter(Boolean)" :key="m" :value="m">{{ m }}</option>
        </select>
      </div>
      <div class="table">
        <div class="table-head"><span class="th" style="width:80px">ID</span><span class="th" style="width:100px">Username</span><span class="th" style="width:200px">Operation</span><span class="th" style="width:80px">Module</span><span class="th" style="width:100px">IP</span><span class="th" style="width:60px">Method</span><span class="th" style="width:60px">Result</span><span class="th" style="width:50px">ms</span><span class="th" style="width:160px">Time</span></div>
        <div v-if="loading" class="table-empty">Loading...</div>
        <div v-if="!loading && logs.length === 0" class="table-empty">No logs found</div>
        <div v-for="log in logs" :key="log.id" class="table-row">
          <span class="td td--mono td--muted" style="width:80px">{{ log.id }}</span>
          <span class="td td--title" style="width:100px">{{ log.username }}</span>
          <span class="td td--secondary" style="width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ log.operation }}</span>
          <span class="td td--mono td--muted" style="width:80px">{{ log.module }}</span>
          <span class="td td--mono td--muted" style="width:100px">{{ log.requestIp || '—' }}</span>
          <span class="td td--mono td--muted" style="width:60px">{{ log.requestMethod || '—' }}</span>
          <div class="td" style="width:60px"><span class="status-badge" :style="{background:log.result==='success'?'var(--success,#34D399)':'var(--danger,#F87171)'}">{{ log.result }}</span></div>
          <span class="td td--mono td--muted" style="width:50px;text-align:right">{{ log.duration || '—' }}</span>
          <span class="td td--muted" style="width:160px;font-size:11px">{{ log.createTime ? log.createTime.slice(0,19) : '—' }}</span>
        </div>
      </div>
      <div class="pagination">
        <span class="page-info">Showing {{ logs.length }} of {{ total }} results</span>
        <div class="page-buttons">
          <span :class="['page-prev',{'page--disabled':currentPage<=1}]" @click="currentPage>1&&(currentPage--,loadLogs())">←</span>
          <template v-for="p in visiblePages" :key="p"><div v-if="typeof p==='number'" :class="['page-num',{'page-num--active':p===currentPage}]" @click="currentPage=p;loadLogs()">{{ p }}</div><span v-else class="page-ellipsis">...</span></template>
          <span :class="['page-next',{'page--disabled':currentPage>=totalPages}]" @click="currentPage<totalPages&&(currentPage++,loadLogs())">→</span>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.settings-page { display: flex; min-height: 100vh; background: var(--bg-secondary,#F7F8FA); }
.sidebar { width: 240px; background: var(--bg-inverse,#0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse,#FFF); }
.sidebar__logo-text { font-family: var(--font-sans,Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-inverse,#FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent,#4A9FD8); flex-shrink: 0; }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.btn-back { width: 36px; height: 36px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; color: var(--text-secondary,#666); }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.toolbar { display: flex; gap: 12px; align-items: center; }
.search-box { display: flex; align-items: center; gap: 8px; width: 300px; padding: 10px 16px; border-radius: 12px; background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); }
.search-icon { font-size: 14px; color: var(--text-muted,#888); }
.search-box input { flex:1; background:transparent;border:none;outline:none;font-family:var(--font-sans,Inter);font-size:13px;color:var(--text-primary,#1A1A1A); }
.search-box input::placeholder { color:var(--text-muted,#888); }
.filter-select { padding: 10px 14px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-secondary,#666); outline: none; cursor: pointer; }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 1px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 12px 16px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 11px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.table-row { display: flex; align-items: center; padding: 10px 16px; min-height: 44px; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { flex-shrink: 0; font-family: var(--font-sans,Inter); font-size: 11px; display: flex; align-items: center; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.status-badge { padding: 2px 8px; border-radius: 999px; font-size: 10px; font-weight: 500; color: #FFF; }
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--accent,#4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted,#888); cursor: default; pointer-events: none; }
.page-num { width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); cursor: pointer; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); }
.page-num--active { background: var(--accent,#4A9FD8); color: #FFF; font-weight: 600; border-color: var(--accent,#4A9FD8); }
.page-ellipsis { font-size: 12px; color: var(--text-muted,#888); }
</style>

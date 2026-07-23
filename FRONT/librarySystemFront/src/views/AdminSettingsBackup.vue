<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listBackups, createBackup } from '../api/system'

const router = useRouter()

function goBack() { router.push('/admin/settings') }

const backups = ref<any[]>([])
const loading = ref(false)
const creating = ref(false)
const msg = ref('')

async function loadBackups() {
  loading.value = true
  try { backups.value = await listBackups() } catch { backups.value = [] } finally { loading.value = false }
}
async function handleCreate() {
  creating.value = true; msg.value = ''
  try {
    const r = await createBackup()
    msg.value = 'Backup created: ' + r.fileName + ' (' + r.fileSizeDisplay + ')'
    loadBackups()
  } catch (err: any) { msg.value = err.message || 'Backup failed' } finally { creating.value = false }
}

onMounted(() => { loadBackups() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header">
        <div class="header-left"><button class="btn-back" @click="goBack">&#8592;</button><h1 class="header__title">Data Backup</h1></div>
      </header>
      <div class="backup-actions">
        <button class="btn-primary" :disabled="creating" @click="handleCreate">
          <span v-if="creating" class="spinner"></span><span v-else>Backup Now</span>
        </button>
        <div v-if="msg" :class="['backup-msg', { 'backup-msg--err': msg.includes('fail') }]">{{ msg }}</div>
      </div>
      <div class="table">
        <div class="table-head"><span class="th" style="width:200px">File Name</span><span class="th" style="width:100px">Size</span><span class="th" style="width:100px">Type</span><span class="th" style="width:100px">Status</span><span class="th" style="width:160px">Created</span><span class="th-spacer"></span><span class="th th--right" style="width:120px">Actions</span></div>
        <div v-if="loading" class="table-empty">Loading...</div>
        <div v-if="!loading && backups.length === 0" class="table-empty">No backups yet</div>
        <div v-for="b in backups" :key="b.id" class="table-row">
          <span class="td td--mono td--title" style="width:200px">{{ b.fileName }}</span>
          <span class="td td--mono" style="width:100px">{{ b.fileSizeDisplay }}</span>
          <span class="td td--secondary" style="width:100px">{{ b.backupType }}</span>
          <div class="td" style="width:100px"><span class="status-badge" :style="{background: b.backupStatus === 1 ? 'var(--success,#34D399)' : 'var(--warning,#FBBF24)'}">{{ b.backupStatus === 1 ? 'Success' : 'Pending' }}</span></div>
          <span class="td td--muted" style="width:160px;font-size:11px">{{ b.createTime ? b.createTime.slice(0,19) : '-' }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:120px"><button class="btn-sm btn-sm--edit">Download</button></div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.settings-page { flex: 1; display: flex; flex-direction: column; }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.btn-back { width: 36px; height: 36px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; color: var(--text-secondary,#666); }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.btn-primary { display: flex; align-items: center; gap: 8px; padding: 10px 24px; border-radius: 10px; background: var(--accent,#4A9FD8); color: #FFF; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; }
.btn-primary:disabled { opacity: 0.6; }
.backup-actions { display: flex; align-items: center; gap: 16px; }
.backup-msg { padding: 10px 14px; border-radius: 10px; background: rgba(52,211,153,0.1); color: var(--success,#34D399); font-size: 13px; border: 1px solid var(--success,#34D399); }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; padding: 12px 20px; border-top: 0.5px solid var(--border,#E5E7EB); align-items: center; }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { flex-shrink: 0; font-family: var(--font-sans,Inter); font-size: 12px; display: flex; align-items: center; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 500; color: #FFF; }
.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse,#FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
</style>

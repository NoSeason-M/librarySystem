<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '../api/system'

const router = useRouter()

function goBack() { router.push('/admin/settings') }

const announcements = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const loading = ref(false)
const showModal = ref(false)
const modalMode = ref<'create'|'edit'>('create')
const editId = ref(0)
const form = ref({ title: '', content: '', type: 'general', targetRoles: 'all', topFlag: 0, status: 0 })
const saving = ref(false)
const formError = ref('')

const visiblePages = computed(() => { const p:(number|string)[]=[]; if(totalPages.value<=5){for(let i=1;i<=totalPages.value;i++) p.push(i)}else{p.push(1);const s=Math.max(2,currentPage.value-1);const e=Math.min(totalPages.value-1,currentPage.value+1);if(s>2)p.push('...');for(let i=s;i<=e;i++)p.push(i);if(e<totalPages.value-1)p.push('...');p.push(totalPages.value)}return p })

async function loadList() {
  loading.value = true
  try { const r = await listAnnouncements(currentPage.value, 10); announcements.value = r.records; total.value = r.total; totalPages.value = r.pages || 1 }
  catch { announcements.value = [] } finally { loading.value = false }
}
function openCreate() { modalMode.value = 'create'; editId.value = 0; form.value = { title: '', content: '', type: 'general', targetRoles: 'all', topFlag: 0, status: 0 }; formError.value = ''; showModal.value = true }
function openEdit(a: any) { modalMode.value = 'edit'; editId.value = a.id; form.value = { title: a.title, content: a.content, type: a.type, targetRoles: a.targetRoles, topFlag: a.topFlag || 0, status: a.status }; formError.value = ''; showModal.value = true }
async function submitForm() {
  if (!form.value.title.trim() || !form.value.content.trim()) { formError.value = '标题和内容不能为空'; return }
  saving.value = true; formError.value = ''
  try { if (modalMode.value === 'create') await createAnnouncement(form.value); else await updateAnnouncement(editId.value, form.value); showModal.value = false; loadList() }
  catch (err: any) { formError.value = err.message } finally { saving.value = false }
}
async function handleDelete(id: number) { if (!confirm('确认删除此公告？')) return; try { await deleteAnnouncement(id); loadList() } catch { alert('删除失败') } }
async function publishItem(a: any) { try { await updateAnnouncement(a.id, { status: 1 }); loadList() } catch { alert('发布失败') } }

function getStatusLabel(s: number) { return s === 1 ? '已发布' : s === 0 ? '草稿' : '已归档' }
function getStatusColor(s: number) { return s === 1 ? 'var(--success,#34D399)' : s === 0 ? 'var(--text-muted,#888)' : 'var(--danger,#F87171)' }
function getTypeLabel(t: string) { return t === 'urgent' ? '紧急' : t === 'notice' ? '通知' : '一般' }

onMounted(() => { loadList() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header">
        <div class="header-left"><button class="btn-back" @click="goBack">&#8592;</button><h1 class="header__title">公告管理</h1></div>
        <button class="btn-primary" @click="openCreate">+ 创建</button>
      </header>
      <div class="table">
        <div class="table-head"><span class="th" style="width:200px">标题</span><span class="th" style="width:70px">类型</span><span class="th" style="width:80px">范围</span><span class="th" style="width:50px">置顶</span><span class="th" style="width:90px">状态</span><span class="th" style="width:150px">发布时间</span><span class="th-spacer"></span><span class="th th--right" style="width:140px">操作</span></div>
        <div v-if="loading" class="table-empty">加载中...</div>
        <div v-if="!loading && announcements.length === 0" class="table-empty">暂无公告</div>
        <div v-for="a in announcements" :key="a.id" class="table-row">
          <span class="td td--title" style="width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ a.title }}</span>
          <span class="td td--secondary" style="width:70px">{{ getTypeLabel(a.type) }}</span>
          <span class="td td--secondary" style="width:80px">{{ a.targetRoles }}</span>
          <span class="td" style="width:50px">{{ a.topFlag ? '&#11088;' : '-' }}</span>
          <div class="td" style="width:90px"><span class="status-badge" :style="{background:getStatusColor(a.status)}">{{ getStatusLabel(a.status) }}</span></div>
          <span class="td td--muted" style="width:150px;font-size:11px">{{ a.publishTime || '-' }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:140px">
            <button v-if="a.status === 0" class="btn-sm btn-sm--edit" @click="publishItem(a)">发布</button>
            <button class="btn-sm btn-sm--edit" @click="openEdit(a)">编辑</button>
            <button class="btn-sm btn-sm--del" @click="handleDelete(a.id)">删除</button>
          </div>
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
.btn-primary { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent,#4A9FD8); color: #FFF; font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; align-items: center; padding: 12px 20px; min-height: 48px; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { flex-shrink: 0; font-family: var(--font-sans,Inter); font-size: 12px; display: flex; align-items: center; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 500; color: #FFF; }
.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--del { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
</style>

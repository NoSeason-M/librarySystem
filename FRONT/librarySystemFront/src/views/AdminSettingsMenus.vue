<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMenus, createMenu, updateMenu, deleteMenu } from '../api/system'

const router = useRouter()

function goBack() { router.push('/admin/settings') }

const menus = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const modalMode = ref<'create'|'edit'>('create')
const editId = ref(0)
const form = ref({ name: '', permission: '', path: '', component: '', icon: '', type: 1, parentId: 0, sort: 0, visible: 1 })
const saving = ref(false)
const formError = ref('')

const typeOptions = [
  { value: 0, label: 'Directory' },
  { value: 1, label: 'Menu' },
  { value: 2, label: 'Button' },
]

async function loadMenus() {
  loading.value = true
  try { menus.value = await listMenus() } catch { menus.value = [] } finally { loading.value = false }
}
function openCreate(parentId = 0) {
  modalMode.value = 'create'; editId.value = 0
  form.value = { name: '', permission: '', path: '', component: '', icon: '', type: parentId > 0 ? 2 : 1, parentId, sort: 0, visible: 1 }
  formError.value = ''; showModal.value = true
}
function openEdit(m: any) {
  modalMode.value = 'edit'; editId.value = m.id
  form.value = { name: m.name, permission: m.permission || '', path: m.path || '', component: m.component || '', icon: m.icon || '', type: m.type, parentId: m.parentId || 0, sort: m.sort || 0, visible: m.visible ?? 1 }
  formError.value = ''; showModal.value = true
}
async function submitForm() {
  if (!form.value.name.trim()) { formError.value = 'Name is required'; return }
  saving.value = true; formError.value = ''
  try {
    if (modalMode.value === 'create') await createMenu(form.value)
    else await updateMenu(editId.value, form.value)
    showModal.value = false; loadMenus()
  } catch (err: any) { formError.value = err.message } finally { saving.value = false }
}
async function handleDelete(id: number) {
  if (!confirm('Delete this menu item?')) return
  try { await deleteMenu(id); loadMenus() } catch { alert('Delete failed') }
}

function getTypeLabel(t: number) { return typeOptions.find(o => o.value === t)?.label || '-' }
function getTypeColor(t: number) { return t === 0 ? '#4A9FD8' : t === 1 ? '#34D399' : '#888' }

function renderMenuTree(items: any[], depth = 0): any[] {
  const result: any[] = []
  for (const m of items) {
    result.push({ ...m, depth })
    if (m.children && m.children.length > 0) result.push(...renderMenuTree(m.children, depth + 1))
  }
  return result
}

onMounted(() => { loadMenus() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header">
        <div class="header-left"><button class="btn-back" @click="goBack">&#8592;</button><h1 class="header__title">Menu Management</h1></div>
        <button class="btn-primary" @click="openCreate(0)">+ Add Menu</button>
      </header>
      <div class="table">
        <div class="table-head">
          <span class="th" style="width:60px">#</span>
          <span class="th" style="width:220px">Name</span>
          <span class="th" style="width:150px">Permission</span>
          <span class="th" style="width:120px">Path</span>
          <span class="th" style="width:60px">Type</span>
          <span class="th" style="width:50px">Sort</span>
          <span class="th-spacer"></span>
          <span class="th th--right" style="width:120px">Actions</span>
        </div>
        <div v-if="loading" class="table-empty">Loading...</div>
        <div v-if="!loading && menus.length === 0" class="table-empty">No menus</div>
        <div v-for="m in renderMenuTree(menus)" :key="m.id" class="table-row" :style="{ paddingLeft: 20 + m.depth * 24 + 'px' }">
          <span class="td td--mono td--muted" style="width:60px">{{ m.id }}</span>
          <span class="td td--title" style="width:220px">{{ m.name }}</span>
          <span class="td td--mono td--muted" style="width:150px;font-size:11px">{{ m.permission || '-' }}</span>
          <span class="td td--muted" style="width:120px;font-size:11px">{{ m.path || '-' }}</span>
          <div class="td" style="width:60px"><span class="status-badge" :style="{background:getTypeColor(m.type)}">{{ getTypeLabel(m.type) }}</span></div>
          <span class="td td--secondary" style="width:50px">{{ m.sort }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:120px">
            <button class="btn-sm btn-sm--edit" @click="openCreate(m.id)" v-if="m.type !== 2">+ Sub</button>
            <button class="btn-sm btn-sm--edit" @click="openEdit(m)">Edit</button>
            <button class="btn-sm btn-sm--del" @click="handleDelete(m.id)">Del</button>
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
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 1px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; align-items: center; padding: 10px 20px; min-height: 44px; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { flex-shrink: 0; font-family: var(--font-sans,Inter); font-size: 12px; display: flex; align-items: center; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.status-badge { padding: 2px 8px; border-radius: 999px; font-size: 10px; font-weight: 500; color: #FFF; }
.btn-sm { padding: 4px 8px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--del { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listRoles, createRole, updateRole, deleteRole, listMenus } from '../api/system'

const router = useRouter()
const adminNav = [
  { icon: '📊', label: 'Dashboard' }, { icon: '📖', label: 'Borrow/Return' }, { icon: '📚', label: 'Books' },
  { icon: '👥', label: 'Readers' }, { icon: '📈', label: 'Statistics' }, { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings' },
]
function navigateTo(l: string) {
  if (l === 'Dashboard') router.push('/admin')
  else if (l === 'Books') router.push('/admin/books')
  else if (l === 'Readers') router.push('/admin/readers')
  else if (l === 'Statistics') router.push('/admin/statistics')
  else if (l === 'Settings') router.push('/admin/settings')
}
function goBack() { router.push('/admin/settings') }

const roles = ref<any[]>([])
const menus = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const editId = ref(0)
const form = ref({ name: '', code: '', description: '', menuIds: [] as number[] })
const saving = ref(false)
const formError = ref('')

async function loadRoles() {
  loading.value = true
  try { roles.value = await listRoles() } catch { roles.value = [] } finally { loading.value = false }
}
async function loadMenus() {
  try { menus.value = await listMenus() } catch { menus.value = [] }
}

function openCreate() {
  modalMode.value = 'create'; editId.value = 0
  form.value = { name: '', code: '', description: '', menuIds: [] }; formError.value = ''; showModal.value = true
}
function openEdit(r: any) {
  modalMode.value = 'edit'; editId.value = r.id
  form.value = { name: r.name, code: r.code, description: r.description || '', menuIds: (r.menuIds || []).map(Number) }
  formError.value = ''; showModal.value = true
}
async function submitForm() {
  if (!form.value.name.trim() || !form.value.code.trim()) { formError.value = 'Name and Code are required'; return }
  saving.value = true; formError.value = ''
  try {
    if (modalMode.value === 'create') await createRole(form.value)
    else await updateRole(editId.value, form.value)
    showModal.value = false; loadRoles()
  } catch (err: any) { formError.value = err.message } finally { saving.value = false }
}
async function handleDelete(id: number) {
  if (!confirm('Delete this role?')) return
  try { await deleteRole(id); loadRoles() } catch { alert('Delete failed') }
}

function toggleMenu(mid: number) {
  const idx = form.value.menuIds.indexOf(mid)
  if (idx >= 0) form.value.menuIds.splice(idx, 1)
  else form.value.menuIds.push(mid)
}

function getAllMenuIds(items: any[]): number[] {
  let ids: number[] = []
  for (const m of items) { ids.push(m.id); if (m.children) ids = ids.concat(getAllMenuIds(m.children)) }
  return ids
}

function selectAll() { form.value.menuIds = getAllMenuIds(menus.value) }
function deselectAll() { form.value.menuIds = [] }

onMounted(() => { loadRoles(); loadMenus() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header"><div class="header-left"><button class="btn-back" @click="goBack">←</button><h1 class="header__title">Role Management</h1></div><button class="btn-primary" @click="openCreate">+ Create Role</button></header>
      <div class="table">
        <div class="table-head"><span class="th" style="width:80px">ID</span><span class="th" style="width:150px">Name</span><span class="th" style="width:180px">Code</span><span class="th" style="width:220px">Description</span><span class="th" style="width:60px">Sort</span><span class="th-spacer"></span><span class="th th--right" style="width:120px">Actions</span></div>
        <div v-if="loading" class="table-empty">Loading...</div>
        <div v-if="!loading && roles.length === 0" class="table-empty">No roles found</div>
        <div v-for="r in roles" :key="r.id" class="table-row">
          <span class="td td--mono td--muted" style="width:80px">{{ r.id }}</span>
          <span class="td td--title" style="width:150px">{{ r.name }}</span>
          <span class="td td--mono" style="width:180px">{{ r.code }}</span>
          <span class="td td--secondary" style="width:220px">{{ r.description || '—' }}</span>
          <span class="td td--secondary" style="width:60px">{{ r.sort }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:120px">
            <button class="btn-sm btn-sm--edit" @click="openEdit(r)">Edit</button>
            <button class="btn-sm btn-sm--del" @click="handleDelete(r.id)" v-if="r.code !== 'ROLE_ADMIN'">Delete</button>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal modal--lg">
        <div class="modal__header"><h2 class="modal__title">{{ modalMode === 'create' ? 'Create Role' : 'Edit Role' }}</h2><button class="modal__close" @click="showModal = false">✕</button></div>
        <div v-if="formError" class="modal__error">{{ formError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">Name *</label><div class="input-box"><input v-model="form.name" type="text" placeholder="e.g. 阅览室管理员" /></div></div>
            <div class="field"><label class="field-label">Code *</label><div class="input-box"><input v-model="form.code" type="text" placeholder="e.g. ROLE_READING_ROOM" /></div></div>
          </div>
          <div class="field"><label class="field-label">Description</label><div class="input-box"><input v-model="form.description" type="text" placeholder="Role description" /></div></div>
          <div class="field"><label class="field-label">Permissions <span style="font-size:11px;color:var(--text-muted,#888);font-weight:400">(select menus and buttons)</span></label>
            <div class="permission-actions"><button class="btn-link" @click="selectAll">Select All</button><button class="btn-link" @click="deselectAll">Deselect All</button></div>
            <div class="menu-tree">
              <div v-for="m in menus" :key="m.id" class="menu-node">
                <label class="menu-checkbox"><input type="checkbox" :checked="form.menuIds.includes(m.id)" @change="toggleMenu(m.id)" /><span :style="{fontWeight:600}">{{ m.name }}</span><span v-if="m.permission" class="menu-perm">{{ m.permission }}</span></label>
                <div v-if="m.children" class="menu-children">
                  <div v-for="c in m.children" :key="c.id" class="menu-node menu-node--child">
                    <label class="menu-checkbox"><input type="checkbox" :checked="form.menuIds.includes(c.id)" @change="toggleMenu(c.id)" /><span>{{ c.name }}</span><span v-if="c.permission" class="menu-perm">{{ c.permission }}</span></label>
                    <div v-if="c.children" class="menu-children">
                      <div v-for="b in c.children" :key="b.id" class="menu-node menu-node--btn">
                        <label class="menu-checkbox"><input type="checkbox" :checked="form.menuIds.includes(b.id)" @change="toggleMenu(b.id)" /><span class="menu-btn-name">{{ b.name }}</span><span v-if="b.permission" class="menu-perm">{{ b.permission }}</span></label>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal__footer"><button class="btn-cancel" @click="showModal = false">Cancel</button><button class="btn-primary" :disabled="saving" @click="submitForm"><span v-if="saving" class="spinner"></span><span v-else>Save</span></button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-page { display: flex; min-height: 100vh; background: var(--bg-secondary,#F7F8FA); }
.sidebar { width: 240px; background: var(--bg-inverse,#0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse,#FFF); }
.sidebar__logo-text { font-family: var(--font-sans,Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); transition: background 0.15s; }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-inverse,#FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent,#4A9FD8); flex-shrink: 0; }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.btn-back { width: 36px; height: 36px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; color: var(--text-secondary,#666); }
.btn-back:hover { background: var(--bg-secondary,#F7F8FA); }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.btn-primary { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; }
.btn-primary:hover { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 10px 24px; border-radius: 10px; background: var(--bg-primary,#FFF); color: var(--text-secondary,#666); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border,#E5E7EB); cursor: pointer; }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; align-items: center; padding: 12px 20px; min-height: 48px; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { flex-shrink: 0; display: flex; align-items: center; font-family: var(--font-sans,Inter); font-size: 12px; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; }
.btn-sm:hover { opacity: 0.8; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--del { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 520px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal--lg { max-width: 640px; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; display: flex; align-items: center; justify-content: center; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }
.modal__error { margin: 0 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius,12px); background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--border,#E5E7EB); }
.input-box:focus-within { border-color: var(--accent,#4A9FD8); border-width: 2px; }
.input-box input { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.form-row-2 { display: flex; gap: 12px; }
.permission-actions { display: flex; gap: 12px; margin-bottom: 4px; }
.btn-link { background: none; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--accent,#4A9FD8); padding: 0; }
.btn-link:hover { text-decoration: underline; }
.menu-tree { max-height: 320px; overflow-y: auto; padding: 8px; background: var(--bg-secondary,#F7F8FA); border-radius: 10px; display: flex; flex-direction: column; gap: 4px; }
.menu-node { display: flex; flex-direction: column; gap: 2px; }
.menu-node--child { padding-left: 24px; }
.menu-node--btn { padding-left: 48px; }
.menu-checkbox { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-secondary,#666); cursor: pointer; padding: 3px 0; }
.menu-checkbox input[type="checkbox"] { accent-color: var(--accent,#4A9FD8); }
.menu-perm { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 10px; color: var(--text-muted,#888); margin-left: 4px; }
.menu-btn-name { color: var(--text-muted,#888); }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse,#FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

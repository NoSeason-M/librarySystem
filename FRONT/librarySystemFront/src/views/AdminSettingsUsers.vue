<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listUsers, createUser, updateUser, resetPassword, listRoles } from '../api/system'

const router = useRouter()

const adminNav = [
  { icon: '📊', label: 'Dashboard' }, { icon: '📖', label: 'Borrow/Return' }, { icon: '📚', label: 'Books' },
  { icon: '👥', label: 'Readers' }, { icon: '📈', label: 'Statistics' }, { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings' },
]

const keyword = ref('')
const users = ref<any[]>([])
const roles = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const loading = ref(false)

const showModal = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const editId = ref(0)
const form = ref({ username: '', realName: '', email: '', phone: '', password: '', roleIds: [] as number[] })
const saving = ref(false)
const formError = ref('')

const showDetail = ref(false)
const detailUser = ref<any>(null)

const visiblePages = computed(() => {
  const p: (number | string)[] = []
  if (totalPages.value <= 5) { for (let i = 1; i <= totalPages.value; i++) p.push(i) }
  else {
    p.push(1); const s = Math.max(2, currentPage.value - 1); const e = Math.min(totalPages.value - 1, currentPage.value + 1)
    if (s > 2) p.push('...'); for (let i = s; i <= e; i++) p.push(i); if (e < totalPages.value - 1) p.push('...'); p.push(totalPages.value)
  }
  return p
})

function navigateTo(l: string) {
  if (l === 'Dashboard') router.push('/admin')
  else if (l === 'Books') router.push('/admin/books')
  else if (l === 'Readers') router.push('/admin/readers')
  else if (l === 'Statistics') router.push('/admin/statistics')
  else if (l === 'Settings') router.push('/admin/settings')
}

async function loadUsers() {
  loading.value = true
  try {
    const r = await listUsers(keyword.value || undefined, currentPage.value, 10)
    users.value = r.records; total.value = r.total; totalPages.value = r.pages || 1
  } catch { users.value = [] } finally { loading.value = false }
}

async function loadRoles() {
  try { roles.value = await listRoles() } catch { roles.value = [] }
}

function openCreate() {
  modalMode.value = 'create'; editId.value = 0
  form.value = { username: '', realName: '', email: '', phone: '', password: '', roleIds: [] }
  formError.value = ''; showModal.value = true
}

function openEdit(u: any) {
  modalMode.value = 'edit'; editId.value = u.id
  form.value = { username: u.username || '', realName: u.realName || '', email: u.email || '', phone: u.phone || '', password: '', roleIds: (u.roleIds || []).map(Number) }
  formError.value = ''; showModal.value = true
}

function openDetail(u: any) { detailUser.value = u; showDetail.value = true }

async function submitForm() {
  if (!form.value.realName.trim()) { formError.value = '请输入姓名'; return }
  saving.value = true; formError.value = ''
  try {
    if (modalMode.value === 'create') {
      if (!form.value.username.trim()) { formError.value = '请输入用户名'; saving.value = false; return }
      await createUser(form.value)
    } else {
      await updateUser(editId.value, form.value)
    }
    showModal.value = false; loadUsers()
  } catch (err: any) { formError.value = err.message || '操作失败' } finally { saving.value = false }
}

async function handleResetPwd(id: number) {
  if (!confirm('确认重置密码为 123456？')) return
  try { await resetPassword(id); alert('密码已重置为 123456') } catch { alert('重置失败') }
}

function toggleRole(rid: number) {
  const idx = form.value.roleIds.indexOf(rid)
  if (idx >= 0) form.value.roleIds.splice(idx, 1)
  else form.value.roleIds.push(rid)
}

function goBack() { router.push('/admin/settings') }

onMounted(() => { loadUsers(); loadRoles() })
</script>

<template>
  <div class="settings-page">

    <main class="main">
      <header class="header">
        <div class="header-left"><button class="btn-back" @click="goBack">←</button><h1 class="header__title">用户管理</h1></div>
        <button class="btn-primary" @click="openCreate">+ 创建用户</button>
      </header>

      <div class="toolbar">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="keyword" class="search-input" placeholder="搜索用户名或姓名..." @keyup.enter="currentPage=1;loadUsers()" /></div>
      </div>

      <div class="table">
        <div class="table-head"><span class="th" style="width:60px">ID</span><span class="th" style="width:120px">用户名</span><span class="th" style="width:120px">姓名</span><span class="th" style="width:180px">邮箱</span><span class="th" style="width:120px">电话</span><span class="th" style="width:200px">角色</span><span class="th" style="width:80px">状态</span><span class="th-spacer"></span><span class="th th--right" style="width:160px">操作</span></div>
        <div v-if="loading" class="table-empty">加载中...</div>
        <div v-if="!loading && users.length === 0" class="table-empty">暂无用户</div>
        <div v-for="u in users" :key="u.id" class="table-row">
          <span class="td td--mono td--muted" style="width:60px">{{ u.id }}</span>
          <span class="td td--title" style="width:120px">{{ u.username }}</span>
          <span class="td td--secondary" style="width:120px">{{ u.realName }}</span>
          <span class="td td--secondary" style="width:180px">{{ u.email || '—' }}</span>
          <span class="td td--secondary" style="width:120px">{{ u.phone || '—' }}</span>
          <span class="td td--secondary" style="width:200px">{{ (u.roleNames || []).join(', ') || '—' }}</span>
          <div class="td" style="width:80px"><span class="status-badge" :style="{ background: u.status === 1 ? 'var(--success,#34D399)' : 'var(--danger,#F87171)' }">{{ u.status === 1 ? '启用' : '禁用' }}</span></div>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:160px">
            <button class="btn-sm btn-sm--edit" @click="openEdit(u)">编辑</button>
            <button class="btn-sm btn-sm--card" @click="handleResetPwd(u.id)">重置密码</button>
          </div>
        </div>
      </div>

      <div class="pagination">
        <span class="page-info">共 {{ total }} 条，显示 {{ users.length }} 条</span>
        <div class="page-buttons">
          <span :class="['page-prev',{ 'page--disabled': currentPage <= 1 }]" @click="currentPage > 1 && (currentPage--,loadUsers())">←</span>
          <template v-for="p in visiblePages" :key="p"><div v-if="typeof p === 'number'" :class="['page-num',{ 'page-num--active': p === currentPage }]" @click="currentPage=p;loadUsers()">{{ p }}</div><span v-else class="page-ellipsis">...</span></template>
          <span :class="['page-next',{ 'page--disabled': currentPage >= totalPages }]" @click="currentPage < totalPages && (currentPage++,loadUsers())">→</span>
        </div>
      </div>
    </main>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal__header"><h2 class="modal__title">{{ modalMode === 'create' ? '创建用户' : '编辑用户' }}</h2><button class="modal__close" @click="showModal = false">✕</button></div>
        <div v-if="formError" class="modal__error">{{ formError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">用户名 *</label><div class="input-box"><input v-model="form.username" type="text" placeholder="登录用户名" :disabled="modalMode === 'edit'" /></div></div>
            <div class="field"><label class="field-label">姓名 *</label><div class="input-box"><input v-model="form.realName" type="text" placeholder="真实姓名" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">邮箱</label><div class="input-box"><input v-model="form.email" type="email" placeholder="电子邮箱" /></div></div>
            <div class="field"><label class="field-label">电话</label><div class="input-box"><input v-model="form.phone" type="tel" placeholder="手机号" /></div></div>
          </div>
          <div v-if="modalMode === 'create'" class="field"><label class="field-label">初始密码</label><div class="input-box"><input v-model="form.password" type="text" placeholder="默认: 123456" /></div></div>
          <div class="field"><label class="field-label">角色</label><div class="role-checkboxes"><label v-for="r in roles" :key="r.id" class="role-checkbox"><input type="checkbox" :checked="form.roleIds.includes(r.id)" @change="toggleRole(r.id)" /><span>{{ r.name }}</span></label></div></div>
        </div>
        <div class="modal__footer"><button class="btn-cancel" @click="showModal = false">取消</button><button class="btn-primary" :disabled="saving" @click="submitForm"><span v-if="saving" class="spinner"></span><span v-else>{{ modalMode === 'create' ? '创建' : '保存' }}</span></button></div>
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

.btn-primary { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; }
.btn-primary:hover { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 10px 24px; border-radius: 10px; background: var(--bg-primary,#FFF); color: var(--text-secondary,#666); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border,#E5E7EB); cursor: pointer; }

.toolbar { display: flex; gap: 12px; align-items: center; }
.search-box { display: flex; align-items: center; gap: 8px; width: 360px; padding: 10px 16px; border-radius: var(--input-radius,12px); background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); }
.search-icon { font-size: 14px; color: var(--text-muted,#888); }
.search-input { flex: 1; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.search-input::placeholder { color: var(--text-muted,#888); }

.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; align-items: center; padding: 12px 20px; min-height: 56px; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { flex-shrink: 0; display: flex; align-items: center; font-family: var(--font-sans,Inter); font-size: 12px; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 500; color: var(--text-inverse,#FFF); }

.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; transition: opacity 0.15s; }
.btn-sm:hover { opacity: 0.8; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--card { background: rgba(74,159,216,0.08); color: var(--accent,#4A9FD8); }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--accent,#4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted,#888); cursor: default; pointer-events: none; }
.page-num { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); cursor: pointer; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); }
.page-num--active { background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-weight: 600; border-color: var(--accent,#4A9FD8); }
.page-ellipsis { font-size: 12px; color: var(--text-muted,#888); padding: 0 4px; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 560px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; display: flex; align-items: center; justify-content: center; }
.modal__close:hover { background: var(--border,#E5E7EB); }
.modal__error { margin: 12px 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }

.field { display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0; }
.field-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius,12px); background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--border,#E5E7EB); }
.input-box:focus-within { border-color: var(--accent,#4A9FD8); border-width: 2px; }
.input-box input { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.input-box input::placeholder { color: var(--text-muted,#888); }
.form-row-2 { display: flex; gap: 12px; }
.role-checkboxes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 8px 0;
}
.role-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1.5px solid var(--border, #E5E7EB);
  background: var(--bg-primary, #FFF);
  font-family: var(--font-sans,Inter);
  font-size: 13px;
  color: var(--text-secondary,#666);
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
  width: 100%;
}
.role-checkbox:hover {
  border-color: var(--accent, #4A9FD8);
  background: var(--accent-light, #E8F4FD);
}
.role-checkbox:has(input:checked) {
  border-color: var(--accent, #4A9FD8);
  background: var(--accent-light, #E8F4FD);
}
.role-checkbox input[type="checkbox"] { accent-color: var(--accent,#4A9FD8); }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse,#FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

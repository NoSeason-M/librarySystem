<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/index'

interface PublisherItem {
  id: number
  name: string
  shortName?: string
  phone?: string
  address?: string
}

const router = useRouter()
const loading = ref(true)
const keyword = ref('')
const publishers = ref<PublisherItem[]>([])

const showModal = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const editId = ref(0)
const form = ref({ name: '', shortName: '', phone: '', address: '' })
const saving = ref(false)
const formError = ref('')

onMounted(() => loadPublishers())

async function loadPublishers() {
  loading.value = true
  try {
    publishers.value = await http.get('/publishers') as any
  } catch {
    publishers.value = []
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  if (!keyword.value) return publishers.value
  const kw = keyword.value.toLowerCase()
  return publishers.value.filter(p =>
    p.name.toLowerCase().includes(kw) ||
    (p.shortName || '').toLowerCase().includes(kw)
  )
})

function openCreate() {
  modalMode.value = 'create'
  editId.value = 0
  form.value = { name: '', shortName: '', phone: '', address: '' }
  formError.value = ''
  showModal.value = true
}

function openEdit(p: PublisherItem) {
  modalMode.value = 'edit'
  editId.value = p.id
  form.value = { name: p.name, shortName: p.shortName || '', phone: p.phone || '', address: p.address || '' }
  formError.value = ''
  showModal.value = true
}

async function submitForm() {
  if (!form.value.name.trim()) { formError.value = '请输入出版社名称'; return }
  saving.value = true
  formError.value = ''
  try {
    if (modalMode.value === 'create') {
      await http.post('/publishers', form.value)
    } else {
      await http.put(`/publishers/${editId.value}`, form.value)
    }
    showModal.value = false
    loadPublishers()
  } catch (err: any) {
    formError.value = err.message || '操作失败'
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number, name: string) {
  if (!confirm(`确认删除出版社「${name}」？`)) return
  try {
    await http.delete(`/publishers/${id}`)
    loadPublishers()
  } catch {
    alert('删除失败，可能有关联的图书')
  }
}
</script>

<template>
  <div class="admin-publishers">
    <main class="main">
      <header class="header">
        <div class="header-left">
          <button class="btn-back" @click="$router.push('/admin/books')">←</button>
          <h1 class="header__title">出版社管理</h1>
        </div>
        <button class="btn-add" @click="openCreate"><span class="btn-add__icon">+</span><span>新增出版社</span></button>
      </header>

      <div class="toolbar">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input v-model="keyword" class="search-input" placeholder="搜索名称或简称..." />
        </div>
      </div>

      <div class="table">
        <div class="table-head">
          <span class="th" style="width:60px">ID</span>
          <span class="th" style="width:180px">名称</span>
          <span class="th" style="width:120px">简称</span>
          <span class="th" style="width:140px">电话</span>
          <span class="th" style="width:260px">地址</span>
          <span class="th-spacer"></span>
          <span class="th th--right" style="width:120px">操作</span>
        </div>

        <div v-if="loading" class="table-empty">加载中...</div>
        <div v-if="!loading && filtered.length === 0" class="table-empty">暂无出版社</div>

        <div v-for="p in filtered" :key="p.id" class="table-row">
          <span class="td td--mono td--muted" style="width:60px">{{ p.id }}</span>
          <span class="td td--name" style="width:180px">{{ p.name }}</span>
          <span class="td" style="width:120px">{{ p.shortName || '—' }}</span>
          <span class="td td--secondary" style="width:140px">{{ p.phone || '—' }}</span>
          <span class="td td--secondary" style="width:260px">{{ p.address || '—' }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:120px">
            <button class="btn-sm btn-sm--edit" @click="openEdit(p)">编辑</button>
            <button class="btn-sm btn-sm--del" @click="handleDelete(p.id, p.name)">删除</button>
          </div>
        </div>
      </div>
    </main>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal__header">
          <h2 class="modal__title">{{ modalMode === 'create' ? '新增出版社' : '编辑出版社' }}</h2>
          <button class="modal__close" @click="showModal = false">✕</button>
        </div>
        <div v-if="formError" class="modal__error">{{ formError }}</div>
        <div class="modal__body">
          <div class="field"><label class="field-label">名称 *</label><div class="input-box"><input v-model="form.name" type="text" placeholder="出版社全称" /></div></div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">简称</label><div class="input-box"><input v-model="form.shortName" type="text" placeholder="如：人邮社" /></div></div>
            <div class="field"><label class="field-label">电话</label><div class="input-box"><input v-model="form.phone" type="text" placeholder="联系电话" /></div></div>
          </div>
          <div class="field"><label class="field-label">地址</label><div class="input-box"><input v-model="form.address" type="text" placeholder="出版社地址" /></div></div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showModal = false">取消</button>
          <button class="btn-primary" :disabled="saving" @click="submitForm">
            <span v-if="saving" class="spinner"></span><span v-else>{{ modalMode === 'create' ? '创建' : '保存' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-publishers { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary,#F7F8FA); }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.btn-back { width: 36px; height: 36px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; color: var(--text-secondary,#666); }
.btn-back:hover { background: var(--bg-secondary,#F7F8FA); }
.btn-add { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; }
.btn-add:hover { opacity: 0.9; }
.btn-add__icon { font-size: 16px; line-height: 1; }
.toolbar { display: flex; gap: 12px; align-items: center; }
.search-box { display: flex; align-items: center; gap: 8px; width: 320px; padding: 10px 16px; border-radius: var(--input-radius,12px); background: var(--bg-primary,#FFF); border: 1.5px solid var(--border,#E5E7EB); }
.search-icon { font-size: 14px; color: var(--text-muted,#888); }
.search-input { flex: 1; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.search-input::placeholder { color: var(--text-muted,#888); }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); align-items: center; }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; padding: 12px 20px; align-items: center; border-top: 0.5px solid var(--border,#E5E7EB); }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.td { font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-secondary,#666); flex-shrink: 0; }
.td--name { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; transition: opacity 0.15s; }
.btn-sm:hover { opacity: 0.8; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--del { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 520px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; }
.modal__close:hover { background: var(--border,#E5E7EB); }
.modal__error { margin: 12px 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }
.field { display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0; }
.field-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius,12px); background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--border,#E5E7EB); }
.input-box:focus-within { border-color: var(--accent,#4A9FD8); }
.input-box input { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.input-box input::placeholder { color: var(--text-muted,#888); }
.form-row-2 { display: flex; gap: 12px; }
.btn-primary { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius,10px); background: var(--accent,#4A9FD8); color: var(--text-inverse,#FFF); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; min-width: 80px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 10px 24px; border-radius: var(--button-radius,10px); background: var(--bg-primary,#FFF); color: var(--text-secondary,#666); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border,#E5E7EB); cursor: pointer; }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse,#FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

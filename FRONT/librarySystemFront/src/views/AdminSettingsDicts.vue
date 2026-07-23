<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listDicts, getDictWithItems, createDictItem, updateDictItem, deleteDictItem } from '../api/system'

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

const dicts = ref<any[]>([])
const selectedDict = ref<any>(null)
const items = ref<any[]>([])
const loading = ref(false)
const showItemModal = ref(false)
const editItemId = ref(0)
const itemForm = ref({ itemLabel: '', itemValue: '', sort: 0 })
const itemSaving = ref(false)
const itemError = ref('')

async function loadDicts() {
  loading.value = true
  try { dicts.value = await listDicts() } catch { dicts.value = [] } finally { loading.value = false }
}
async function selectDict(d: any) {
  selectedDict.value = d
  items.value = []
  try { const r = await getDictWithItems(d.dictCode); items.value = r.items || [] } catch { items.value = [] }
}
function openCreateItem() {
  editItemId.value = 0; itemForm.value = { itemLabel: '', itemValue: '', sort: 0 }
  itemError.value = ''; showItemModal.value = true
}
function openEditItem(item: any) {
  editItemId.value = item.id; itemForm.value = { itemLabel: item.itemLabel, itemValue: item.itemValue, sort: item.sort || 0 }
  itemError.value = ''; showItemModal.value = true
}
async function submitItem() {
  if (!itemForm.value.itemLabel.trim() || !itemForm.value.itemValue.trim()) { itemError.value = 'Label and value are required'; return }
  itemSaving.value = true; itemError.value = ''
  try {
    if (editItemId.value === 0) await createDictItem({ ...itemForm.value, dictCode: selectedDict.value.dictCode })
    else await updateDictItem(editItemId.value, itemForm.value)
    showItemModal.value = false; selectDict(selectedDict.value)
  } catch (err: any) { itemError.value = err.message } finally { itemSaving.value = false }
}
async function handleDeleteItem(id: number) {
  if (!confirm('Delete this item?')) return
  try { await deleteDictItem(id); selectDict(selectedDict.value) } catch { alert('Delete failed') }
}
onMounted(() => { loadDicts() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header"><div class="header-left"><button class="btn-back" @click="goBack">←</button><h1 class="header__title">Data Dictionary</h1></div></header>
      <div class="dict-layout">
        <div class="dict-list">
          <div v-if="loading" class="table-empty">Loading...</div>
          <div v-for="d in dicts" :key="d.id" :class="['dict-card', { 'dict-card--active': selectedDict?.id === d.id }]" @click="selectDict(d)">
            <span class="dict-name">{{ d.dictName }}</span>
            <span class="dict-code">{{ d.dictCode }}</span>
          </div>
        </div>
        <div class="dict-items">
          <div v-if="!selectedDict" class="table-empty">Select a dictionary from the left</div>
          <template v-else>
            <div class="dict-header"><h2 class="dict-title">{{ selectedDict.dictName }} <span class="dict-code-inline">({{ selectedDict.dictCode }})</span></h2><button class="btn-primary btn-sm--text" @click="openCreateItem">+ Add Item</button></div>
            <div class="table">
              <div class="table-head"><span class="th" style="width:60px">ID</span><span class="th" style="width:140px">Label</span><span class="th" style="width:140px">Value</span><span class="th" style="width:60px">Sort</span><span class="th" style="width:60px">Default</span><span class="th-spacer"></span><span class="th th--right" style="width:120px">Actions</span></div>
              <div v-if="items.length === 0" class="table-empty">No items</div>
              <div v-for="item in items" :key="item.id" class="table-row">
                <span class="td td--mono td--muted" style="width:60px">{{ item.id }}</span>
                <span class="td td--title" style="width:140px">{{ item.itemLabel }}</span>
                <span class="td td--mono" style="width:140px">{{ item.itemValue }}</span>
                <span class="td td--secondary" style="width:60px">{{ item.sort }}</span>
                <span class="td td--secondary" style="width:60px"><span v-if="item.defaultFlag" class="status-badge" style="background:var(--accent,#4A9FD8);padding:2px 8px;border-radius:999px;font-size:10px;color:#FFF">Default</span></span>
                <div class="td-spacer"></div>
                <div class="td td--actions" style="width:120px"><button class="btn-sm btn-sm--edit" @click="openEditItem(item)">Edit</button><button class="btn-sm btn-sm--del" @click="handleDeleteItem(item.id)">Delete</button></div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </main>

    <div v-if="showItemModal" class="modal-overlay" @click.self="showItemModal = false">
      <div class="modal">
        <div class="modal__header"><h2 class="modal__title">{{ editItemId === 0 ? 'Add' : 'Edit' }} Dictionary Item</h2><button class="modal__close" @click="showItemModal = false">✕</button></div>
        <div v-if="itemError" class="modal__error">{{ itemError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">Label *</label><div class="input-box"><input v-model="itemForm.itemLabel" type="text" placeholder="Display label" /></div></div>
            <div class="field"><label class="field-label">Value *</label><div class="input-box"><input v-model="itemForm.itemValue" type="text" placeholder="Actual value" /></div></div>
          </div>
          <div class="field"><label class="field-label">Sort</label><div class="input-box"><input v-model.number="itemForm.sort" type="number" placeholder="0" /></div></div>
        </div>
        <div class="modal__footer"><button class="btn-cancel" @click="showItemModal = false">Cancel</button><button class="btn-primary" :disabled="itemSaving" @click="submitItem"><span v-if="itemSaving" class="spinner"></span><span v-else>Save</span></button></div>
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
.btn-primary { padding: 10px 20px; border-radius: 10px; background: var(--accent,#4A9FD8); color: #FFF; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 600; }
.btn-primary:disabled { opacity: 0.6; }
.btn-cancel { padding: 10px 24px; border-radius: 10px; background: var(--bg-primary,#FFF); color: var(--text-secondary,#666); border: 1.5px solid var(--border,#E5E7EB); cursor: pointer; font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; padding: 14px 20px; background: var(--bg-secondary,#F7F8FA); }
.th { font-family: var(--font-sans,Inter); font-size: 12px; font-weight: 600; color: var(--text-muted,#888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; padding: 10px 20px; border-top: 0.5px solid var(--border,#E5E7EB); align-items: center; }
.td { flex-shrink: 0; font-family: var(--font-sans,Inter); font-size: 12px; display: flex; align-items: center; }
.td--title { font-weight: 500; color: var(--text-primary,#1A1A1A); }
.td--secondary { color: var(--text-secondary,#666); }
.td--mono { font-family: var(--font-mono,'Geist Mono',monospace); }
.td--muted { color: var(--text-muted,#888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--del { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
.status-badge { font-weight: 500; white-space: nowrap; }

.dict-layout { display: flex; gap: 24px; flex: 1; }
.dict-list { width: 280px; display: flex; flex-direction: column; gap: 6px; flex-shrink: 0; }
.dict-card { padding: 14px; border-radius: 12px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; transition: all 0.1s; display: flex; flex-direction: column; gap: 4px; }
.dict-card:hover { border-color: var(--accent,#4A9FD8); }
.dict-card--active { border-color: var(--accent,#4A9FD8); background: var(--accent-light,#E8F4FD); }
.dict-name { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.dict-code { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 11px; color: var(--text-muted,#888); }
.dict-items { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.dict-header { display: flex; justify-content: space-between; align-items: center; }
.dict-title { font-family: var(--font-sans,Inter); font-size: 16px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.dict-code-inline { font-weight: 400; font-size: 12px; color: var(--text-muted,#888); }
.btn-sm--text { padding: 6px 14px; font-size: 12px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 500px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; }
.modal__error { margin: 12px 28px 0; padding: 10px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }
.field { display: flex; flex-direction: column; gap: 6px; flex: 1; }
.field-label { font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: 12px; background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--border,#E5E7EB); }
.input-box:focus-within { border-color: var(--accent,#4A9FD8); }
.input-box input { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.form-row-2 { display: flex; gap: 12px; }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse,#FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

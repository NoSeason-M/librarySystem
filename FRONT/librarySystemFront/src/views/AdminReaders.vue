<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listReaders, getReaderTypes, createReader, updateReader, cardAction } from '../api/readers'
import type { ReaderItem, ReaderType } from '../api/readers'

const router = useRouter()

// ===== Sidebar =====
const adminNav = [
  { icon: '📊', label: 'Dashboard' },
  { icon: '📖', label: 'Borrow/Return' },
  { icon: '📚', label: 'Books' },
  { icon: '👥', label: 'Readers' },
  { icon: '📈', label: 'Statistics' },
  { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings' },
]

// ===== Table =====
const keyword = ref('')
const readers = ref<ReaderItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 8
const loading = ref(false)
const errorMsg = ref('')

// ===== Filters =====
const defaultReaderTypes: ReaderType[] = [
  { id: 1, name: 'Student', code: 'STUDENT', maxBorrow: 5, borrowDays: 30 },
  { id: 2, name: 'Teacher', code: 'TEACHER', maxBorrow: 10, borrowDays: 60 },
  { id: 3, name: 'Staff', code: 'STAFF', maxBorrow: 8, borrowDays: 45 },
  { id: 4, name: 'External', code: 'EXTERNAL', maxBorrow: 3, borrowDays: 14 },
]
const readerTypes = ref<ReaderType[]>(defaultReaderTypes)
const selectedTypeId = ref<number | undefined>(undefined)
const typeDropdownOpen = ref(false)
const selectedCardStatus = ref<number | undefined>(undefined)
const showAdvancedSearch = ref(false)
const advSearch = ref({ readerNo: "", email: "", registerDateStart: "", registerDateEnd: "" })
const statusDropdownOpen = ref(false)

const statusFilterOptions = [
  { value: undefined, label: 'Card Status' },
  { value: 1, label: 'Normal' },
  { value: 0, label: 'Lost' },
  { value: 2, label: 'Frozen' },
]

// ===== Modal =====
const showCardModal = ref(false)
const cardReader = ref<ReaderItem | null>(null)
const cardActionLoading = ref(false)
const cardActionMsg = ref('')

const showAddModal = ref(false)
const addForm = ref({ realName: '', email: '', phone: '', readerTypeId: 1, remark: '' })
const saving = ref(false)
const submitError = ref('')

const showEditModal = ref(false)
const editForm = ref({ id: 0, realName: '', email: '', phone: '', readerTypeId: 1 })
const editLoading = ref(false)
const editError = ref('')

// ===== Computed =====
const filteredReaders = computed(() => {
  let result = readers.value
  if (selectedTypeId.value) {
    result = result.filter(r => r.readerTypeId === selectedTypeId.value)
  }
  if (selectedCardStatus.value !== undefined) {
    result = result.filter(r => r.cardStatus === selectedCardStatus.value)
  }
  return result
})

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  if (totalPages.value <= 5) {
    for (let i = 1; i <= totalPages.value; i++) pages.push(i)
  } else {
    pages.push(1)
    const start = Math.max(2, currentPage.value - 1)
    const end = Math.min(totalPages.value - 1, currentPage.value + 1)
    if (start > 2) pages.push('...')
    for (let i = start; i <= end; i++) pages.push(i)
    if (end < totalPages.value - 1) pages.push('...')
    pages.push(totalPages.value)
  }
  return pages
})

// ===== Methods =====
async function loadReaders() {
  loading.value = true
  try {
    const result = await listReaders(keyword.value || undefined, selectedTypeId.value, selectedCardStatus.value, advSearch.value.readerNo || undefined, advSearch.value.email || undefined, advSearch.value.registerDateStart || undefined, advSearch.value.registerDateEnd || undefined, currentPage.value, pageSize)
    readers.value = result.records
    total.value = result.total
    totalPages.value = result.pages || 1
  } catch {
    readers.value = []
    errorMsg.value = 'Failed to load readers'
  } finally {
    loading.value = false
  }
}

async function loadReaderTypes() {
  try {
    const types = await getReaderTypes()
    if (types && types.length > 0) {
      readerTypes.value = types
    }
  } catch { /* use defaults */ }
}

function clearAdvSearch() {
  advSearch.value = { readerNo: "", email: "", registerDateStart: "", registerDateEnd: "" }
  onSearch()
}
function toggleAdvanced() {
  showAdvancedSearch.value = !showAdvancedSearch.value
  if (!showAdvancedSearch.value) clearAdvSearch()
}

function onSearch() {
  currentPage.value = 1
  loadReaders()
}

function goToPage(page: number) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadReaders()
}

function navigateTo(label: string) {
  if (label === 'Dashboard') router.push('/admin')
  else if (label === 'Books') router.push('/admin/books')
}

function getCardStatusColor(status: number | undefined): string {
  if (status === undefined || status === 1) return 'var(--success, #34D399)'
  if (status === 0) return 'var(--danger, #F87171)'
  if (status === 2) return 'var(--text-muted, #888888)'
  return 'var(--text-muted, #888)'
}

function getCardStatusLabel(status: number | undefined): string {
  if (status === undefined || status === 1) return 'Normal'
  if (status === 0) return 'Lost'
  if (status === 2) return 'Frozen'
  return 'Unknown'
}

function getInitials(name: string): string {
  return (name || '?').charAt(0).toUpperCase()
}

// ===== Card Action Modal =====
function openCardModal(reader: ReaderItem) {
  cardReader.value = reader
  cardActionMsg.value = ''
  showCardModal.value = true
}

async function handleCardAction(action: string) {
  if (!cardReader.value) return
  cardActionLoading.value = true
  cardActionMsg.value = ''
  try {
    await cardAction(cardReader.value.id, action)
    cardActionMsg.value = 'Card status updated!'
    loadReaders()
    // Update local state
    const actionMap: Record<string, number> = { lost: 0, restore: 1, freeze: 2, unfreeze: 1 }
    cardReader.value.cardStatus = actionMap[action] ?? 1
    setTimeout(() => { showCardModal.value = false }, 800)
  } catch (err: any) {
    cardActionMsg.value = err.message || 'Action failed'
  } finally {
    cardActionLoading.value = false
  }
}

// ===== Add Reader =====
function openAddModal() {
  addForm.value = { realName: '', email: '', phone: '', readerTypeId: 1, remark: '' }
  submitError.value = ''
  showAddModal.value = true
}

async function submitAdd() {
  if (!addForm.value.realName.trim()) {
    submitError.value = 'Name is required'
    return
  }
  saving.value = true
  submitError.value = ''
  try {
    await createReader(addForm.value)
    showAddModal.value = false
    currentPage.value = 1
    loadReaders()
  } catch (err: any) {
    submitError.value = err.message || 'Failed to create reader'
  } finally {
    saving.value = false
  }
}

// ===== Edit Reader =====
function openEditModal(reader: ReaderItem) {
  editForm.value = {
    id: reader.id,
    realName: reader.realName || '',
    email: reader.email || '',
    phone: reader.phone || '',
    readerTypeId: reader.readerTypeId || 1,
  }
  editError.value = ''
  showEditModal.value = true
}

async function submitEdit() {
  if (!editForm.value.realName.trim()) {
    editError.value = 'Name is required'
    return
  }
  editLoading.value = true
  editError.value = ''
  try {
    await updateReader(editForm.value.id, editForm.value)
    showEditModal.value = false
    loadReaders()
  } catch (err: any) {
    editError.value = err.message || 'Failed to update reader'
  } finally {
    editLoading.value = false
  }
}

onMounted(() => {
  loadReaders()
  loadReaderTypes()
})
</script>

<template>
  <div class="admin-readers">
    <!-- Sidebar -->

    <!-- Main -->
    <main class="main">
      <header class="header">
        <h1 class="header__title">Readers</h1>
        <button class="btn-add" @click="openAddModal">
          <span class="btn-add__icon">+</span><span>Add New Reader</span>
        </button>
      </header>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input v-model="keyword" class="search-input" placeholder="Search by name, reader no or phone..." @click="showAdvancedSearch = true" @keyup.enter="onSearch" />
          <button class="search-btn" @click="onSearch">Search</button>
        </div>
        <div class="filter-dropdown" @click="typeDropdownOpen = !typeDropdownOpen; statusDropdownOpen = false">
          <span>{{ selectedTypeId ? (readerTypes.find(t => t.id === selectedTypeId)?.name || 'All Types') : 'All Types' }}</span>
          <span class="filter-arrow">▼</span>
          <div v-if="typeDropdownOpen" class="dropdown-menu">
            <div class="dropdown-item" :class="{ 'dropdown-item--active': !selectedTypeId }" @click.stop="selectedTypeId = undefined; typeDropdownOpen = false; onSearch()">All Types</div>
            <div v-for="t in readerTypes" :key="t.id"
              class="dropdown-item"
              :class="{ 'dropdown-item--active': selectedTypeId === t.id }"
              @click.stop="selectedTypeId = t.id; typeDropdownOpen = false; onSearch()">{{ t.name }}</div>
          </div>
        </div>
        <div class="filter-dropdown" @click="statusDropdownOpen = !statusDropdownOpen; typeDropdownOpen = false">
          <span>{{ statusFilterOptions.find(o => o.value === selectedCardStatus)?.label || 'Card Status' }}</span>
          <span class="filter-arrow">▼</span>
          <div v-if="statusDropdownOpen" class="dropdown-menu">
            <div v-for="opt in statusFilterOptions" :key="String(opt.value)"
              class="dropdown-item"
              :class="{ 'dropdown-item--active': selectedCardStatus === opt.value }"
              @click.stop="selectedCardStatus = opt.value; statusDropdownOpen = false; onSearch()">{{ opt.label }}</div>
          </div>
        </div>
      </div>

      <div v-if="showAdvancedSearch" class="advanced-search">
        <div class="adv-row">
          <div class="adv-field"><label>Reader No</label><input v-model="advSearch.readerNo" placeholder="Reader No" @keyup.enter="onSearch" /></div>
          <div class="adv-field"><label>Email</label><input v-model="advSearch.email" placeholder="Email" @keyup.enter="onSearch" /></div>
          <div class="adv-field"><label>Register Date</label></div>
        </div>
        <div class="adv-row">
          <div class="adv-field"><label>From</label><input v-model="advSearch.registerDateStart" type="date" /></div>
          <div class="adv-field"><label>To</label><input v-model="advSearch.registerDateEnd" type="date" /></div>
          <div class="adv-field"></div>
        </div>
      </div>
      <div class="table">
        <div class="table-head">
          <span class="th" style="width:50px">Avatar</span>
          <span class="th" style="width:150px">Name</span>
          <span class="th" style="width:130px">Reader No</span>
          <span class="th" style="width:90px">Type</span>
          <span class="th" style="width:110px">Card Status</span>
          <span class="th" style="width:70px">Borrowed</span>
          <span class="th" style="width:130px">Phone</span>
          <span class="th" style="width:150px">Email</span>
          <span class="th-spacer"></span>
          <span class="th th--right" style="width:110px">Actions</span>
        </div>


        <div v-if="loading" class="table-empty">Loading...</div>
        <div v-if="!loading && filteredReaders.length === 0" class="table-empty">No readers found</div>
        <div v-for="reader in filteredReaders" :key="reader.id" class="table-row">
          <div class="td" style="width:50px"><div class="avatar-circle">{{ getInitials(reader.realName) }}</div></div>
          <span class="td td--name" style="width:150px">{{ reader.realName }}</span>
          <span class="td td--mono" style="width:130px">{{ reader.readerNo }}</span>
          <span class="td td--secondary" style="width:90px">{{ reader.readerTypeName || '-' }}</span>
          <div class="td" style="width:110px">
            <span class="status-badge" :style="{ background: getCardStatusColor(reader.cardStatus) }">{{ getCardStatusLabel(reader.cardStatus) }}</span>
          </div>
          <span class="td td--secondary" style="width:70px">{{ reader.currentBorrowed }}</span>
          <span class="td td--secondary" style="width:130px">{{ reader.phone || '-' }}</span>
          <span class="td td--secondary" style="width:150px">{{ reader.email || '-' }}</span>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:110px">
            <button class="btn-action btn-action--edit" @click="openEditModal(reader)">Edit</button>
            <button class="btn-action btn-action--card" @click="openCardModal(reader)">Card</button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination">
        <span class="page-info">Showing {{ filteredReaders.length }} of {{ total }} results</span>
        <div class="page-buttons">
          <span class="page-prev" :class="{ 'page--disabled': currentPage <= 1 }" @click="goToPage(currentPage - 1)">←</span>
          <template v-for="p in visiblePages" :key="p">
            <div v-if="typeof p === 'number'" :class="['page-num', { 'page-num--active': p === currentPage }]" @click="goToPage(p)">{{ p }}</div>
            <span v-else class="page-ellipsis">...</span>
          </template>
          <span class="page-next" :class="{ 'page--disabled': currentPage >= totalPages }" @click="goToPage(currentPage + 1)">→</span>
        </div>
      </div>
    </main>

    <!-- ===== Card Action Modal ===== -->
    <div v-if="showCardModal" class="modal-overlay" @click.self="showCardModal = false">
      <div class="modal modal--sm">
        <div class="modal__header">
          <h2 class="modal__title">Card Management</h2>
          <button class="modal__close" @click="showCardModal = false">✕</button>
        </div>
        <div class="modal__body">
          <div v-if="cardReader" class="reader-info-card">
            <p class="reader-info-name">{{ cardReader.realName }}</p>
            <p class="reader-info-detail">{{ cardReader.readerNo }} · {{ cardReader.readerTypeName }} · Current: {{ cardReader.currentBorrowed }} books</p>
            <span class="reader-info-badge" :style="{ background: getCardStatusColor(cardReader.cardStatus) }">{{ getCardStatusLabel(cardReader.cardStatus) }}</span>
          </div>
          <div v-if="cardActionMsg" class="msg" :class="cardActionMsg.includes('updated') ? 'msg--success' : 'msg--error'">{{ cardActionMsg }}</div>
          <div class="card-actions">
            <button class="card-action-btn" :disabled="cardActionLoading" @click="handleCardAction('lost')">
              <span class="card-action-icon" style="color:var(--danger,#F87171)">🔴</span>
              <span>Report Lost</span>
            </button>
            <button class="card-action-btn" :disabled="cardActionLoading" @click="handleCardAction('restore')">
              <span class="card-action-icon" style="color:var(--success,#34D399)">🟢</span>
              <span>Restore Card</span>
            </button>
            <button class="card-action-btn" :disabled="cardActionLoading" @click="handleCardAction('freeze')">
              <span class="card-action-icon" style="color:var(--text-muted,#888)">⏸️</span>
              <span>Freeze Card</span>
            </button>
            <button class="card-action-btn" :disabled="cardActionLoading" @click="handleCardAction('unfreeze')">
              <span class="card-action-icon" style="color:var(--accent,#4A9FD8)">▶️</span>
              <span>Unfreeze Card</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== Add Reader Modal ===== -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal">
        <div class="modal__header">
          <h2 class="modal__title">Add New Reader</h2>
          <button class="modal__close" @click="showAddModal = false">✕</button>
        </div>
        <div v-if="submitError" class="modal__error">{{ submitError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field">
              <label class="field-label">Name *</label>
              <div class="input-box"><input v-model="addForm.realName" type="text" placeholder="Full name" /></div>
            </div>
            <div class="field">
              <label class="field-label">Reader Type</label>
              <div class="input-box select-box">
                <select v-model="addForm.readerTypeId">
                  <option v-for="t in readerTypes" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
                <span class="select-arrow">▼</span>
              </div>
            </div>
          </div>
          <div class="form-row-2">
            <div class="field">
              <label class="field-label">Email</label>
              <div class="input-box"><input v-model="addForm.email" type="email" placeholder="Email" /></div>
            </div>
            <div class="field">
              <label class="field-label">Phone</label>
              <div class="input-box"><input v-model="addForm.phone" type="tel" placeholder="Phone" /></div>
            </div>
          </div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showAddModal = false">Cancel</button>
          <button class="btn-primary" :disabled="saving" @click="submitAdd">
            <span v-if="saving" class="spinner"></span><span v-else>Create Reader</span>
          </button>
        </div>
      </div>
    </div>

    <!-- ===== Edit Reader Modal ===== -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal">
        <div class="modal__header">
          <h2 class="modal__title">Edit Reader</h2>
          <button class="modal__close" @click="showEditModal = false">✕</button>
        </div>
        <div v-if="editError" class="modal__error">{{ editError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field">
              <label class="field-label">Name</label>
              <div class="input-box"><input v-model="editForm.realName" type="text" placeholder="Full name" /></div>
            </div>
            <div class="field">
              <label class="field-label">Reader Type</label>
              <div class="input-box select-box">
                <select v-model="editForm.readerTypeId">
                  <option v-for="t in readerTypes" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
                <span class="select-arrow">▼</span>
              </div>
            </div>
          </div>
          <div class="form-row-2">
            <div class="field">
              <label class="field-label">Email</label>
              <div class="input-box"><input v-model="editForm.email" type="email" placeholder="Email" /></div>
            </div>
            <div class="field">
              <label class="field-label">Phone</label>
              <div class="input-box"><input v-model="editForm.phone" type="tel" placeholder="Phone" /></div>
            </div>
          </div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showEditModal = false">Cancel</button>
          <button class="btn-primary" :disabled="editLoading" @click="submitEdit">
            <span v-if="editLoading" class="spinner"></span><span v-else>Save Changes</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-readers { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary, #F7F8FA); }

/* Sidebar */
.sidebar { width: 240px; background: var(--bg-inverse, #0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse, #FFF); }
.sidebar__logo-text { font-family: var(--font-sans, Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-muted, #888); transition: background 0.15s; }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item--active { background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-weight: 600; }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-inverse, #FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light, #E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent, #4A9FD8); flex-shrink: 0; }

/* Main */
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans, Inter); font-size: 24px; font-weight: 700; color: var(--text-primary, #1A1A1A); margin: 0; }
.btn-add { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; }
.btn-add:hover { opacity: 0.9; }
.btn-add__icon { font-size: 16px; line-height: 1; }

/* Toolbar */
.toolbar { display: flex; gap: 12px; align-items: center; position: relative; }
.search-bar { display: flex; align-items: center; gap: 8px; width: 480px; padding: 4px 6px 4px 20px; border-radius: 999px; background: var(--bg-secondary, #F7F8FA); border: 1.5px solid var(--border, #E5E7EB); transition: border-color 0.15s; }
.search-bar .search-icon { font-size: 16px; color: var(--text-muted, #888); flex-shrink: 0; }
.search-bar .search-input { flex: 1; height: 36px; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 14px; color: var(--text-primary, #1A1A1A); }
.search-bar .search-input::placeholder { color: var(--text-muted, #888); }
.search-bar:focus-within { border-color: var(--accent, #4A9FD8); }
.search-bar .search-btn { padding: 10px 20px; border-radius: 999px; background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 600; border: none; cursor: pointer; white-space: nowrap; flex-shrink: 0; transition: opacity 0.15s; }
.search-bar .search-btn:hover { opacity: 0.9; }

.filter-dropdown { position: relative; display: flex; align-items: center; justify-content: space-between; width: 140px; padding: 10px 14px; border-radius: 10px; background: var(--bg-primary, #FFF); border: 1.5px solid var(--border, #E5E7EB); font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-secondary, #666); cursor: pointer; user-select: none; }
.filter-arrow { font-size: 10px; color: var(--text-muted, #888); }
.dropdown-menu { position: absolute; top: calc(100% + 4px); left: 0; width: 200px; max-height: 280px; overflow-y: auto; background: var(--bg-primary, #FFF); border-radius: 12px; border: 1px solid var(--border, #E5E7EB); box-shadow: 0 4px 16px rgba(0,0,0,0.08); z-index: 100; padding: 4px; }
.dropdown-item { padding: 8px 14px; border-radius: 8px; font-size: 13px; color: var(--text-secondary, #666); cursor: pointer; transition: background 0.1s; }
.dropdown-item:hover { background: var(--bg-secondary, #F7F8FA); }
.dropdown-item--active { background: var(--accent-light, #E8F4FD); color: var(--accent, #4A9FD8); font-weight: 500; }

/* Table */
.table { background: var(--bg-primary, #FFF); border-radius: var(--card-radius, 16px); border: 1px solid var(--border, #E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 14px 20px; background: var(--bg-secondary, #F7F8FA); }
.th { font-family: var(--font-sans, Inter); font-size: 12px; font-weight: 600; color: var(--text-muted, #888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; align-items: center; padding: 12px 20px; min-height: 72px; border-top: 0.5px solid var(--border, #E5E7EB); }
.table-empty { padding: 40px 20px; text-align: center; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-muted, #888); }
.td { flex-shrink: 0; display: flex; align-items: center; }
.td--name { font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 500; color: var(--text-primary, #1A1A1A); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--secondary { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-secondary, #666); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--mono { font-family: var(--font-mono, 'Geist Mono', monospace); font-size: 12px; color: var(--text-secondary, #666); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 4px; justify-content: flex-end; }

.avatar-circle { width: 36px; height: 36px; border-radius: 999px; background: var(--accent-light, #E8F4FD); display: flex; align-items: center; justify-content: center; font-family: var(--font-sans, Inter); font-size: 12px; font-weight: 600; color: var(--accent, #4A9FD8); }
.status-badge { padding: 4px 10px; border-radius: 999px; font-family: var(--font-sans, Inter); font-size: 11px; font-weight: 500; color: var(--text-inverse, #FFF); white-space: nowrap; }

.btn-action { padding: 5px 8px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans, Inter); font-size: 10px; font-weight: 500; transition: opacity 0.15s; }
.btn-action:hover { opacity: 0.8; }
.btn-action--edit { background: var(--accent-light, #E8F4FD); color: var(--accent, #4A9FD8); }
.btn-action--card { background: rgba(74, 159, 216, 0.08); color: var(--accent, #4A9FD8); }

/* Pagination */
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-muted, #888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--accent, #4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted, #888); cursor: default; pointer-events: none; }
.page-num { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-secondary, #666); cursor: pointer; background: var(--bg-primary, #FFF); border: 1px solid var(--border, #E5E7EB); }
.page-num--active { background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-weight: 600; border-color: var(--accent, #4A9FD8); }
.page-ellipsis { font-size: 12px; color: var(--text-muted, #888); padding: 0 4px; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 520px; background: var(--bg-primary, #FFF); border-radius: var(--card-radius, 16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal--sm { max-width: 380px; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans, Inter); font-size: 20px; font-weight: 600; color: var(--text-primary, #1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary, #F7F8FA); border: none; font-size: 14px; color: var(--text-muted, #888); cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.modal__close:hover { background: var(--border, #E5E7EB); }
.modal__error { margin: 12px 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger, #F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }

.msg { padding: 10px 14px; border-radius: 10px; font-size: 13px; }
.msg--success { background: rgba(52,211,153,0.1); color: var(--success, #34D399); border: 1px solid var(--success, #34D399); }
.msg--error { background: rgba(248,113,113,0.1); color: var(--danger, #F87171); border: 1px solid var(--danger, #F87171); }

/* Reader Info Card */
.reader-info-card { display: flex; flex-direction: column; gap: 6px; padding: 14px; background: var(--bg-secondary, #F7F8FA); border-radius: 10px; }
.reader-info-name { font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; color: var(--text-primary, #1A1A1A); margin: 0; }
.reader-info-detail { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-muted, #888); margin: 0; }
.reader-info-badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; font-family: var(--font-sans, Inter); font-size: 11px; font-weight: 500; color: var(--text-inverse, #FFF); width: fit-content; }

/* Card Actions */
.card-actions { display: flex; flex-direction: column; gap: 8px; }
.card-action-btn { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 12px; background: var(--bg-primary, #FFF); border: 1px solid var(--border, #E5E7EB); cursor: pointer; font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 500; color: var(--text-primary, #1A1A1A); transition: box-shadow 0.15s; text-align: left; width: 100%; }
.card-action-btn:hover:not(:disabled) { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.card-action-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.card-action-icon { font-size: 18px; line-height: 1; }

/* Form */
.field { display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0; }
.field-label { font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 500; color: var(--text-primary, #1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius, 12px); background: var(--bg-secondary, #F7F8FA); border: 1.5px solid var(--border, #E5E7EB); transition: border-color 0.2s; }
.input-box:focus-within { border-color: var(--accent, #4A9FD8); border-width: 2px; }
.input-box input, .input-box select { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-primary, #1A1A1A); }
.input-box input::placeholder { color: var(--text-muted, #888); }
.select-box { position: relative; padding: 0; }
.select-box select { width: 100%; padding: 10px 14px; cursor: pointer; appearance: none; -webkit-appearance: none; }
.select-arrow { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); font-size: 10px; color: var(--text-muted, #888); pointer-events: none; }
.form-row-2 { display: flex; gap: 12px; }

/* Buttons */
.btn-primary { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius, 10px); background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; min-width: 120px; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius, 10px); background: var(--bg-primary, #FFF); color: var(--text-secondary, #666); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border, #E5E7EB); cursor: pointer; transition: background 0.15s; }
.btn-cancel:hover { background: var(--bg-secondary, #F7F8FA); }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse, #FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.toolbar-actions { display: flex; align-items: center; gap: 8px; margin-left: auto; }
.advanced-search { background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.adv-row { display: flex; gap: 16px; }
.adv-field { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.adv-field label { font-size: 11px; color: var(--text-muted,#888); font-weight: 500; }
.adv-field input, .adv-field select { padding: 8px 10px; border-radius: 8px; border: 1.5px solid var(--border,#E5E7EB); background: var(--bg-secondary,#F7F8FA); font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-primary,#1A1A1A); outline: none; }
.adv-field input:focus, .adv-field select:focus { border-color: var(--accent,#4A9FD8); }
.adv-actions { display: flex; gap: 8px; }
</style>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listAdminBooks, deleteBook, createBook, updateBook, getCategoryTree } from '../api/books'
import type { BookItem } from '../api/books'

const router = useRouter()

const adminNav = [
  { icon: '📊', label: 'Dashboard' },
  { icon: '📖', label: 'Borrow/Return' },
  { icon: '📚', label: 'Books' },
  { icon: '👥', label: 'Readers' },
  { icon: '📈', label: 'Statistics' },
  { icon: '💰', label: 'Fines' },
  { icon: '⚙️', label: 'Settings' },
]
const realName = ref(localStorage.getItem('realName') || 'Admin')
const userInitials = ref(realName.value.charAt(0).toUpperCase())

const keyword = ref('')
const books = ref<BookItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 8
const loading = ref(false)

const categories = ref<{ id: number; name: string }[]>([])
const selectedCategoryId = ref<number | null>(null)
const categoryDropdownOpen = ref(false)

const statusOptions = [
  { value: '', label: 'All Status' },
  { value: 'available', label: 'Available' },
  { value: 'borrowed', label: 'Borrowed' },
]
const selectedStatus = ref('')
const statusDropdownOpen = ref(false)

// Add Modal
const showAddModal = ref(false)
const saving = ref(false)
const submitError = ref('')
const addForm = ref({ title: '', author: '', isbn: '', categoryId: null as number | null, publishDate: '', price: null as number | null, pages: null as number | null, binding: '', language: '中文', summary: '' })

// Edit Modal
const showEditModal = ref(false)
const editLoading = ref(false)
const editError = ref('')
const editForm = ref({ id: 0, title: '', author: '', isbn: '', categoryId: null as number | null, publishDate: '', price: null as number | null, pages: null as number | null, binding: '', language: '中文', summary: '' })

const filteredBooks = computed(() => {
  let result = books.value
  if (selectedCategoryId.value) result = result.filter(b => b.categoryId === selectedCategoryId.value)
  if (selectedStatus.value === 'available') result = result.filter(b => (b.availableCopies ?? 0) > 0)
  else if (selectedStatus.value === 'borrowed') result = result.filter(b => (b.availableCopies ?? 0) === 0)
  return result
})

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  if (totalPages.value <= 5) { for (let i = 1; i <= totalPages.value; i++) pages.push(i) }
  else {
    pages.push(1); const s = Math.max(2, currentPage.value - 1); const e = Math.min(totalPages.value - 1, currentPage.value + 1)
    if (s > 2) pages.push('...'); for (let i = s; i <= e; i++) pages.push(i); if (e < totalPages.value - 1) pages.push('...'); pages.push(totalPages.value)
  }
  return pages
})

async function loadBooks() {
  loading.value = true
  try {
    const r = await listAdminBooks(keyword.value || undefined, currentPage.value, pageSize)
    books.value = r.records; total.value = r.total; totalPages.value = r.pages || 1
  } catch { books.value = [] } finally { loading.value = false }
}

async function loadCategories() {
  try { const t = await getCategoryTree(); categories.value = t.map((c: any) => ({ id: c.id, name: c.name })) } catch { categories.value = [] }
}

function onSearch() { currentPage.value = 1; loadBooks() }
function goToPage(p: number) { if (p < 1 || p > totalPages.value) return; currentPage.value = p; loadBooks() }

function navigateTo(label: string) {
  if (label === 'Dashboard') router.push('/admin')
  else if (label === 'Books') router.push('/admin/books')
}

function getStatusLabel(book: BookItem) { return (book.availableCopies ?? 0) > 0 ? 'Available' : 'Borrowed' }
function getStatusColor(book: BookItem) { return (book.availableCopies ?? 0) > 0 ? 'var(--success,#34D399)' : 'var(--warning,#FBBF24)' }

function editBook(book: BookItem) {
  editForm.value = { id: book.id, title: book.title, author: book.author, isbn: book.isbn || '', categoryId: book.categoryId || null, publishDate: book.publishDate || '', price: book.price, pages: book.pages || null, binding: book.binding || '', language: book.language || '中文', summary: book.summary || '' }
  editError.value = ''; showEditModal.value = true
}

async function handleDelete(id: number, title: string) {
  if (!confirm(`Delete "${title}"?`)) return
  try { await deleteBook(id); loadBooks() } catch { alert('Delete failed') }
}

async function submitEdit() {
  if (!editForm.value.title.trim() || !editForm.value.author.trim()) { editError.value = 'Title and Author are required'; return }
  editLoading.value = true; editError.value = ''
  try { await updateBook(editForm.value.id, editForm.value as any); showEditModal.value = false; loadBooks() } catch (err: any) { editError.value = err.message || 'Update failed' } finally { editLoading.value = false }
}

function openAddModal() {
  addForm.value = { title: '', author: '', isbn: '', categoryId: null, publishDate: '', price: null, pages: null, binding: '', language: '中文', summary: '' }
  submitError.value = ''; showAddModal.value = true
}

async function submitAddBook() {
  if (!addForm.value.title.trim() || !addForm.value.author.trim()) { submitError.value = 'Title and Author are required'; return }
  saving.value = true; submitError.value = ''
  try { await createBook(addForm.value as any); showAddModal.value = false; currentPage.value = 1; loadBooks() } catch (err: any) { submitError.value = err.message || 'Create failed' } finally { saving.value = false }
}

function selectCategory(id: number | null) { selectedCategoryId.value = id; categoryDropdownOpen.value = false }
function selectStatus(val: string) { selectedStatus.value = val; statusDropdownOpen.value = false }
function toggleCatDropdown() { categoryDropdownOpen.value = !categoryDropdownOpen.value; statusDropdownOpen.value = false }
function toggleStatusDropdown() { statusDropdownOpen.value = !statusDropdownOpen.value; categoryDropdownOpen.value = false }
function getSelectedCategoryName() { if (!selectedCategoryId.value) return 'All Categories'; const c = categories.value.find(c => c.id === selectedCategoryId.value); return c ? c.name : 'All Categories' }

onMounted(() => { loadBooks(); loadCategories() })
</script>

<template>
  <div class="admin-books">
    <aside class="sidebar">
      <div class="sidebar__logo"><span>📚</span><span class="sidebar__logo-text">LibraryOS</span></div>
      <nav class="sidebar__nav">
        <div v-for="item in adminNav" :key="item.label" class="sidebar__item" :class="{ 'sidebar__item--active': item.label === 'Books' }" @click="navigateTo(item.label)"><span class="sidebar__item-icon">{{ item.icon }}</span><span>{{ item.label }}</span></div>
      </nav>
      <div class="sidebar__bottom"><div class="sidebar__avatar">{{ userInitials }}</div><span>{{ realName }}</span></div>
    </aside>

    <main class="main">
      <header class="header"><h1 class="header__title">Books</h1><button class="btn-add" @click="openAddModal"><span class="btn-add__icon">+</span><span>Add New Book</span></button></header>

      <div class="toolbar">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="keyword" class="search-input" placeholder="Search by title, author or ISBN..." @keyup.enter="onSearch" /></div>
        <div class="filter-dropdown" @click="toggleCatDropdown"><span>{{ getSelectedCategoryName() }}</span><span class="filter-arrow">▼</span>
          <div v-if="categoryDropdownOpen" class="dropdown-menu"><div class="dropdown-item" @click.stop="selectCategory(null)">All Categories</div><div v-for="cat in categories" :key="cat.id" class="dropdown-item" :class="{ 'dropdown-item--active': selectedCategoryId === cat.id }" @click.stop="selectCategory(cat.id)">{{ cat.name }}</div></div>
        </div>
        <div class="filter-dropdown" @click="toggleStatusDropdown"><span>{{ selectedStatus ? statusOptions.find(o => o.value === selectedStatus)?.label : 'All Status' }}</span><span class="filter-arrow">▼</span>
          <div v-if="statusDropdownOpen" class="dropdown-menu"><div v-for="opt in statusOptions" :key="opt.value" class="dropdown-item" :class="{ 'dropdown-item--active': selectedStatus === opt.value }" @click.stop="selectStatus(opt.value)">{{ opt.label }}</div></div>
        </div>
      </div>

      <div class="table">
        <div class="table-head"><span class="th" style="width:48px">Cover</span><span class="th" style="width:220px">Title</span><span class="th" style="width:160px">Author</span><span class="th" style="width:180px">ISBN</span><span class="th" style="width:120px">Category</span><span class="th" style="width:70px">Copies</span><span class="th" style="width:100px">Status</span><span class="th-spacer"></span><span class="th th--right" style="width:100px">Actions</span></div>
        <div v-if="loading" class="table-empty">Loading...</div>
        <div v-if="!loading && filteredBooks.length === 0" class="table-empty">No books found</div>
        <div v-for="book in filteredBooks" :key="book.id" class="table-row">
          <div class="td" style="width:48px"><div class="cover-thumb">📖</div></div>
          <span class="td td--title" style="width:220px">{{ book.title }}</span>
          <span class="td td--secondary" style="width:160px">{{ book.author }}</span>
          <span class="td td--mono td--muted" style="width:180px">{{ book.isbn }}</span>
          <span class="td td--secondary" style="width:120px">{{ book.categoryName || '—' }}</span>
          <span class="td td--secondary" style="width:70px">{{ book.availableCopies }}/{{ book.totalCopies }}</span>
          <div class="td" style="width:100px"><span class="status-badge" :style="{ background: getStatusColor(book) }">{{ getStatusLabel(book) }}</span></div>
          <div class="td-spacer"></div>
          <div class="td td--actions" style="width:100px">
            <button class="btn-edit" @click="editBook(book)">Edit</button>
            <button class="btn-del" @click="handleDelete(book.id, book.title)">Del</button>
          </div>
        </div>
      </div>

      <div class="pagination">
        <span class="page-info">Showing {{ filteredBooks.length }} of {{ total }} results</span>
        <div class="page-buttons">
          <span class="page-prev" :class="{ 'page--disabled': currentPage <= 1 }" @click="goToPage(currentPage - 1)">←</span>
          <template v-for="p in visiblePages" :key="p"><div v-if="typeof p === 'number'" :class="['page-num', { 'page-num--active': p === currentPage }]" @click="goToPage(p)">{{ p }}</div><span v-else class="page-ellipsis">...</span></template>
          <span class="page-next" :class="{ 'page--disabled': currentPage >= totalPages }" @click="goToPage(currentPage + 1)">→</span>
        </div>
      </div>
    </main>

    <!-- Add Book Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal">
        <div class="modal__header"><h2 class="modal__title">Add New Book</h2><button class="modal__close" @click="showAddModal = false">✕</button></div>
        <div v-if="submitError" class="modal__error">{{ submitError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">Title *</label><div class="input-box"><input v-model="addForm.title" type="text" placeholder="Book title" /></div></div>
            <div class="field"><label class="field-label">Author *</label><div class="input-box"><input v-model="addForm.author" type="text" placeholder="Author name" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">ISBN</label><div class="input-box"><input v-model="addForm.isbn" type="text" placeholder="978-xxx" /></div></div>
            <div class="field"><label class="field-label">Category</label><div class="input-box select-box"><select v-model="addForm.categoryId"><option :value="null">Select category</option><option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">Publish Date</label><div class="input-box"><input v-model="addForm.publishDate" type="date" /></div></div>
            <div class="field"><label class="field-label">Price</label><div class="input-box"><input v-model.number="addForm.price" type="number" step="0.01" placeholder="0.00" /></div></div>
          </div>
          <div class="form-row-3">
            <div class="field"><label class="field-label">Pages</label><div class="input-box"><input v-model.number="addForm.pages" type="number" placeholder="0" /></div></div>
            <div class="field"><label class="field-label">Binding</label><div class="input-box"><input v-model="addForm.binding" type="text" placeholder="平装/精装" /></div></div>
            <div class="field"><label class="field-label">Language</label><div class="input-box select-box"><select v-model="addForm.language"><option value="中文">中文</option><option value="英文">英文</option><option value="中英双语">中英双语</option><option value="日文">日文</option><option value="其他">其他</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="field"><label class="field-label">Summary</label><div class="input-box"><textarea v-model="addForm.summary" class="input-textarea" placeholder="Book description..." rows="3"></textarea></div></div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showAddModal = false">Cancel</button>
          <button class="btn-primary" :disabled="saving" @click="submitAddBook"><span v-if="saving" class="spinner"></span><span v-else>Create Book</span></button>
        </div>
      </div>
    </div>

    <!-- Edit Book Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal">
        <div class="modal__header"><h2 class="modal__title">Edit Book</h2><button class="modal__close" @click="showEditModal = false">✕</button></div>
        <div v-if="editError" class="modal__error">{{ editError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">Title *</label><div class="input-box"><input v-model="editForm.title" type="text" placeholder="Book title" /></div></div>
            <div class="field"><label class="field-label">Author *</label><div class="input-box"><input v-model="editForm.author" type="text" placeholder="Author name" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">ISBN</label><div class="input-box"><input v-model="editForm.isbn" type="text" placeholder="978-xxx" /></div></div>
            <div class="field"><label class="field-label">Category</label><div class="input-box select-box"><select v-model="editForm.categoryId"><option :value="null">Select category</option><option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">Publish Date</label><div class="input-box"><input v-model="editForm.publishDate" type="date" /></div></div>
            <div class="field"><label class="field-label">Price</label><div class="input-box"><input v-model.number="editForm.price" type="number" step="0.01" placeholder="0.00" /></div></div>
          </div>
          <div class="form-row-3">
            <div class="field"><label class="field-label">Pages</label><div class="input-box"><input v-model.number="editForm.pages" type="number" placeholder="0" /></div></div>
            <div class="field"><label class="field-label">Binding</label><div class="input-box"><input v-model="editForm.binding" type="text" placeholder="平装/精装" /></div></div>
            <div class="field"><label class="field-label">Language</label><div class="input-box select-box"><select v-model="editForm.language"><option value="中文">中文</option><option value="英文">英文</option><option value="中英双语">中英双语</option><option value="日文">日文</option><option value="其他">其他</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="field"><label class="field-label">Summary</label><div class="input-box"><textarea v-model="editForm.summary" class="input-textarea" placeholder="Book description..." rows="3"></textarea></div></div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showEditModal = false">Cancel</button>
          <button class="btn-primary" :disabled="editLoading" @click="submitEdit"><span v-if="editLoading" class="spinner"></span><span v-else>Save Changes</span></button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-books { display: flex; min-height: 100vh; background: var(--bg-secondary, #F7F8FA); }
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
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans, Inter); font-size: 24px; font-weight: 700; color: var(--text-primary, #1A1A1A); margin: 0; }
.btn-add { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; }
.btn-add:hover { opacity: 0.9; }
.btn-add__icon { font-size: 16px; line-height: 1; }
.toolbar { display: flex; gap: 12px; align-items: center; position: relative; }
.search-box { display: flex; align-items: center; gap: 8px; width: 360px; padding: 10px 16px; border-radius: var(--input-radius, 12px); background: var(--bg-primary, #FFF); border: 1.5px solid var(--border, #E5E7EB); }
.search-icon { font-size: 14px; line-height: 1; color: var(--text-muted, #888); }
.search-input { flex: 1; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-primary, #1A1A1A); }
.search-input::placeholder { color: var(--text-muted, #888); }
.filter-dropdown { position: relative; display: flex; align-items: center; justify-content: space-between; width: 160px; padding: 10px 14px; border-radius: 10px; background: var(--bg-primary, #FFF); border: 1.5px solid var(--border, #E5E7EB); font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-secondary, #666); cursor: pointer; user-select: none; }
.filter-arrow { font-size: 10px; color: var(--text-muted, #888); }
.dropdown-menu { position: absolute; top: calc(100% + 4px); left: 0; width: 220px; max-height: 300px; overflow-y: auto; background: var(--bg-primary, #FFF); border-radius: 12px; border: 1px solid var(--border, #E5E7EB); box-shadow: 0 4px 16px rgba(0,0,0,0.08); z-index: 100; padding: 4px; }
.dropdown-item { padding: 8px 14px; border-radius: 8px; font-size: 13px; color: var(--text-secondary, #666); cursor: pointer; transition: background 0.1s; }
.dropdown-item:hover { background: var(--bg-secondary, #F7F8FA); }
.dropdown-item--active { background: var(--accent-light, #E8F4FD); color: var(--accent, #4A9FD8); font-weight: 500; }
.table { background: var(--bg-primary, #FFF); border-radius: var(--card-radius, 16px); border: 1px solid var(--border, #E5E7EB); display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.table-head { display: flex; align-items: center; padding: 14px 20px; background: var(--bg-secondary, #F7F8FA); }
.th { font-family: var(--font-sans, Inter); font-size: 12px; font-weight: 600; color: var(--text-muted, #888); flex-shrink: 0; }
.th--right { text-align: right; }
.th-spacer { flex: 1; }
.table-row { display: flex; align-items: center; padding: 12px 20px; min-height: 72px; border-top: 0.5px solid var(--border, #E5E7EB); }
.table-empty { padding: 40px 20px; text-align: center; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-muted, #888); }
.td { flex-shrink: 0; display: flex; align-items: center; }
.td--title { font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 500; color: var(--text-primary, #1A1A1A); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--secondary { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-secondary, #666); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--mono { font-family: var(--font-mono, 'Geist Mono', monospace); }
.td--muted { color: var(--text-muted, #888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 8px; justify-content: flex-end; }
.cover-thumb { width: 36px; height: 48px; border-radius: 6px; background: var(--accent-light, #E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 16px; line-height: 1; }
.status-badge { padding: 4px 10px; border-radius: 999px; font-family: var(--font-sans, Inter); font-size: 11px; font-weight: 500; color: var(--text-inverse, #FFF); white-space: nowrap; }
.btn-edit { padding: 6px 12px; border-radius: 6px; background: var(--accent-light, #E8F4FD); font-family: var(--font-sans, Inter); font-size: 11px; font-weight: 500; color: var(--accent, #4A9FD8); border: none; cursor: pointer; }
.btn-edit:hover { opacity: 0.8; }
.btn-del { padding: 6px 12px; border-radius: 6px; background: rgba(248, 113, 113, 0.1); font-family: var(--font-sans, Inter); font-size: 11px; font-weight: 500; color: var(--danger, #F87171); border: none; cursor: pointer; }
.btn-del:hover { opacity: 0.8; }
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.page-info { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-muted, #888); }
.page-buttons { display: flex; gap: 4px; align-items: center; }
.page-prev, .page-next { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--accent, #4A9FD8); cursor: pointer; padding: 0 4px; }
.page--disabled { color: var(--text-muted, #888); cursor: default; pointer-events: none; }
.page-num { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-secondary, #666); cursor: pointer; background: var(--bg-primary, #FFF); border: 1px solid var(--border, #E5E7EB); }
.page-num--active { background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-weight: 600; border-color: var(--accent, #4A9FD8); }
.page-ellipsis { font-size: 12px; color: var(--text-muted, #888); padding: 0 4px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 560px; background: var(--bg-primary, #FFF); border-radius: var(--card-radius, 16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans, Inter); font-size: 20px; font-weight: 600; color: var(--text-primary, #1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary, #F7F8FA); border: none; font-size: 14px; color: var(--text-muted, #888); cursor: pointer; display: flex; align-items: center; justify-content: center; }
.modal__close:hover { background: var(--border, #E5E7EB); }
.modal__error { margin: 12px 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger, #F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 500; color: var(--text-primary, #1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius, 12px); background: var(--bg-secondary, #F7F8FA); border: 1.5px solid var(--border, #E5E7EB); transition: border-color 0.2s; }
.input-box:focus-within { border-color: var(--accent, #4A9FD8); border-width: 2px; }
.input-box input, .input-box select, .input-textarea { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-primary, #1A1A1A); }
.input-box input::placeholder, .input-textarea::placeholder { color: var(--text-muted, #888); }
.input-textarea { resize: vertical; min-height: 60px; padding: 0; }
.select-box { position: relative; padding: 0; }
.select-box select { width: 100%; height: 100%; padding: 10px 14px; cursor: pointer; appearance: none; -webkit-appearance: none; }
.select-arrow { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); font-size: 10px; color: var(--text-muted, #888); pointer-events: none; }
.form-row-2 { display: flex; gap: 12px; }
.form-row-2 .field { flex: 1; min-width: 0; }
.form-row-3 { display: flex; gap: 12px; }
.form-row-3 .field { flex: 1; min-width: 0; }
.btn-primary { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius, 10px); background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; min-width: 120px; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius, 10px); background: var(--bg-primary, #FFF); color: var(--text-secondary, #666); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border, #E5E7EB); cursor: pointer; transition: background 0.15s; }
.btn-cancel:hover { background: var(--bg-secondary, #F7F8FA); }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse, #FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

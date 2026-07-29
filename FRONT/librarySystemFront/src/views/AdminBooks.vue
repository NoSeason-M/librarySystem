<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listAdminBooks, deleteBook, createBook, updateBook, getCategoryTree, uploadCover } from '../api/books'
import type { BookItem } from '../api/books'

const router = useRouter()

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
  { value: '', label: '全部状态' },
  { value: 'available', label: '可借' },
  { value: 'borrowed', label: '已借出' },
]
const selectedStatus = ref('')
const showAdvancedSearch = ref(false)
const advSearch = ref({ author: "", isbn: "", publisher: "", language: "", binding: "", yearStart: "", yearEnd: "" })
const statusDropdownOpen = ref(false)

// Add Modal
const showAddModal = ref(false)
const saving = ref(false)
const submitError = ref('')
const addForm = ref({ title: '', author: '', isbn: '', categoryId: null as number | null, publishDate: '', price: null as number | null, pages: null as number | null, binding: '', language: '中文', summary: '' })
const coverFile = ref<File | null>(null)
const coverPreview = ref("")
const coverFileEdit = ref<File | null>(null)
const coverPreviewEdit = ref("")

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

onMounted(() => { loadBooks(); loadCategories() })

async function loadBooks() {
  loading.value = true
  try {
    const r = await listAdminBooks({
      keyword: keyword.value || undefined, author: advSearch.value.author || undefined,
      isbn: advSearch.value.isbn || undefined, categoryId: selectedCategoryId.value || undefined,
      publisher: advSearch.value.publisher || undefined, language: advSearch.value.language || undefined,
      binding: advSearch.value.binding || undefined, yearStart: advSearch.value.yearStart || undefined,
      yearEnd: advSearch.value.yearEnd || undefined, statusFilter: selectedStatus.value || undefined,
      page: currentPage.value, size: pageSize
    })
    books.value = r.records; total.value = r.total; totalPages.value = r.pages || 1
  } catch { books.value = [] } finally { loading.value = false }
}

async function loadCategories() {
  try { const t = await getCategoryTree(); categories.value = t.map((c: any) => ({ id: c.id, name: c.name })) } catch { categories.value = [] }
}

function clearAdvSearch() {
  advSearch.value = { author: "", isbn: "", publisher: "", language: "", binding: "", yearStart: "", yearEnd: "" }
  onSearch()
}

function onSearch() { currentPage.value = 1; loadBooks() }
function goToPage(p: number) { if (p < 1 || p > totalPages.value) return; currentPage.value = p; loadBooks() }

function getStatusLabel(book: BookItem) { return (book.availableCopies ?? 0) > 0 ? '可借' : '已借出' }
function getStatusColor(book: BookItem) { return (book.availableCopies ?? 0) > 0 ? 'var(--success,#34D399)' : 'var(--warning,#FBBF24)' }

function editBook(book: BookItem) {
  coverFileEdit.value = null; coverPreviewEdit.value = book.coverUrl || ""
  editForm.value = { id: book.id, title: book.title, author: book.author, isbn: book.isbn || '', categoryId: book.categoryId || null, publishDate: book.publishDate || '', price: book.price, pages: book.pages || null, binding: book.binding || '', language: book.language || '中文', summary: book.summary || '' }
  editError.value = ''; showEditModal.value = true
}

async function handleDelete(id: number, title: string) {
  if (!confirm(`确认删除「${title}」？`)) return
  try { await deleteBook(id); loadBooks() } catch { alert('删除失败') }
}

async function submitEdit() {
  if (!editForm.value.title.trim() || !editForm.value.author.trim()) { editError.value = '书名和作者为必填项'; return }
  editLoading.value = true; editError.value = ''
  try {
      await updateBook(editForm.value.id, editForm.value as any)
      if (coverFileEdit.value) await uploadCover(editForm.value.id, coverFileEdit.value)
      showEditModal.value = false; loadBooks()
    } catch (err: any) { editError.value = err.message || '更新失败' } finally { editLoading.value = false }
}

function openAddModal() {
  coverFile.value = null; coverPreview.value = ""
  addForm.value = { title: '', author: '', isbn: '', categoryId: null, publishDate: '', price: null, pages: null, binding: '', language: '中文', summary: '' }
  submitError.value = ''; showAddModal.value = true
}

async function submitAddBook() {
  if (!addForm.value.title.trim() || !addForm.value.author.trim()) { submitError.value = '书名和作者为必填项'; return }
  saving.value = true; submitError.value = ''
  try {
      const r = await createBook(addForm.value as any)
      if (coverFile.value) await uploadCover(r.bookId, coverFile.value)
      showAddModal.value = false; currentPage.value = 1; loadBooks()
    } catch (err: any) { submitError.value = err.message || '创建失败' } finally { saving.value = false }
}

function selectCategory(id: number | null) { selectedCategoryId.value = id; categoryDropdownOpen.value = false }
function selectStatus(val: string) { selectedStatus.value = val; statusDropdownOpen.value = false }
function toggleCatDropdown() { categoryDropdownOpen.value = !categoryDropdownOpen.value; statusDropdownOpen.value = false }
function toggleStatusDropdown() { statusDropdownOpen.value = !statusDropdownOpen.value; categoryDropdownOpen.value = false }

function handleCoverChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    coverFile.value = target.files[0]
    const reader = new FileReader()
    reader.onload = () => { coverPreview.value = reader.result as string }
    reader.readAsDataURL(target.files[0])
  }
}

function handleCoverChangeEdit(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    coverFileEdit.value = target.files[0]
    const reader = new FileReader()
    reader.onload = () => { coverPreviewEdit.value = reader.result as string }
    reader.readAsDataURL(target.files[0])
  }
}

function getSelectedCategoryName() { if (!selectedCategoryId.value) return '全部分类'; const c = categories.value.find(c => c.id === selectedCategoryId.value); return c ? c.name : '全部分类' }
</script>

<template>
  <div class="admin-books">
    <main class="main">
      <header class="header"><h1 class="header__title">图书管理</h1><button class="btn-add" @click="openAddModal"><span class="btn-add__icon">+</span><span>新增图书</span></button></header>

      <div class="toolbar">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input v-model="keyword" class="search-input" placeholder="按书名、作者或ISBN搜索..." @click="showAdvancedSearch = true" @keyup.enter="onSearch" />
          <button class="search-btn" @click="onSearch">搜索</button>
        </div>
        <div class="filter-dropdown" @click="toggleCatDropdown"><span>{{ getSelectedCategoryName() }}</span><span class="filter-arrow">▼</span>
          <div v-if="categoryDropdownOpen" class="dropdown-menu"><div class="dropdown-item" @click.stop="selectCategory(null)">全部分类</div><div v-for="cat in categories" :key="cat.id" class="dropdown-item" :class="{ 'dropdown-item--active': selectedCategoryId === cat.id }" @click.stop="selectCategory(cat.id)">{{ cat.name }}</div></div>
        </div>
        <div class="filter-dropdown" @click="toggleStatusDropdown"><span>{{ selectedStatus ? statusOptions.find(o => o.value === selectedStatus)?.label : '全部状态' }}</span><span class="filter-arrow">▼</span>
          <div v-if="statusDropdownOpen" class="dropdown-menu"><div v-for="opt in statusOptions" :key="opt.value" class="dropdown-item" :class="{ 'dropdown-item--active': selectedStatus === opt.value }" @click.stop="selectStatus(opt.value)">{{ opt.label }}</div></div>
        </div>
      </div>

      <div v-if="showAdvancedSearch" class="advanced-search">
        <div class="adv-row">
          <div class="adv-field"><label>作者</label><input v-model="advSearch.author" placeholder="作者" @keyup.enter="onSearch" /></div>
          <div class="adv-field"><label>ISBN</label><input v-model="advSearch.isbn" placeholder="ISBN" @keyup.enter="onSearch" /></div>
          <div class="adv-field"><label>出版社</label><input v-model="advSearch.publisher" placeholder="出版社" @keyup.enter="onSearch" /></div>
        </div>
        <div class="adv-row">
          <div class="adv-field"><label>语言</label>
            <select v-model="advSearch.language"><option value="">全部</option><option value="中文">中文</option><option value="英文">英文</option></select>
          </div>
          <div class="adv-field"><label>装帧</label>
            <select v-model="advSearch.binding"><option value="">全部</option><option value="平装">平装</option><option value="精装">精装</option></select>
          </div>
          <div class="adv-field"><label>年份</label>
            <div class="adv-year-range"><input v-model="advSearch.yearStart" placeholder="2020" style="width:80px" /> ~ <input v-model="advSearch.yearEnd" placeholder="2026" style="width:80px" /></div>
          </div>
        </div>
      </div>
      <div class="table">
        <div class="table-head"><span class="th" style="width:48px">封面</span><span class="th" style="width:220px">书名</span><span class="th" style="width:160px">作者</span><span class="th" style="width:180px">ISBN</span><span class="th" style="width:120px">分类</span><span class="th" style="width:70px">副本</span><span class="th" style="width:100px">状态</span><span class="th-spacer"></span><span class="th th--right" style="width:100px">操作</span></div>
        <div v-if="loading" class="table-empty">加载中...</div>
        <div v-if="!loading && filteredBooks.length === 0" class="table-empty">未找到图书</div>
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
            <button class="btn-edit" @click="editBook(book)">编辑</button>
            <button class="btn-del" @click="handleDelete(book.id, book.title)">删除</button>
          </div>
        </div>
      </div>

      <div class="pagination">
        <span class="page-info">显示 {{ filteredBooks.length }} 条，共 {{ total }} 条</span>
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
        <div class="modal__header"><h2 class="modal__title">新增图书</h2><button class="modal__close" @click="showAddModal = false">✕</button></div>
        <div v-if="submitError" class="modal__error">{{ submitError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">书名 *</label><div class="input-box"><input v-model="addForm.title" type="text" placeholder="书名" /></div></div>
            <div class="field"><label class="field-label">作者 *</label><div class="input-box"><input v-model="addForm.author" type="text" placeholder="作者" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">ISBN</label><div class="input-box"><input v-model="addForm.isbn" type="text" placeholder="978-xxx" /></div></div>
            <div class="field"><label class="field-label">分类</label><div class="input-box select-box"><select v-model="addForm.categoryId"><option :value="null">选择分类</option><option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">出版日期</label><div class="input-box"><input v-model="addForm.publishDate" type="date" /></div></div>
            <div class="field"><label class="field-label">定价</label><div class="input-box"><input v-model.number="addForm.price" type="number" step="0.01" placeholder="0.00" /></div></div>
          </div>
          <div class="form-row-3">
            <div class="field"><label class="field-label">页数</label><div class="input-box"><input v-model.number="addForm.pages" type="number" placeholder="0" /></div></div>
            <div class="field"><label class="field-label">装帧</label><div class="input-box"><input v-model="addForm.binding" type="text" placeholder="平装/精装" /></div></div>
            <div class="field"><label class="field-label">语言</label><div class="input-box select-box"><select v-model="addForm.language"><option value="中文">中文</option><option value="英文">英文</option><option value="中英双语">中英双语</option><option value="日文">日文</option><option value="其他">其他</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="field"><label class="field-label">封面图片</label>
            <div class="cover-upload">
              <div v-if="coverPreview" class="cover-preview"><img :src="coverPreview" alt="cover" /></div>
              <input type="file" accept="image/jpeg,image/png,image/webp" @change="handleCoverChange" class="cover-input" />
            </div>
          </div>
          <div class="field"><label class="field-label">简介</label><div class="input-box"><textarea v-model="addForm.summary" class="input-textarea" placeholder="图书简介..." rows="3"></textarea></div></div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showAddModal = false">取消</button>
          <button class="btn-primary" :disabled="saving" @click="submitAddBook"><span v-if="saving" class="spinner"></span><span v-else>创建图书</span></button>
        </div>
      </div>
    </div>

    <!-- Edit Book Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal">
        <div class="modal__header"><h2 class="modal__title">编辑图书</h2><button class="modal__close" @click="showEditModal = false">✕</button></div>
        <div v-if="editError" class="modal__error">{{ editError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">书名 *</label><div class="input-box"><input v-model="editForm.title" type="text" placeholder="书名" /></div></div>
            <div class="field"><label class="field-label">作者 *</label><div class="input-box"><input v-model="editForm.author" type="text" placeholder="作者" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">ISBN</label><div class="input-box"><input v-model="editForm.isbn" type="text" placeholder="978-xxx" /></div></div>
            <div class="field"><label class="field-label">分类</label><div class="input-box select-box"><select v-model="editForm.categoryId"><option :value="null">选择分类</option><option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">出版日期</label><div class="input-box"><input v-model="editForm.publishDate" type="date" /></div></div>
            <div class="field"><label class="field-label">定价</label><div class="input-box"><input v-model.number="editForm.price" type="number" step="0.01" placeholder="0.00" /></div></div>
          </div>
          <div class="form-row-3">
            <div class="field"><label class="field-label">页数</label><div class="input-box"><input v-model.number="editForm.pages" type="number" placeholder="0" /></div></div>
            <div class="field"><label class="field-label">装帧</label><div class="input-box"><input v-model="editForm.binding" type="text" placeholder="平装/精装" /></div></div>
            <div class="field"><label class="field-label">语言</label><div class="input-box select-box"><select v-model="editForm.language"><option value="中文">中文</option><option value="英文">英文</option><option value="中英双语">中英双语</option><option value="日文">日文</option><option value="其他">其他</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="field"><label class="field-label">封面图片</label>
            <div class="cover-upload">
              <div v-if="coverPreview" class="cover-preview"><img :src="coverPreview" alt="cover" /></div>
              <input type="file" accept="image/jpeg,image/png,image/webp" @change="handleCoverChange" class="cover-input" />
            </div>
          </div>
          <div class="field"><label class="field-label">简介</label><div class="input-box"><textarea v-model="editForm.summary" class="input-textarea" placeholder="图书简介..." rows="3"></textarea></div></div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showEditModal = false">取消</button>
          <button class="btn-primary" :disabled="editLoading" @click="submitEdit"><span v-if="editLoading" class="spinner"></span><span v-else>保存修改</span></button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-books { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary, #F7F8FA); }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 24px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header__title { font-family: var(--font-sans, Inter); font-size: 24px; font-weight: 700; color: var(--text-primary, #1A1A1A); margin: 0; }
.btn-add { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; }
.btn-add:hover { opacity: 0.9; }
.btn-add__icon { font-size: 16px; line-height: 1; }
.toolbar { display: flex; gap: 12px; align-items: center; position: relative; }
.search-bar { display: flex; align-items: center; gap: 8px; width: 480px; padding: 4px 6px 4px 20px; border-radius: 999px; background: var(--bg-secondary, #F7F8FA); border: 1.5px solid var(--border, #E5E7EB); transition: border-color 0.2s; }
.search-bar .search-icon { font-size: 16px; color: var(--text-muted, #888); flex-shrink: 0; }
.search-bar .search-input { flex: 1; height: 36px; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 14px; color: var(--text-primary, #1A1A1A); }
.search-bar .search-input::placeholder { color: var(--text-muted, #888); }
.search-bar:focus-within { border-color: var(--accent, #4A9FD8); }
.search-bar .search-btn { padding: 10px 20px; border-radius: 999px; background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 600; border: none; cursor: pointer; white-space: nowrap; flex-shrink: 0; transition: opacity 0.15s; }
.search-bar .search-btn:hover { opacity: 0.9; }
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
.td { flex-shrink: 0; display: flex; align-items: center; font-family: var(--font-sans, Inter); font-size: 12px; }
.td--title { font-weight: 500; color: var(--text-primary, #1A1A1A); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--secondary { color: var(--text-secondary, #666); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td--mono { font-family: var(--font-mono, 'Geist Mono', monospace); }
.td--muted { color: var(--text-muted, #888); }
.td-spacer { flex: 1; }
.td--actions { display: flex; gap: 6px; justify-content: flex-end; }
.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 500; color: var(--text-inverse, #FFF); white-space: nowrap; }
.btn-edit { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans, Inter); font-size: 10px; font-weight: 500; background: var(--accent-light, #E8F4FD); color: var(--accent, #4A9FD8); transition: opacity 0.15s; }
.btn-edit:hover { opacity: 0.8; }
.btn-del { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans, Inter); font-size: 10px; font-weight: 500; background: rgba(248,113,113,0.1); color: var(--danger, #F87171); transition: opacity 0.15s; }
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
.modal { width: 100%; max-width: 640px; background: var(--bg-primary, #FFF); border-radius: var(--card-radius, 16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans, Inter); font-size: 20px; font-weight: 600; color: var(--text-primary, #1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary, #F7F8FA); border: none; font-size: 14px; color: var(--text-muted, #888); cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.modal__close:hover { background: var(--border, #E5E7EB); }
.modal__error { margin: 12px 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger, #F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }

.field { display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0; }
.field-label { font-family: var(--font-sans, Inter); font-size: 13px; font-weight: 500; color: var(--text-primary, #1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius, 12px); background: var(--bg-secondary, #F7F8FA); border: 1.5px solid var(--border, #E5E7EB); display: flex; align-items: center; }
.input-box:focus-within { border-color: var(--accent, #4A9FD8); border-width: 2px; }
.input-box input, .input-box select { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-primary, #1A1A1A); }
.input-box input::placeholder { color: var(--text-muted, #888); }
.select-box { position: relative; padding: 0; }
.select-box select { padding: 10px 14px; cursor: pointer; appearance: none; -webkit-appearance: none; }
.select-arrow { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); font-size: 10px; color: var(--text-muted, #888); pointer-events: none; }
.form-row-2 { display: flex; gap: 12px; }
.form-row-3 { display: flex; gap: 12px; }
.input-textarea { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans, Inter); font-size: 13px; color: var(--text-primary, #1A1A1A); resize: vertical; min-height: 60px; }
.cover-upload { display: flex; align-items: center; gap: 12px; }
.cover-preview { width: 80px; height: 100px; border-radius: 8px; overflow: hidden; border: 1px solid var(--border, #E5E7EB); background: var(--bg-secondary, #F7F8FA); }
.cover-preview img { width: 100%; height: 100%; object-fit: cover; }
.cover-input { font-size: 12px; color: var(--text-secondary, #666); }
.advanced-search { background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.adv-row { display: flex; gap: 16px; }
.adv-field { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.adv-field label { font-size: 11px; color: var(--text-muted,#888); font-weight: 500; }
.adv-field input, .adv-field select { padding: 8px 10px; border-radius: 8px; border: 1.5px solid var(--border,#E5E7EB); background: var(--bg-secondary,#F7F8FA); font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-primary,#1A1A1A); outline: none; }
.adv-field input:focus, .adv-field select:focus { border-color: var(--accent,#4A9FD8); }
.adv-year-range { display: flex; align-items: center; gap: 6px; }
.btn-primary { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius, 10px); background: var(--accent, #4A9FD8); color: var(--text-inverse, #FFF); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; transition: opacity 0.15s; min-width: 120px; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-radius: var(--button-radius, 10px); background: var(--bg-primary, #FFF); color: var(--text-secondary, #666); font-family: var(--font-sans, Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border, #E5E7EB); cursor: pointer; transition: background 0.15s; }
.btn-cancel:hover { background: var(--bg-secondary, #F7F8FA); }
.spinner { width: 16px; height: 16px; border: 2px solid var(--text-inverse, #FFF); border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.cover-thumb { width: 36px; height: 48px; border-radius: 6px; background: var(--accent-light, #E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 16px; }
</style>

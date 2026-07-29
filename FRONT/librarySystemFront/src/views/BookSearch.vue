<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchBooks, getCategoryTree, getHotBooks } from '../api/books'
import http from '../api/index'
import type { BookItem } from '../api/books'
import NotifBell from '../components/NotifBell.vue'

const route = useRoute()
const router = useRouter()

// Nav
const realName = ref(localStorage.getItem('realName') || 'Reader')
const userInitials = ref(realName.value.charAt(0).toUpperCase())

// Search state
const keyword = ref('')
const showFilters = ref(false)
const loading = ref(true)
const books = ref<BookItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = ref(0)

// Filter state
const filters = ref({
  author: '',
  isbn: '',
  categoryId: undefined as number | undefined,
  publisherId: undefined as number | undefined,
  language: '',
  yearStart: '',
  yearEnd: '',
  availableOnly: false,
})

// Sort
const sortOptions = [
  { value: 'borrow_count_desc', label: '借阅最多' },
  { value: 'publish_date_desc', label: '最新出版' },
  { value: 'publish_date_asc', label: '最早出版' },
  { value: 'title_asc', label: '书名 A-Z' },
]
const currentSort = ref('borrow_count_desc')

// Dropdown data
const categories = ref<Array<{ id: number; name: string; children?: any[] }>>([])
const publishers = ref<Array<{ id: number; name: string }>>([])
const languages = ['中文', '英文']

// Fallback hot books
const suggestions = ref<BookItem[]>([])

const hasResults = computed(() => books.value.length > 0)
const emptyKeyword = computed(() => !keyword.value && !filters.value.author && !filters.value.isbn)

onMounted(async () => {
  // Load dropdown data
  await Promise.all([
    loadCategories(),
    loadPublishers(),
  ])

  // Read query params from URL
  const q = route.query
  if (q.keyword) keyword.value = q.keyword as string
  if (q.author) filters.value.author = q.author as string
  if (q.isbn) filters.value.isbn = q.isbn as string
  if (q.categoryId) filters.value.categoryId = Number(q.categoryId)
  if (q.publisherId) filters.value.publisherId = Number(q.publisherId)
  if (q.language) filters.value.language = q.language as string
  if (q.sort) currentSort.value = q.sort as string

  doSearch()
})

async function loadCategories() {
  try {
    const tree = await getCategoryTree()
    // Flatten tree to flat list for dropdown
    categories.value = flattenTree(tree)
  } catch {
    categories.value = []
  }
}

function flattenTree(nodes: any[]): any[] {
  const result: any[] = []
  function walk(list: any[]) {
    for (const n of list) {
      result.push({ id: n.id, name: n.name })
      if (n.children && n.children.length > 0) walk(n.children)
    }
  }
  walk(nodes)
  return result
}

async function loadPublishers() {
  try {
    const data = await http.get('/publishers')
    publishers.value = (data as unknown as any[]).map((p: any) => ({ id: p.id, name: p.name }))
  } catch {
    publishers.value = []
  }
}

async function doSearch() {
  loading.value = true
  try {
    // Build search params
    const params: any = { page: currentPage.value, size: pageSize.value, sort: currentSort.value }
    if (keyword.value) params.keyword = keyword.value
    if (filters.value.author) params.author = filters.value.author
    if (filters.value.isbn) params.isbn = filters.value.isbn
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.publisherId) params.publisherId = filters.value.publisherId
    if (filters.value.language) params.language = filters.value.language
    if (filters.value.yearStart) params.yearStart = filters.value.yearStart
    if (filters.value.yearEnd) params.yearEnd = filters.value.yearEnd
    if (filters.value.availableOnly) params.availableOnly = true

    const result: any = await searchBooks(params)
    books.value = result.records || []
    total.value = result.total || 0
    totalPages.value = result.pages || 1

    // If no results, load suggestions
    if (books.value.length === 0 && emptyKeyword.value) {
      loadSuggestions()
    }
  } catch {
    books.value = []
    total.value = 0
    totalPages.value = 1
    if (emptyKeyword.value) loadSuggestions()
  } finally {
    loading.value = false
  }
}

async function loadSuggestions() {
  try {
    suggestions.value = await getHotBooks(4)
  } catch {
    suggestions.value = []
  }
}

function onSearch() {
  currentPage.value = 1
  doSearch()
  // Update URL with query params
  router.replace({ query: { ...route.query, keyword: keyword.value || undefined } })
}

function onSortChange() {
  currentPage.value = 1
  doSearch()
}

function toggleFilters() {
  showFilters.value = !showFilters.value
}

function clearFilters() {
  filters.value = {
    author: '',
    isbn: '',
    categoryId: undefined,
    publisherId: undefined,
    language: '',
    yearStart: '',
    yearEnd: '',
    availableOnly: false,
  }
  keyword.value = ''
  currentSort.value = 'borrow_count_desc'
  onSearch()
}

function hasActiveFilters(): boolean {
  return !!(filters.value.author || filters.value.isbn || filters.value.categoryId ||
    filters.value.publisherId || filters.value.language || filters.value.yearStart ||
    filters.value.yearEnd || filters.value.availableOnly)
}

function goToDetail(bookId: number) {
  router.push(`/books/${bookId}`)
}

function goToDashboard() {
  router.push('/reader')
}

function goHome() {
  router.push('/home')
}

// Pagination
const pageNumbers = computed(() => {
  const p: (number | string)[] = []
  const tp = totalPages.value
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) p.push(i)
  } else {
    p.push(1)
    const s = Math.max(2, currentPage.value - 1)
    const e = Math.min(tp - 1, currentPage.value + 1)
    if (s > 2) p.push('...')
    for (let i = s; i <= e; i++) p.push(i)
    if (e < tp - 1) p.push('...')
    p.push(tp)
  }
  return p
})

function goPage(page: number | string) {
  if (typeof page !== 'number') return
  currentPage.value = page
  doSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <div class="search-page">
    <!-- Nav Bar -->
    <nav class="nav">
      <span class="nav__logo" @click="goHome">📚 LibraryOS</span>
      <div class="nav__links">
        <span class="nav__link" @click="goHome">首页</span>
        <span class="nav__link nav__link--active">搜索</span>
        <span class="nav__link" @click="goHome">分类</span>
        <span class="nav__link" @click="goToDashboard">我的</span>
      </div>
      <div class="nav__user">
        <NotifBell />
        <span class="nav__username">{{ realName }}</span>
        <div class="nav__avatar" @click="goToDashboard">{{ userInitials }}</div>
      </div>
    </nav>

    <!-- Search Header -->
    <div class="search-header">
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <div class="search-input-area">
          <input
            v-model="keyword"
            class="search-input"
            placeholder="按书名、作者或ISBN搜索..."
            @keyup.enter="onSearch"
          />
        </div>
        <button class="search-btn" @click="onSearch">搜索</button>
        <button
          :class="['filter-toggle', { 'filter-toggle--active': showFilters }]"
          @click="toggleFilters"
          :title="showFilters ? '收起筛选' : '展开筛选'"
        >
          <span v-if="hasActiveFilters()" class="filter-dot"></span>
          ⏳
        </button>
      </div>

      <!-- Sort & Result count -->
      <div class="search-meta">
        <span class="result-count" v-if="!loading">
          {{ total > 0 ? `共找到 ${total} 本书` : '' }}
        </span>
        <div class="sort-group">
          <label class="sort-label">排序：</label>
          <select v-model="currentSort" @change="onSortChange" class="sort-select">
            <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Filters Panel -->
    <transition name="fade">
      <div v-if="showFilters" class="filters-panel">
        <div class="filters-grid">
          <div class="filter-field">
            <label class="filter-label">作者</label>
            <input v-model="filters.author" class="filter-input" placeholder="作者名称" @keyup.enter="onSearch" />
          </div>
          <div class="filter-field">
            <label class="filter-label">ISBN</label>
            <input v-model="filters.isbn" class="filter-input" placeholder="精确ISBN" @keyup.enter="onSearch" />
          </div>
          <div class="filter-field">
            <label class="filter-label">分类</label>
            <select v-model="filters.categoryId" class="filter-select" @change="onSearch">
              <option :value="undefined">全部分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="filter-field">
            <label class="filter-label">出版社</label>
            <select v-model="filters.publisherId" class="filter-select" @change="onSearch">
              <option :value="undefined">全部出版社</option>
              <option v-for="p in publishers" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div class="filter-field">
            <label class="filter-label">语言</label>
            <select v-model="filters.language" class="filter-select" @change="onSearch">
              <option value="">全部语言</option>
              <option v-for="lang in languages" :key="lang" :value="lang">{{ lang }}</option>
            </select>
          </div>
          <div class="filter-field">
            <label class="filter-label">出版年份</label>
            <div class="year-range">
              <input v-model="filters.yearStart" class="filter-input" placeholder="起始年" type="number" @keyup.enter="onSearch" />
              <span class="year-sep">—</span>
              <input v-model="filters.yearEnd" class="filter-input" placeholder="结束年" type="number" @keyup.enter="onSearch" />
            </div>
          </div>
          <div class="filter-field filter-field--checkbox">
            <label class="checkbox-label">
              <input v-model="filters.availableOnly" type="checkbox" class="filter-checkbox" @change="onSearch" />
              <span>仅显示可借</span>
            </label>
          </div>
        </div>
        <div class="filter-actions">
          <button class="btn-clear" @click="clearFilters">清除所有筛选</button>
        </div>
      </div>
    </transition>

    <!-- Results -->
    <div class="results-section">
      <!-- Loading -->
      <div v-if="loading" class="loading-msg">
        <div class="loading-spinner"></div>
        <span>正在搜索图书...</span>
      </div>

      <!-- Results Grid -->
      <div v-if="!loading && hasResults" class="book-grid">
        <div
          v-for="book in books"
          :key="book.id"
          class="book-card"
          @click="goToDetail(book.id)"
        >
          <div class="book-cover">
            <span class="book-cover-icon">📖</span>
          </div>
          <div class="book-info">
            <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
            <p class="book-author" :title="book.author">{{ book.author }}</p>
            <p class="book-publisher" v-if="book.publisherName">{{ book.publisherName }}</p>
            <div class="book-meta">
              <span class="book-tag book-tag--rating">★ {{ book.rating ?? '—' }}</span>
              <span :class="['book-tag', book.availableCopies > 0 ? 'book-tag--available' : 'book-tag--unavailable']">
                {{ book.availableCopies }}/{{ book.totalCopies }}
              </span>
              <span class="book-tag book-tag--borrow">📚 {{ book.borrowCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && !hasResults" class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3 class="empty-title">未找到相关图书</h3>
        <p class="empty-desc">
          {{ emptyKeyword ? '试试按书名、作者或ISBN搜索。' : '试试调整筛选条件或搜索关键词。' }}
        </p>
        <button v-if="hasActiveFilters()" class="btn-clear-empty" @click="clearFilters">清除所有筛选</button>

        <!-- Suggestions -->
        <div v-if="suggestions.length > 0" class="suggestions">
          <h4 class="suggestions-title">🔥 热门推荐</h4>
          <div class="suggestions-grid">
            <div
              v-for="book in suggestions"
              :key="book.id"
              class="suggestion-card"
              @click="goToDetail(book.id)"
            >
              <span class="suggestion-icon">📖</span>
              <div>
                <p class="suggestion-title">{{ book.title }}</p>
                <p class="suggestion-author">{{ book.author }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="!loading && totalPages > 1" class="pagination">
        <button
          :disabled="currentPage <= 1"
          class="page-btn"
          @click="goPage(currentPage - 1)"
        >
          ← 上一页
        </button>
        <template v-for="p in pageNumbers" :key="p">
          <span v-if="p === '...'" class="page-ellipsis">...</span>
          <button
            v-else
            :class="['page-btn', { 'page-btn--active': p === currentPage }]"
            @click="goPage(p)"
          >
            {{ p }}
          </button>
        </template>
        <button
          :disabled="currentPage >= totalPages"
          class="page-btn"
          @click="goPage(currentPage + 1)"
        >
          下一页 →
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary, #F7F8FA);
}

/* ===== Nav Bar ===== */
.nav {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 40px;
  background: var(--bg-primary, #FFFFFF);
  height: 68px;
  flex-shrink: 0;
}

.nav__logo {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  cursor: pointer;
}

.nav__links {
  display: flex;
  gap: 24px;
  flex: 1;
}

.nav__link {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-secondary, #666666);
  cursor: pointer;
  transition: color 0.15s;
}

.nav__link:hover,
.nav__link--active {
  color: var(--accent, #4A9FD8);
}

.nav__user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav__username {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary, #666);
}

.nav__avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  color: var(--accent, #4A9FD8);
  flex-shrink: 0;
  cursor: pointer;
}

/* ===== Search Header ===== */
.search-header {
  padding: 32px 80px 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 720px;
  height: 48px;
  padding: 4px 6px 4px 20px;
  border-radius: 999px;
  background: var(--bg-primary, #FFFFFF);
  border: 1.5px solid var(--border, #E5E7EB);
  transition: border-color 0.15s;
}

.search-bar:focus-within {
  border-color: var(--accent, #4A9FD8);
}

.search-icon {
  font-size: 16px;
  line-height: 1;
  flex-shrink: 0;
}

.search-input-area {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-primary, #1A1A1A);
}

.search-input::placeholder {
  color: var(--text-muted, #888888);
}

.search-btn {
  padding: 10px 20px;
  border-radius: 999px;
  background: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFFFFF);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  transition: opacity 0.15s;
}

.search-btn:hover {
  opacity: 0.9;
}

.filter-toggle {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: background 0.15s;
  flex-shrink: 0;
}

.filter-toggle:hover,
.filter-toggle--active {
  background: var(--accent-light, #E8F4FD);
}

.filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--danger, #F87171);
  position: absolute;
  top: 6px;
  right: 6px;
}

/* Search Meta */
.search-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-count {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-muted, #888);
}

.sort-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-secondary, #666);
}

.sort-select {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--border, #E5E7EB);
  background: var(--bg-primary, #FFF);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  cursor: pointer;
  outline: none;
}

.sort-select:focus {
  border-color: var(--accent, #4A9FD8);
}

/* ===== Filters Panel ===== */
.filters-panel {
  margin: 0 80px;
  padding: 20px;
  background: var(--bg-primary, #FFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
  margin-bottom: 0;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-field--checkbox {
  justify-content: flex-end;
}

.filter-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary, #666);
}

.filter-input {
  padding: 10px 14px;
  border-radius: var(--input-radius, 12px);
  border: 1px solid var(--border, #E5E7EB);
  background: var(--bg-secondary, #F7F8FA);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  outline: none;
  transition: border-color 0.15s;
  width: 100%;
}

.filter-input:focus {
  border-color: var(--accent, #4A9FD8);
  background: var(--bg-primary, #FFF);
}

.filter-select {
  padding: 10px 14px;
  border-radius: var(--input-radius, 12px);
  border: 1px solid var(--border, #E5E7EB);
  background: var(--bg-secondary, #F7F8FA);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  outline: none;
  cursor: pointer;
  transition: border-color 0.15s;
  width: 100%;
}

.filter-select:focus {
  border-color: var(--accent, #4A9FD8);
  background: var(--bg-primary, #FFF);
}

.year-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.year-sep {
  color: var(--text-muted, #888);
  font-size: 14px;
  flex-shrink: 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  cursor: pointer;
  padding: 10px 0;
}

.filter-checkbox {
  width: 16px;
  height: 16px;
  accent-color: var(--accent, #4A9FD8);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
}

.btn-clear {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  background: transparent;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--danger, #F87171);
  cursor: pointer;
  transition: background 0.15s;
}

.btn-clear:hover {
  background: rgba(248, 113, 113, 0.08);
}

/* ===== Results Section ===== */
.results-section {
  padding: 20px 80px 40px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.loading-msg {
  padding: 60px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: var(--text-muted, #888);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  border: 2px solid var(--border, #E5E7EB);
  border-top-color: var(--accent, #4A9FD8);
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ===== Book Grid ===== */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.book-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: var(--bg-primary, #FFFFFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}

.book-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.book-cover {
  height: 160px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px 14px 0 0;
}

.book-cover-icon {
  font-size: 36px;
  line-height: 1;
}

.book-info {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.book-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-secondary, #666666);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-publisher {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  color: var(--text-muted, #888);
  margin: 0;
}

.book-meta {
  display: flex;
  gap: 8px;
  margin-top: 2px;
  flex-wrap: wrap;
}

.book-tag {
  padding: 3px 8px;
  border-radius: 999px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 500;
  line-height: 1.3;
}

.book-tag--rating {
  background: rgba(251, 191, 36, 0.15);
  color: var(--warning, #FBBF24);
}

.book-tag--available {
  background: rgba(52, 211, 153, 0.15);
  color: var(--success, #34D399);
}

.book-tag--unavailable {
  background: rgba(248, 113, 113, 0.1);
  color: var(--danger, #F87171);
}

.book-tag--borrow {
  background: var(--accent-light, #E8F4FD);
  color: var(--accent, #4A9FD8);
}

/* ===== Empty State ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  line-height: 1;
}

.empty-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}

.empty-desc {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin: 0;
}

.btn-clear-empty {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid var(--border, #E5E7EB);
  background: var(--bg-primary, #FFF);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--accent, #4A9FD8);
  cursor: pointer;
  transition: all 0.15s;
}

.btn-clear-empty:hover {
  background: var(--accent-light, #E8F4FD);
  border-color: var(--accent, #4A9FD8);
}

/* Suggestions */
.suggestions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
  width: 100%;
  max-width: 600px;
}

.suggestions-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
  text-align: left;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.suggestion-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: var(--bg-primary, #FFF);
  border-radius: 12px;
  border: 1px solid var(--border, #E5E7EB);
  cursor: pointer;
  transition: box-shadow 0.15s;
  text-align: left;
}

.suggestion-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.suggestion-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.suggestion-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.suggestion-author {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-secondary, #666);
  margin: 0;
}

/* ===== Pagination ===== */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 20px 0;
}

.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid var(--border, #E5E7EB);
  background: var(--bg-primary, #FFF);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-secondary, #666);
  cursor: pointer;
  transition: all 0.15s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--accent, #4A9FD8);
  color: var(--accent, #4A9FD8);
}

.page-btn--active {
  background: var(--accent, #4A9FD8);
  border-color: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFF);
  font-weight: 600;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-ellipsis {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-muted, #888);
  padding: 0 4px;
}
</style>

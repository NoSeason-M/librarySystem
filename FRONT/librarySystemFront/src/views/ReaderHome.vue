<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotBooks, getNewArrivals, getCategoryTree } from '../api/books'
import type { BookItem } from '../api/books'
import NotifBell from '../components/NotifBell.vue'
import AppLogo from '../components/AppLogo.vue'

const router = useRouter()

// Nav
const realName = ref(localStorage.getItem('realName') || 'Reader')
const userInitials = ref(realName.value.charAt(0).toUpperCase())

// Search
const keyword = ref('')
const activeFilter = ref('all')
const filters = [
  { key: 'all', label: '全部' },
  { key: 'available', label: '可借' },
]

// Categories from API
interface CategoryItem {
  id: number
  name: string
  emoji: string
  children?: CategoryItem[]
}
const categories = ref<CategoryItem[]>([])

// Emoji map for top-level categories
const categoryEmojis: Record<string, string> = {
  '马克思主义、列宁主义、毛泽东思想、邓小平理论': '📕',
  '哲学、宗教': '💭',
  '社会科学总论': '📊',
  '政治、法律': '⚖️',
  '军事': '⚔️',
  '经济': '💰',
  '文化、科学、教育、体育': '📚',
  '语言、文字': '🔤',
  '文学': '📖',
  '艺术': '🎨',
  '历史、地理': '🏛️',
  '自然科学总论': '🔬',
  '数理科学和化学': '🧮',
  '天文学、地球科学': '🌍',
  '生物科学': '🧬',
  '医药、卫生': '🏥',
  '农业科学': '🌾',
  '工业技术': '⚙️',
  '交通运输': '🚗',
  '航空、航天': '🚀',
  '环境科学、安全科学': '🌿',
  '综合性图书': '📚',
}

const defaultEmojis = ['📕', '💭', '📊', '⚖️', '⚔️', '💰', '📚', '🔤', '📖', '🎨', '🏛️', '🔬', '🧮', '🌍', '🧬', '🏥', '🌾', '⚙️', '🚗', '🚀', '🌿', '📚']

// Books
const hotBooks = ref<BookItem[]>([])
const newBooks = ref<BookItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [hot, newArrivals, catTree] = await Promise.all([
      getHotBooks(4).catch(() => []),
      getNewArrivals(30, 4).catch(() => []),
      getCategoryTree().catch(() => []),
    ])
    hotBooks.value = hot.length > 0 ? hot : getDemoBooks()
    newBooks.value = newArrivals
    categories.value = buildCategoryList(catTree)
  } catch {
    hotBooks.value = getDemoBooks()
    categories.value = getDemoCategories()
  } finally {
    loading.value = false
  }
})

function buildCategoryList(tree: any[]): CategoryItem[] {
  return tree.map((c, i) => ({
    id: c.id,
    name: c.name,
    emoji: categoryEmojis[c.name] || defaultEmojis[i % defaultEmojis.length] || '📚',
    children: c.children,
  }))
}

function getDemoBooks(): BookItem[] {
  return [
    { id: 1, isbn: '', title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', coverUrl: null, publishDate: null, price: null, totalCopies: 5, availableCopies: 4, borrowCount: 128, rating: 4.5, summary: null, categoryName: null },
    { id: 2, isbn: '', title: 'To Kill a Mockingbird', author: 'Harper Lee', coverUrl: null, publishDate: null, price: null, totalCopies: 5, availableCopies: 2, borrowCount: 95, rating: 4.8, summary: null, categoryName: null },
    { id: 3, isbn: '', title: '1984', author: 'George Orwell', coverUrl: null, publishDate: null, price: null, totalCopies: 3, availableCopies: 0, borrowCount: 210, rating: 4.6, summary: null, categoryName: null },
    { id: 4, isbn: '', title: 'Pride and Prejudice', author: 'Jane Austen', coverUrl: null, publishDate: null, price: null, totalCopies: 4, availableCopies: 3, borrowCount: 67, rating: 4.7, summary: null, categoryName: null },
  ]
}

function getDemoCategories(): CategoryItem[] {
  return [
    { id: 9, name: '文学', emoji: '📖' },
    { id: 2, name: '哲学、宗教', emoji: '💭' },
    { id: 6, name: '经济', emoji: '💰' },
    { id: 18, name: '工业技术', emoji: '⚙️' },
    { id: 7, name: '文化、科学、教育、体育', emoji: '📚' },
    { id: 8, name: '语言、文字', emoji: '🔤' },
    { id: 10, name: '艺术', emoji: '🎨' },
    { id: 11, name: '历史、地理', emoji: '🏛️' },
  ]
}

function doSearch() {
  router.push({ path: '/books', query: { keyword: keyword.value } })
}

function goToCategory(catId: number) {
  router.push({ path: '/books', query: { categoryId: catId } })
}

function goToDetail(bookId: number) {
  router.push(`/books/${bookId}`)
}

function goToDashboard() {
  router.push('/reader')
}

function goToSearch() {
  router.push({ path: '/books' })
}

function goToSearchByFilter(filter: string) {
  if (filter === 'available') {
    router.push({ path: '/books', query: { availableOnly: 'true' } })
  } else {
    router.push({ path: '/books' })
  }
}
</script>

<template>
  <div class="reader-home">
    <!-- Nav Bar -->
    <nav class="nav">
      <span class="nav__logo"><AppLogo /> LibraryOS</span>
      <div class="nav__links">
        <span class="nav__link nav__link--active" @click="goToSearch">首页</span>
        <span class="nav__link" @click="goToSearch">浏览</span>
        <span class="nav__link" @click="goToSearch">分类</span>
        <span class="nav__link" @click="goToDashboard">我的</span>
      </div>
      <div class="nav__user">
        <NotifBell />
        <span class="nav__username">{{ realName }}</span>
        <div class="nav__avatar" @click="goToDashboard">{{ userInitials }}</div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero">
      <h1 class="hero__title">想找什么书？</h1>
      <p class="hero__sub">从海量馆藏中搜索你心仪的图书</p>

      <!-- Search Bar (pill) -->
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <div class="search-input-area">
          <input
            v-model="keyword"
            class="search-input"
            placeholder="按书名、作者或ISBN搜索..."
            @keyup.enter="doSearch"
          />
        </div>
        <button class="search-btn" @click="doSearch">搜索</button>
      </div>

      <!-- Quick Filters -->
      <div class="quick-filters">
        <div
          v-for="f in filters"
          :key="f.key"
          :class="['filter-chip', { 'filter-chip--active': activeFilter === f.key }]"
          @click="activeFilter = f.key; goToSearchByFilter(f.key)"
        >
          {{ f.label }}
        </div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="categories-section">
      <div class="section-header">
        <h2 class="section-title">按分类浏览</h2>
        <a class="section-viewall" @click="goToSearch">查看全部 →</a>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories.slice(0, 8)"
          :key="cat.id"
          class="category-card"
          @click="goToCategory(cat.id)"
        >
          <span class="category-emoji">{{ cat.emoji }}</span>
          <span class="category-label">{{ cat.name.length > 8 ? cat.name.slice(0, 7) + '…' : cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- Hot Books Section -->
    <section class="hot-section">
      <div class="section-header">
        <h2 class="section-title">🔥 本周热门</h2>
        <a class="section-viewall" @click="goToSearch">查看全部 →</a>
      </div>

      <div v-if="loading" class="loading-msg">正在加载图书...</div>

      <div v-if="!loading" class="book-grid">
        <div v-for="book in hotBooks" :key="book.id" class="book-card" @click="goToDetail(book.id)">
          <div class="book-cover">
            <span class="book-cover-icon">📖</span>
          </div>
          <div class="book-info">
            <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
            <p class="book-author" :title="book.author">{{ book.author }}</p>
            <div class="book-meta">
              <span class="book-tag book-tag--warning">★ {{ book.rating ?? '–' }}</span>
              <span :class="['book-tag', book.availableCopies > 0 ? 'book-tag--success' : 'book-tag--danger']">
                {{ book.availableCopies }}/{{ book.totalCopies }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.reader-home {
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

/* ===== Hero Section ===== */
.hero {
  background: var(--bg-primary, #FFFFFF);
  padding: 60px 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero__title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}

.hero__sub {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 15px;
  color: var(--text-secondary, #666666);
  margin: 0;
}

/* Search Bar (pill) */
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 600px;
  height: 48px;
  padding: 4px 6px 4px 20px;
  border-radius: 999px;
  background: var(--bg-secondary, #F7F8FA);
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

.search-btn:hover { opacity: 0.9; }

/* Quick Filters */
.quick-filters {
  display: flex;
  gap: 8px;
}

.filter-chip {
  padding: 10px 16px;
  border-radius: 999px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  background: var(--bg-secondary, #F7F8FA);
  border: 1px solid var(--border, #E5E7EB);
  color: var(--text-secondary, #666666);
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.filter-chip--active {
  background: var(--accent, #4A9FD8);
  border-color: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFFFFF);
  font-weight: 500;
}

.filter-chip:hover:not(.filter-chip--active) {
  border-color: var(--accent, #4A9FD8);
  color: var(--accent, #4A9FD8);
}

/* ===== Section Common ===== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}

.section-viewall {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--accent, #4A9FD8);
  cursor: pointer;
  transition: opacity 0.15s;
}

.section-viewall:hover { opacity: 0.8; }

.loading-msg {
  padding: 40px;
  text-align: center;
  color: var(--text-muted, #888);
  font-size: 14px;
}

/* ===== Categories Section ===== */
.categories-section {
  padding: 40px 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.category-grid {
  display: flex;
  gap: 12px;
}

.category-card {
  width: 150px;
  height: 104px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: var(--bg-primary, #FFFFFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}

.category-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  transform: translateY(-1px);
}

.category-emoji {
  font-size: 28px;
  line-height: 1;
}

.category-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary, #1A1A1A);
  text-align: center;
}

/* ===== Hot Books Section ===== */
.hot-section {
  padding: 0 80px 40px 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.book-grid {
  display: flex;
  gap: 16px;
}

.book-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: var(--bg-primary, #FFFFFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
  max-width: 308px;
}

.book-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.book-cover {
  height: 180px;
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

.book-meta {
  display: flex;
  gap: 12px;
  margin-top: 2px;
}

.book-tag {
  padding: 3px 8px;
  border-radius: 999px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 500;
  color: var(--text-inverse, #FFFFFF);
  line-height: 1.3;
}

.book-tag--warning { background: var(--warning, #FBBF24); }
.book-tag--success { background: var(--success, #34D399); }
.book-tag--danger { background: var(--danger, #F87171); }
</style>

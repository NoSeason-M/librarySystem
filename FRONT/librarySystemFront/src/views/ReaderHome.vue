<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchBooks, getHotBooks, getNewArrivals, getCategoryTree } from '../api/books'
import type { BookItem } from '../api/books'

const router = useRouter()

// Nav
const navLinks = ['Home', 'Browse', 'Categories', 'My Books']
const realName = ref(localStorage.getItem('realName') || 'Reader')
const userInitials = ref(realName.value.charAt(0).toUpperCase())

// Search
const keyword = ref('')
const activeFilter = ref('All')
const filters = ['All', 'Available', 'E-Books', 'Audio Books']

// Categories
const categories = [
  { name: 'Literature', emoji: '📖' },
  { name: 'Science', emoji: '🔬' },
  { name: 'Technology', emoji: '💻' },
  { name: 'History', emoji: '🏛️' },
  { name: 'Philosophy', emoji: '💭' },
  { name: 'Art', emoji: '🎨' },
  { name: 'Economics', emoji: '📊' },
  { name: 'Education', emoji: '📝' },
]

// Books
const hotBooks = ref<BookItem[]>([])
const newBooks = ref<BookItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [hot, newArrivals] = await Promise.all([
      getHotBooks(4).catch(() => []),
      getNewArrivals(30, 4).catch(() => []),
    ])
    hotBooks.value = hot.length > 0 ? hot : getDemoBooks()
    newBooks.value = newArrivals
  } catch {
    hotBooks.value = getDemoBooks()
  } finally {
    loading.value = false
  }
})

function getDemoBooks(): BookItem[] {
  return [
    { id: 1, isbn: '', title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', coverUrl: null, publishDate: null, price: null, totalCopies: 5, availableCopies: 4, borrowCount: 128, rating: 4.5, summary: null, categoryName: null },
    { id: 2, isbn: '', title: 'To Kill a Mockingbird', author: 'Harper Lee', coverUrl: null, publishDate: null, price: null, totalCopies: 5, availableCopies: 2, borrowCount: 95, rating: 4.8, summary: null, categoryName: null },
    { id: 3, isbn: '', title: '1984', author: 'George Orwell', coverUrl: null, publishDate: null, price: null, totalCopies: 3, availableCopies: 0, borrowCount: 210, rating: 4.6, summary: null, categoryName: null },
    { id: 4, isbn: '', title: 'Pride and Prejudice', author: 'Jane Austen', coverUrl: null, publishDate: null, price: null, totalCopies: 4, availableCopies: 3, borrowCount: 67, rating: 4.7, summary: null, categoryName: null },
  ]
}

function doSearch() {
  router.push({ path: '/books', query: { keyword: keyword.value } })
}

function goToDetail(bookId: number) {
  router.push(`/books/${bookId}`)
}

function goToDashboard() {
  router.push('/reader')
}
</script>

<template>
  <div class="reader-home">
    <!-- Nav Bar (1440×68) -->
    <nav class="nav">
      <span class="nav__logo">📚 LibraryOS</span>
      <div class="nav__links">
        <span v-for="link in navLinks" :key="link" class="nav__link">{{ link }}</span>
      </div>
      <div class="nav__user">
        <span class="nav__username">{{ realName }}</span>
        <div class="nav__avatar" @click="goToDashboard">{{ userInitials }}</div>
      </div>
    </nav>

    <!-- Hero Section (1440×322) -->
    <section class="hero">
      <h1 class="hero__title">What book are you looking for?</h1>
      <p class="hero__sub">Search from thousands of books in our collection</p>

      <!-- Search Bar (600×48, pill) -->
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <div class="search-input-area">
          <input
            v-model="keyword"
            class="search-input"
            placeholder="Search by title, author, or ISBN..."
            @keyup.enter="doSearch"
          />
        </div>
        <button class="search-btn" @click="doSearch">Search</button>
      </div>

      <!-- Quick Filters -->
      <div class="quick-filters">
        <div
          v-for="f in filters"
          :key="f"
          :class="['filter-chip', { 'filter-chip--active': activeFilter === f }]"
          @click="activeFilter = f"
        >
          {{ f }}
        </div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="categories-section">
      <div class="section-header">
        <h2 class="section-title">Browse by Category</h2>
        <a class="section-viewall">View all →</a>
      </div>
      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.name" class="category-card">
          <span class="category-emoji">{{ cat.emoji }}</span>
          <span class="category-label">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- Hot Books Section -->
    <section class="hot-section">
      <div class="section-header">
        <h2 class="section-title">🔥 Hot Picks This Week</h2>
        <a class="section-viewall">View all →</a>
      </div>

      <div v-if="loading" class="loading-msg">Loading books...</div>

      <div v-if="!loading" class="book-grid">
        <div v-for="book in hotBooks" :key="book.id" class="book-card" @click="goToDetail(book.id)">
          <div class="book-cover">
            <span class="book-cover-icon">📖</span>
          </div>
          <div class="book-info">
            <h3 class="book-title">{{ book.title }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <div class="book-meta">
              <span class="book-tag book-tag--warning">★ {{ book.rating ?? '–' }}</span>
              <span class="book-tag book-tag--success">{{ book.availableCopies }}/{{ book.totalCopies }}</span>
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

/* ===== Nav Bar (1440×68) ===== */
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
.nav__link:hover { color: var(--accent, #4A9FD8); }

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

/* ===== Hero Section (1440×322) ===== */
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

/* Search Bar (600×48, pill) */
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

/* ===== Categories Section (padding: [40,80], gap:20) ===== */
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
}

/* ===== Hot Books Section (padding: [0,80,40,80], gap:20) ===== */
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
</style>

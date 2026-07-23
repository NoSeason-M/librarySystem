<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookDetail, getBookCopies } from '../api/books'
import type { BookItem, BookCopyItem } from '../api/books'

const route = useRoute()
const router = useRouter()

const book = ref<BookItem | null>(null)
const copies = ref<BookCopyItem[]>([])
const loading = ref(true)
const activeTab = ref('Description')
const error = ref('')

const tabs = ['Description', 'Copies', 'Reviews', 'History']

const availableCount = computed(() => book.value?.availableCopies ?? 0)
const borrowedCount = computed(() => (book.value?.totalCopies ?? 0) - (book.value?.availableCopies ?? 0))

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) { error.value = 'Invalid book ID'; loading.value = false; return }

  try {
    const [bookData, copyData] = await Promise.all([
      getBookDetail(id),
      getBookCopies(id).catch(() => []),
    ])
    book.value = bookData
    copies.value = copyData
  } catch {
    error.value = 'Failed to load book details'
    book.value = getDemoBook(id)
    copies.value = getDemoCopies()
  } finally {
    loading.value = false
  }
})

function getDemoBook(id: number): BookItem {
  const demos: Record<number, BookItem> = {
    1: { id: 1, isbn: '9780743273565', title: 'The Great Gatsby', subTitle: null, author: 'F. Scott Fitzgerald', translator: null, coverUrl: null, publishDate: '1925-04-10', price: 12.99, pages: 180, binding: '平装', language: '英文', totalCopies: 5, availableCopies: 3, borrowCount: 128, rating: 4.5, ratingCount: 1024, summary: 'The Great Gatsby is a 1925 novel by American writer F. Scott Fitzgerald. Set in the Jazz Age on Long Island, the novel depicts narrator Nick Carraway\'s interactions with mysterious millionaire Jay Gatsby and Gatsby\'s obsession to reunite with his former lover, Daisy Buchanan.', categoryName: '文学', categoryId: 9, status: 1 },
    7: { id: 7, isbn: '9780743273565', title: 'The Great Gatsby', subTitle: null, author: 'F. Scott Fitzgerald', translator: null, coverUrl: null, publishDate: '1925-04-10', price: 12.99, pages: 180, binding: '平装', language: '英文', totalCopies: 5, availableCopies: 3, borrowCount: 128, rating: 4.5, ratingCount: 1024, summary: 'The Great Gatsby is a 1925 novel by American writer F. Scott Fitzgerald. Set in the Jazz Age on Long Island, the novel depicts narrator Nick Carraway\'s interactions with mysterious millionaire Jay Gatsby and Gatsby\'s obsession to reunite with his former lover, Daisy Buchanan.', categoryName: '文学', categoryId: 9, status: 1 },
  }
  return demos[id] || {
    id, isbn: '', title: 'Unknown Book', subTitle: null, author: 'Unknown', translator: null,
    coverUrl: null, publishDate: null, price: null, pages: null, binding: null, language: null,
    totalCopies: 0, availableCopies: 0, borrowCount: 0, rating: null, ratingCount: null,
    summary: 'No description available.', categoryName: null, categoryId: null, status: 1,
  }
}

function getDemoCopies(): BookCopyItem[] {
  return [
    { id: 1, barcode: 'GTB-001', locationName: 'Main Library - Floor 2', status: 'in', statusLabel: 'Available', dueDate: undefined },
    { id: 2, barcode: 'GTB-002', locationName: 'Main Library - Floor 2', status: 'borrowed', statusLabel: 'Borrowed', dueDate: 'Aug 15, 2026' },
    { id: 3, barcode: 'GTB-003', locationName: 'Science Library', status: 'in', statusLabel: 'Available', dueDate: undefined },
    { id: 4, barcode: 'GTB-004', locationName: 'Science Library', status: 'reserved', statusLabel: 'Reserved', dueDate: 'Aug 20, 2026' },
    { id: 5, barcode: 'GTB-005', locationName: 'Main Library - Floor 2', status: 'borrowed', statusLabel: 'Borrowed', dueDate: 'Sep 1, 2026' },
  ]
}

function goBack() {
  router.back()
}

function renderStars(rating: number | null): string {
  if (!rating) return ''
  const full = Math.floor(rating)
  const half = rating - full >= 0.5 ? '½' : ''
  return '★'.repeat(full) + half
}

function statusBadgeColor(status: string): string {
  switch (status) {
    case 'in': return 'var(--success, #34D399)'
    case 'borrowed': return 'var(--warning, #FBBF24)'
    case 'reserved': return 'var(--accent, #4A9FD8)'
    case 'maintenance': return 'var(--text-muted, #888)'
    case 'lost': case 'discarded': return 'var(--danger, #F87171)'
    default: return 'var(--text-muted, #888)'
  }
}
</script>

<template>
  <div class="book-detail">
    <!-- Nav Bar (1440×66) -->
    <nav class="nav">
      <span class="nav__logo" @click="router.push('/home')">📚 LibraryOS</span>
    </nav>

    <!-- Main Content (padding [40,80], gap=28) -->
    <main class="main">
      <div v-if="loading" class="loading-msg">Loading book details...</div>
      <div v-if="error" class="error-msg">{{ error }}</div>

      <template v-if="!loading && book">
        <!-- ← Back -->
        <div class="back-row" @click="goBack">
          <span class="back-arrow">←</span>
          <span class="back-link">Back to results</span>
        </div>

        <!-- Book Hero (horizontal, gap=40) -->
        <div class="book-hero">
          <!-- Left: Cover (240px) -->
          <div class="cover-section">
            <div class="cover-box">
              <span class="cover-icon">📖</span>
            </div>
            <p class="cover-isbn">ISBN: {{ book.isbn || '—' }}</p>
          </div>

          <!-- Right: Book Info -->
          <div class="book-info">
            <!-- Title -->
            <div class="title-section">
              <h1 class="book-title">{{ book.title }}</h1>
              <p class="book-author" v-if="book.author">by {{ book.author }}</p>
            </div>

            <!-- Meta: Rating + Reviews -->
            <div class="meta-row">
              <span class="meta-rating">★★★★½  {{ book.rating ?? '–' }}</span>
              <span class="meta-reviews" v-if="book.ratingCount">({{ book.ratingCount }} reviews)</span>
            </div>

            <!-- Tags -->
            <div class="tag-row">
              <span v-if="book.categoryName" class="tag">{{ book.categoryName }}</span>
              <span v-if="book.language" class="tag">{{ book.language }}</span>
              <span v-if="book.binding" class="tag">{{ book.binding }}</span>
              <span v-if="book.publishDate" class="tag">{{ book.publishDate?.slice(0, 4) }}</span>
            </div>

            <!-- Availability -->
            <div class="avail-row">
              <div class="avail-card">
                <span class="avail-label">Available</span>
                <span class="avail-value avail-value--success">{{ availableCount }}</span>
              </div>
              <div class="avail-card">
                <span class="avail-label">Borrowed</span>
                <span class="avail-value avail-value--warning">{{ borrowedCount }}</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="action-row">
              <button class="btn-borrow">Borrow This Book</button>
              <button class="btn-fav">♡</button>
            </div>
          </div>
        </div>

        <!-- Tabs -->
        <div class="tabs">
          <div class="tab-bar">
            <div v-for="tab in tabs" :key="tab"
              :class="['tab', { 'tab--active': activeTab === tab }]"
              @click="activeTab = tab">
              <span class="tab-label">{{ tab }}</span>
              <div v-if="activeTab === tab" class="tab-indicator"></div>
            </div>
          </div>
          <div class="tab-divider"></div>

          <!-- Description Tab -->
          <div v-if="activeTab === 'Description'" class="tab-content">
            <h3 class="tab-section-title">About this book</h3>
            <p class="tab-description">{{ book.summary || 'No description available.' }}</p>
          </div>

          <!-- Copies Tab -->
          <div v-if="activeTab === 'Copies'" class="tab-content">
            <h3 class="tab-section-title">Copies in Library</h3>
            <div class="copies-table">
              <!-- Head -->
              <div class="table-head">
                <span class="th">Barcode</span>
                <span class="th">Location</span>
                <span class="th">Status</span>
                <span class="th">Due Date</span>
              </div>
              <!-- Rows -->
              <div v-if="copies.length === 0" class="empty-row">No copies found</div>
              <div v-for="copy in copies" :key="copy.id" class="table-row">
                <span class="td td--mono">{{ copy.barcode }}</span>
                <span class="td">{{ copy.locationName || '—' }}</span>
                <span class="td">
                  <span class="status-badge" :style="{ background: statusBadgeColor(copy.status) }">
                    {{ copy.statusLabel }}
                  </span>
                </span>
                <span class="td">{{ copy.dueDate || '—' }}</span>
              </div>
            </div>
          </div>

          <!-- Reviews (placeholder) -->
          <div v-if="activeTab === 'Reviews'" class="tab-content">
            <h3 class="tab-section-title">Reviews</h3>
            <p class="tab-description tab-description--empty">Reviews coming soon.</p>
          </div>

          <!-- History (placeholder) -->
          <div v-if="activeTab === 'History'" class="tab-content">
            <h3 class="tab-section-title">Borrowing History</h3>
            <p class="tab-description tab-description--empty">Borrowing history coming soon.</p>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.book-detail {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary, #F7F8FA);
}

/* ===== Nav Bar (1440×66) ===== */
.nav {
  display: flex;
  align-items: center;
  padding: 16px 40px;
  background: var(--bg-primary, #FFFFFF);
  height: 66px;
}
.nav__logo {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  cursor: pointer;
}

/* ===== Main Content (padding [40,80], gap=28) ===== */
.main {
  padding: 40px 80px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  flex: 1;
}

.loading-msg { padding: 40px; text-align: center; color: var(--text-muted, #888); font-size: 14px; }
.error-msg { padding: 12px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger, #F87171); font-size: 13px; }

/* ===== Back Row ===== */
.back-row {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  width: fit-content;
}
.back-arrow {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 16px;
  color: var(--accent, #4A9FD8);
}
.back-link {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--accent, #4A9FD8);
}
.back-row:hover { opacity: 0.8; }

/* ===== Book Hero (horizontal, gap=40) ===== */
.book-hero {
  display: flex;
  gap: 40px;
}

/* Cover Section (240px) */
.cover-section {
  width: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.cover-box {
  width: 220px;
  height: 300px;
  border-radius: 12px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-icon { font-size: 64px; line-height: 1; }
.cover-isbn {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 11px;
  color: var(--text-muted, #888);
  margin: 0;
}

/* Book Info */
.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Title Section */
.title-section { display: flex; flex-direction: column; gap: 6px; }
.book-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}
.book-author {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 16px;
  color: var(--text-secondary, #666);
  margin: 0;
}

/* Meta Row */
.meta-row { display: flex; align-items: center; gap: 20px; }
.meta-rating {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 15px;
  color: var(--warning, #FBBF24);
}
.meta-reviews {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-muted, #888);
}

/* Tags */
.tag-row { display: flex; gap: 8px; flex-wrap: wrap; }
.tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--accent, #4A9FD8);
}

/* Availability */
.avail-row { display: flex; gap: 16px; }
.avail-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px;
  width: 120px;
  background: var(--bg-primary, #FFF);
  border-radius: 12px;
  border: 1px solid var(--border, #E5E7EB);
}
.avail-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  color: var(--text-muted, #888);
}
.avail-value {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 24px;
  font-weight: 600;
}
.avail-value--success { color: var(--success, #34D399); }
.avail-value--warning { color: var(--warning, #FBBF24); }

/* Actions */
.action-row { display: flex; gap: 12px; align-items: center; }
.btn-borrow {
  padding: 14px 28px;
  border-radius: 10px;
  background: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFF);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn-borrow:hover { opacity: 0.9; }

.btn-fav {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: var(--bg-primary, #FFF);
  border: 1px solid var(--border, #E5E7EB);
  font-size: 20px;
  color: var(--danger, #F87171);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.btn-fav:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

/* ===== Tabs ===== */
.tabs { display: flex; flex-direction: column; gap: 16px; }

.tab-bar { display: flex; gap: 24px; }
.tab { cursor: pointer; position: relative; display: flex; flex-direction: column; gap: 8px; transition: opacity 0.15s; }
.tab:hover { opacity: 0.8; }
.tab-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  padding: 0 4px 12px;
}
.tab--active .tab-label {
  font-weight: 600;
  color: var(--accent, #4A9FD8);
}
.tab:not(.tab--active) .tab-label {
  color: var(--text-secondary, #666);
}
.tab-indicator {
  width: 60px;
  height: 2px;
  background: var(--accent, #4A9FD8);
  border-radius: 1px;
  position: absolute;
  bottom: 0;
}

.tab-divider {
  width: 100%;
  height: 1px;
  background: var(--border, #E5E7EB);
  margin-top: -8px;
}

/* Tab Content */
.tab-content { display: flex; flex-direction: column; gap: 12px; }
.tab-section-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}
.tab-description {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary, #666);
  margin: 0;
  max-width: 800px;
}
.tab-description--empty {
  color: var(--text-muted, #888);
  font-style: italic;
}

/* ===== Copies Table ===== */
.copies-table {
  background: var(--bg-primary, #FFF);
  border-radius: 12px;
  border: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.table-head {
  display: flex;
  padding: 14px 16px;
  background: var(--bg-secondary, #F7F8FA);
}

.th {
  width: 160px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted, #888);
}

.table-row {
  display: flex;
  padding: 14px 16px;
  border-top: 0.5px solid var(--border, #E5E7EB);
  align-items: center;
}

.td {
  width: 160px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-secondary, #666);
}
.td--mono {
  font-family: var(--font-mono, 'Geist Mono', monospace);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 500;
  color: var(--text-inverse, #FFF);
  white-space: nowrap;
}

.empty-row {
  padding: 20px;
  text-align: center;
  color: var(--text-muted, #888);
  font-size: 13px;
}
</style>

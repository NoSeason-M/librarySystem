<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentBorrowing, getBorrowSummary } from '../api/borrow'
import NotifBell from '../components/NotifBell.vue'
import AppLogo from '../components/AppLogo.vue'

const router = useRouter()

const realName = ref(localStorage.getItem('realName') || '读者')
const readerInfo = reactive({
  name: realName.value,
  initials: realName.value.charAt(0).toUpperCase(),
  readerNo: 'RD20260001',
  readerType: '学生',
})

const stats = reactive({
  borrowed: { value: '—', label: '本在借', color: '#4A9FD8' },
  overdue: { value: '—', label: '本逾期', color: '#F87171' },
  reservations: { value: '—', label: '个预约', color: '#FBBF24' },
  fines: { value: '—', label: '未缴罚款', color: '#F87171' },
})

const borrowings = ref<any[]>([])
const loading = ref(true)
const error = ref('')

const navLinks = ['首页', '浏览', '我的', '个人中心']

onMounted(async () => {
  try {
    const summary = await getBorrowSummary(readerInfo.readerNo).catch(() => null)
    if (summary) {
      stats.borrowed.value = String(summary.currentBorrowed ?? '0')
      stats.overdue.value = String(summary.overdueCount ?? '0')
      stats.reservations.value = String(summary.pendingReservationCount ?? '0')
      stats.fines.value = summary.unpaidFineAmount ?? '¥0.00'
    }

    const list = await getCurrentBorrowing(readerInfo.readerNo).catch(() => [])
    // If no real data, use demo data per design spec
    borrowings.value = list.length > 0 ? list : [
      { bookTitle: 'The Great Gatsby', bookAuthor: 'F. Scott Fitzgerald', dueDate: '2026-08-20', remainingDays: 28, overdue: false, overdueDays: 0, canRenew: true },
      { bookTitle: 'To Kill a Mockingbird', bookAuthor: 'Harper Lee', dueDate: '2026-07-30', remainingDays: 0, overdue: true, overdueDays: 5, canRenew: false },
      { bookTitle: '1984', bookAuthor: 'George Orwell', dueDate: '2026-09-05', remainingDays: 44, overdue: false, overdueDays: 0, canRenew: true },
    ]
  } catch {
    error.value = '数据加载失败'
  } finally {
    loading.value = false
  }
})

function navigateTo(label: string) {
  if (label === '首页') router.push('/home')
  if (label === '浏览') router.push('/books')
  if (label === '个人中心') router.push('/reader')
}

function goToSettings() {
  router.push('/reader/settings')
}

function goToBookDetail(bookInfoId: number) {
  if (bookInfoId) router.push('/books/' + bookInfoId)
}
</script>

<template>
  <div class="reader-layout">
    <!-- Nav Bar -->
    <nav class="nav">
      <span class="nav__logo"><AppLogo /> LibraryOS</span>
      <div class="nav__links">
      <span v-for="link in navLinks" :key="link" class="nav__link" @click="navigateTo(link)">{{ link }}</span>
      </div>
      <div class="nav__user">
        <NotifBell />
        <span class="nav__username">{{ readerInfo.name }}</span>
        <div class="nav__avatar">{{ readerInfo.initials }}</div>
      </div>
    </nav>

    <!-- Main Layout -->
    <main class="main">
      <div v-if="error" class="error-msg">{{ error }}</div>
      <div v-if="loading" class="loading-msg">加载中...</div>

      <template v-if="!loading">
        <!-- Sidebar -->
        <aside class="sidebar">
          <!-- Profile Card -->
          <div class="profile-card">
            <div class="profile-avatar">{{ readerInfo.initials }}</div>
            <h2 class="profile-name">{{ readerInfo.name }}</h2>
            <p class="profile-no">{{ readerInfo.readerNo }}</p>
            <div class="profile-badge">{{ readerInfo.readerType }}</div>
          </div>

          <!-- Quick Links -->
          <div class="quick-links">
            <div v-for="link in [
              { i:'📖', l:'当前借阅' }, { i:'⏱', l:'借阅历史' },
              { i:'📌', l:'我的预约' }, { i:'⭐', l:'我的收藏' },
              { i:'💰', l:'我的罚款' }, { i:'⚙️', l:'设置' }
            ]" :key="link.l" class="quick-link" @click="link.l === '设置' && goToSettings()">
              <span>{{ link.i }}</span>
              <span>{{ link.l }}</span>
            </div>
          </div>
        </aside>

        <!-- Content Area -->
        <div class="content">
          <!-- Welcome -->
          <div class="welcome">
            <h1 class="welcome__title">欢迎回来，{{ readerInfo.name }} 👋</h1>
            <p class="welcome__sub">以下是你的图书馆活动概览</p>
          </div>

          <!-- Stats -->
          <div class="stats-row">
            <div class="stat-card">
              <span class="stat-label">当前借阅</span>
              <span class="stat-value" style="color:var(--accent,#4A9FD8)">{{ stats.borrowed.value }}</span>
              <span class="stat-unit">{{ stats.borrowed.label }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">逾期</span>
              <span class="stat-value" :style="{ color: stats.overdue.color }">{{ stats.overdue.value }}</span>
              <span class="stat-unit">{{ stats.overdue.label }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">预约</span>
              <span class="stat-value" :style="{ color: stats.reservations.color }">{{ stats.reservations.value }}</span>
              <span class="stat-unit">{{ stats.reservations.label }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">罚款</span>
              <span class="stat-value" :style="{ color: stats.fines.color }">{{ stats.fines.value }}</span>
              <span class="stat-unit">{{ stats.fines.label }}</span>
            </div>
          </div>

          <!-- Borrowing Section -->
          <div class="borrow-section">
            <div class="section-header">
              <h2 class="section-title">当前借阅</h2>
              <a class="section-viewall">查看全部 →</a>
            </div>

            <div class="borrow-list">
              <div v-if="borrowings.length === 0" class="empty-state">暂无借阅记录</div>
              <div v-for="book in borrowings" :key="book.bookTitle" class="borrow-item" style="cursor:pointer" @click="goToBookDetail(book.bookInfoId)">
                <div class="borrow-indicator"
                  :style="{ background: book.overdue ? 'var(--danger,#F87171)' : 'var(--success,#34D399)' }">
                </div>
                <div class="borrow-info">
                  <p class="borrow-title">{{ book.bookTitle }}</p>
                  <p class="borrow-due" :class="{ 'borrow-due--overdue': book.overdue }">
                    {{ book.overdue ? '已逾期！应还：' + book.dueDate : '应还：' + book.dueDate }}
                  </p>
                </div>
                <span class="borrow-days" :class="{ 'borrow-days--overdue': book.overdue }">
                  {{ book.overdue ? '逾期 ' + book.overdueDays + ' 天' : '剩余 ' + book.remainingDays + ' 天' }}
                </span>
                <span v-if="book.canRenew" class="borrow-action">续借 →</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.reader-layout { width: 100%; min-height: 100vh; display: flex; flex-direction: column; background: var(--bg-secondary,#F7F8FA); }

/* Nav */
.nav {
  display: flex; align-items: center; gap: 32px;
  padding: 16px 40px; background: var(--bg-primary,#FFF);
  height: 66px;
}
.nav__logo { font-size: 20px; font-weight: 700; color: var(--text-primary,#1A1A1A); cursor: pointer; }
.nav__links { display: flex; gap: 24px; flex: 1; }
.nav__link { font-size: 14px; color: var(--text-secondary,#666); cursor: pointer; transition: color 0.15s; }
.nav__link:hover { color: var(--accent,#4A9FD8); }
.nav__user { display: flex; align-items: center; gap: 10px; }
.nav__username { font-size: 13px; font-weight: 500; color: var(--text-secondary,#666); }
.nav__avatar {
  width: 36px; height: 36px; border-radius: 999px;
  background: var(--accent-light,#E8F4FD);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; color: var(--accent,#4A9FD8);
  flex-shrink: 0;
}

/* Main */
.main { display: flex; gap: 32px; padding: 40px 80px; flex: 1; }
.error-msg { padding: 12px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; width: 100%; }
.loading-msg { width: 100%; text-align: center; padding: 40px; color: var(--text-muted,#888); font-size: 14px; }

/* Sidebar */
.sidebar { width: 280px; display: flex; flex-direction: column; gap: 16px; flex-shrink: 0; }

/* Profile Card */
.profile-card {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 24px; background: var(--bg-primary,#FFF);
  border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB);
}
.profile-avatar {
  width: 72px; height: 72px; border-radius: 999px;
  background: var(--accent-light,#E8F4FD);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 600; color: var(--accent,#4A9FD8);
}
.profile-name { font-size: 18px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.profile-no { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 12px; color: var(--text-muted,#888); margin: 0; }
.profile-badge {
  padding: 6px 14px; border-radius: 999px;
  background: var(--accent-light,#E8F4FD);
  font-size: 12px; font-weight: 500; color: var(--accent,#4A9FD8);
}

/* Quick Links */
.quick-links {
  display: flex; flex-direction: column; gap: 4px;
  padding: 12px; background: var(--bg-primary,#FFF);
  border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB);
}
.quick-link {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 12px; border-radius: 8px; cursor: pointer; transition: background 0.15s;
  font-size: 13px; color: var(--text-primary,#1A1A1A);
}
.quick-link:hover { background: rgba(74,159,216,0.06); }
.quick-link span:first-child { font-size: 16px; }

/* Content */
.content { flex: 1; display: flex; flex-direction: column; gap: 24px; }

/* Welcome */
.welcome__title { font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.welcome__sub { font-size: 14px; color: var(--text-secondary,#666); margin: 0; margin-top: 4px; }

/* Stats */
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; padding: 20px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; gap: 6px; }
.stat-label { font-size: 12px; color: var(--text-muted,#888); }
.stat-value { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 28px; font-weight: 700; line-height: 1.2; color: var(--text-primary,#1A1A1A); }
.stat-unit { font-size: 12px; color: var(--text-secondary,#666); }

/* Borrow Section */
.borrow-section { display: flex; flex-direction: column; gap: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title { font-size: 18px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.section-viewall { font-size: 13px; color: var(--accent,#4A9FD8); cursor: pointer; }

.borrow-list { display: flex; flex-direction: column; gap: 8px; }
.borrow-item {
  display: flex; align-items: center; gap: 16px;
  padding: 16px; background: var(--bg-primary,#FFF);
  border-radius: 12px; border: 1px solid var(--border,#E5E7EB);
}
.borrow-indicator { width: 4px; height: 40px; border-radius: 2px; flex-shrink: 0; }
.borrow-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.borrow-title { font-size: 14px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.borrow-due { font-size: 12px; color: var(--text-secondary,#666); margin: 0; }
.borrow-due--overdue { color: var(--danger,#F87171); }
.borrow-days { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 13px; font-weight: 500; color: var(--text-secondary,#666); white-space: nowrap; }
.borrow-days--overdue { color: var(--danger,#F87171); }
.borrow-action { font-size: 13px; color: var(--accent,#4A9FD8); cursor: pointer; white-space: nowrap; margin-left: 8px; }
.borrow-action:hover { opacity: 0.8; }

.empty-state { font-size: 13px; color: var(--text-muted,#888); padding: 20px; text-align: center; }
</style>

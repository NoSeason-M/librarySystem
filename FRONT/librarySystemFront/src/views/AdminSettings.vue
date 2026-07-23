<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

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

const settingsCards = [
  { icon: 'U', title: '用户管理', desc: '创建、编辑和管理系统用户', color: '#4A9FD8', route: '/admin/settings/users', tags: ['创建', '编辑', '启用'] },
  { icon: 'R', title: '角色管理', desc: '配置角色和权限树', color: '#FBBF24', route: '/admin/settings/roles', tags: ['创建', '权限'] },
  { icon: 'M', title: '菜单管理', desc: '管理导航菜单和权限点', color: '#4A9FD8', route: '/admin/settings/menus', tags: ['添加', '编辑', '树形'] },
  { icon: 'C', title: '系统参数', desc: '配置借阅规则和系统设置', color: '#888888', route: '/admin/settings/config', tags: ['编辑参数'] },
  { icon: 'D', title: '数据字典', desc: '管理字典条目和分类', color: '#34D399', route: '/admin/settings/dicts', tags: ['添加', '编辑'] },
  { icon: 'L', title: '操作日志', desc: '查看系统审计轨迹（只追加）', color: '#F87171', route: '/admin/settings/logs', tags: ['查看', '筛选'] },
  { icon: 'A', title: '公告管理', desc: '发布公告和通知', color: '#FBBF24', route: '/admin/settings/announcements', tags: ['创建', '发布', '置顶'] },
  { icon: 'B', title: '数据备份', desc: '手动备份、下载和恢复', color: '#4A9FD8', route: '/admin/settings/backup', tags: ['立即备份', '下载'] },
]

function navigateTo(label: string) {
  if (label === 'Dashboard') router.push('/admin')
  else if (label === 'Books') router.push('/admin/books')
  else if (label === 'Readers') router.push('/admin/readers')
  else if (label === 'Statistics') router.push('/admin/statistics')
}

function goTo(card: any) {
  router.push(card.route)
}
</script>

<template>
  <div class="admin-settings">

    <main class="main">
      <header class="header">
        <h1 class="header__title">系统设置</h1>
      </header>

      <div class="card-grid">
        <!-- Row 1 -->
        <div class="card-row">
          <div v-for="card in settingsCards.slice(0, 3)" :key="card.title" class="settings-card" @click="goTo(card)">
            <span class="card-icon" :style="{ color: card.color }">{{ card.icon }}</span>
            <h3 class="card-title">{{ card.title }}</h3>
            <p class="card-desc">{{ card.desc }}</p>
            <div class="card-tags">
              <span v-for="tag in card.tags" :key="tag" class="card-tag">{{ tag }}</span>
            </div>
          </div>
        </div>
        <!-- Row 2 -->
        <div class="card-row">
          <div v-for="card in settingsCards.slice(3, 6)" :key="card.title" class="settings-card" @click="goTo(card)">
            <span class="card-icon" :style="{ color: card.color }">{{ card.icon }}</span>
            <h3 class="card-title">{{ card.title }}</h3>
            <p class="card-desc">{{ card.desc }}</p>
            <div class="card-tags">
              <span v-for="tag in card.tags" :key="tag" class="card-tag">{{ tag }}</span>
            </div>
          </div>
        </div>
        <!-- Row 3 -->
        <div class="card-row">
          <div v-for="card in settingsCards.slice(6, 8)" :key="card.title" class="settings-card settings-card--half" @click="goTo(card)">
            <span class="card-icon" :style="{ color: card.color }">{{ card.icon }}</span>
            <h3 class="card-title">{{ card.title }}</h3>
            <p class="card-desc">{{ card.desc }}</p>
            <div class="card-tags">
              <span v-for="tag in card.tags" :key="tag" class="card-tag">{{ tag }}</span>
            </div>
          </div>
          <div class="settings-card--empty"></div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-settings { display: flex; min-height: 100vh; flex: 1; width: 100%; background: var(--bg-secondary, #F7F8FA); }
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

.card-grid { display: flex; flex-direction: column; gap: 20px; }
.card-row { display: flex; gap: 20px; }

.settings-card {
  flex: 1;
  padding: 20px;
  background: var(--bg-primary, #FFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
  min-height: 140px;
}
.settings-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}
.settings-card--half { flex: 1; max-width: calc(50% - 10px); }
.settings-card--empty { flex: 1; max-width: calc(50% - 10px); }

.card-icon { font-family: var(--font-sans, Inter); font-size: 28px; font-weight: 700; height: 32px; }
.card-title { font-family: var(--font-sans, Inter); font-size: 15px; font-weight: 600; color: var(--text-primary, #1A1A1A); margin: 0; }
.card-desc { font-family: var(--font-sans, Inter); font-size: 12px; color: var(--text-muted, #888); margin: 0; line-height: 1.4; }
.card-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 4px; }
.card-tag { padding: 3px 8px; border-radius: 999px; background: var(--bg-secondary, #F7F8FA); font-family: var(--font-sans, Inter); font-size: 10px; font-weight: 500; color: var(--text-secondary, #666); }
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listConfigs, updateConfig } from '../api/system'

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

const configs = ref<any[]>([])
const loading = ref(false)
const editingId = ref(0)
const editValue = ref('')
const saving = ref(false)

async function loadConfigs() {
  loading.value = true
  try { const r = await listConfigs(); configs.value = r.records || [] } catch { configs.value = [] } finally { loading.value = false }
}
function startEdit(c: any) { editingId.value = c.id; editValue.value = c.configValue || '' }
function cancelEdit() { editingId.value = 0; editValue.value = '' }
async function saveEdit(id: number) {
  saving.value = true
  try { await updateConfig(id, { configValue: editValue.value }); editingId.value = 0; loadConfigs() } catch { alert('保存失败') } finally { saving.value = false }
}

const configGroups = [
  { name: '借阅规则', keys: ['borrow.max_books', 'borrow.days', 'borrow.renew_count', 'borrow.renew_days'] },
  { name: '罚款规则', keys: ['fine.overdue_rate', 'fine.damage_multiple', 'fine.lost_multiple'] },
  { name: '预约设置', keys: ['reservation.keep_hours'] },
  { name: '读者默认值', keys: ['reader.initial_password', 'reader.card_prefix'] },
  { name: '安全设置', keys: ['security.captcha', 'security.failed_limit'] },
  { name: '系统信息', keys: ['system.name', 'system.icp', 'system.contact_email'] },
]

const configLabels: Record<string, string> = {
  'borrow.max_books': '最大借阅数',
  'borrow.days': '默认借阅天数',
  'borrow.renew_count': '续借次数上限',
  'borrow.renew_days': '续借天数',
  'fine.overdue_rate': '逾期费率（元/天）',
  'fine.damage_multiple': '损坏赔偿倍数',
  'fine.lost_multiple': '丢失赔偿倍数',
  'reservation.keep_hours': '预约保留时长（小时）',
  'reader.initial_password': '读者初始密码',
  'reader.card_prefix': '读者证号前缀',
  'security.captcha': '启用验证码（1=是/0=否）',
  'security.failed_limit': '登录失败次数限制',
  'system.name': '系统名称',
  'system.icp': 'ICP 备案号',
  'system.contact_email': '联系邮箱',
}

function getConfig(key: string): any { return configs.value.find(c => c.configKey === key) }
function getConfigLabel(key: string): string { return configLabels[key] || key }
function getGroupConfigs(keys: string[]) { return keys.map(k => getConfig(k)).filter(Boolean) }

onMounted(() => { loadConfigs() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header"><div class="header-left"><button class="btn-back" @click="goBack">←</button><h1 class="header__title">系统参数</h1></div></header>
      <div v-if="loading" class="table-empty">加载中...</div>
      <div v-for="group in configGroups" :key="group.name" class="config-group">
        <h2 class="group-title">{{ group.name }}</h2>
        <div class="table">
          <div v-for="c in getGroupConfigs(group.keys)" :key="c.id" class="table-row">
            <div class="config-key">{{ getConfigLabel(c.configKey) }}</div>
            <div v-if="editingId !== c.id" class="config-value">{{ c.configValue || '—' }}</div>
            <div v-else class="config-edit-row"><div class="input-box"><input v-model="editValue" type="text" @keyup.enter="saveEdit(c.id)" /></div></div>
            <div class="config-actions">
              <button v-if="editingId !== c.id" class="btn-sm btn-sm--edit" @click="startEdit(c)">编辑</button>
              <template v-else>
                <button class="btn-sm btn-sm--edit" :disabled="saving" @click="saveEdit(c.id)">保存</button>
                <button class="btn-sm btn-sm--del" @click="cancelEdit">取消</button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.settings-page { display: flex; min-height: 100vh; background: var(--bg-secondary,#F7F8FA); }
.sidebar { width: 240px; background: var(--bg-inverse,#0A0A0A); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar__logo { display: flex; align-items: center; gap: 10px; padding: 20px; font-size: 22px; color: var(--text-inverse,#FFF); }
.sidebar__logo-text { font-family: var(--font-sans,Inter); font-size: 18px; font-weight: 700; }
.sidebar__nav { flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.sidebar__item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-muted,#888); transition: background 0.15s; }
.sidebar__item:hover { background: rgba(255,255,255,0.05); }
.sidebar__item-icon { font-size: 16px; width: 20px; text-align: center; }
.sidebar__bottom { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08); font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-inverse,#FFF); }
.sidebar__avatar { width: 32px; height: 32px; border-radius: 999px; background: var(--accent-light,#E8F4FD); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; color: var(--accent,#4A9FD8); flex-shrink: 0; }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.btn-back { width: 36px; height: 36px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; color: var(--text-secondary,#666); }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.table-empty { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 13px; }
.config-group { display: flex; flex-direction: column; gap: 8px; }
.group-title { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; padding: 8px 0 0; }
.table { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); display: flex; flex-direction: column; overflow: hidden; }
.table-row { display: flex; align-items: center; padding: 12px 20px; min-height: 48px; border-top: 0.5px solid var(--border,#E5E7EB); }
.config-key { width: 240px; font-family: var(--font-mono,'Geist Mono',monospace); font-size: 11px; color: var(--text-secondary,#666); flex-shrink: 0; }
.config-value { flex: 1; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.config-edit-row { flex: 1; display: flex; }
.config-actions { display: flex; gap: 6px; margin-left: 16px; }
.input-box { width: 100%; padding: 8px 12px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--accent,#4A9FD8); }
.input-box input { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.btn-sm { padding: 5px 10px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; }
.btn-sm--edit { background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); }
.btn-sm--del { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
</style>

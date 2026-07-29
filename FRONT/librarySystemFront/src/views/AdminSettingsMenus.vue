<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listMenus, createMenu, updateMenu, deleteMenu } from '../api/system'

const router = useRouter()
function goBack() { router.push('/admin/settings') }

const menus = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const modalMode = ref<'create'|'edit'>('create')
const editId = ref(0)
const form = ref({ name: '', permission: '', path: '', component: '', icon: '', type: 1, parentId: 0, sort: 0, visible: 1 })
const saving = ref(false)
const formError = ref('')

const typeOptions = [
  { value: 0, label: '目录', color: '#4A9FD8' },
  { value: 1, label: '菜单', color: '#34D399' },
  { value: 2, label: '按钮', color: '#888888' },
]

async function loadMenus() {
  loading.value = true
  try { menus.value = await listMenus() } catch { menus.value = [] } finally { loading.value = false }
}

function openCreate(parentId = 0, type = 1) {
  modalMode.value = 'create'; editId.value = 0
  form.value = { name: '', permission: '', path: '', component: '', icon: '', type, parentId, sort: 0, visible: 1 }
  formError.value = ''; showModal.value = true
}

function openEdit(m: any) {
  modalMode.value = 'edit'; editId.value = m.id
  form.value = { name: m.name, permission: m.permission || '', path: m.path || '', component: m.component || '', icon: m.icon || '', type: m.type, parentId: m.parentId || 0, sort: m.sort || 0, visible: m.visible ?? 1 }
  formError.value = ''; showModal.value = true
}

async function submitForm() {
  if (!form.value.name.trim()) { formError.value = '请输入名称'; return }
  saving.value = true; formError.value = ''
  try {
    if (modalMode.value === 'create') await createMenu(form.value)
    else await updateMenu(editId.value, form.value)
    showModal.value = false; loadMenus()
  } catch (err: any) { formError.value = err.message } finally { saving.value = false }
}

async function handleDelete(id: number) {
  if (!confirm('确认删除此菜单项？子项将一并删除')) return
  try { await deleteMenu(id); loadMenus() } catch { alert('删除失败') }
}

function getTypeLabel(t: number) { return typeOptions.find(o => o.value === t)?.label || '-' }
function getTypeColor(t: number) { return typeOptions.find(o => o.value === t)?.color || '#888' }

onMounted(() => { loadMenus() })
</script>

<template>
  <div class="settings-page">
    <main class="main">
      <header class="header">
        <div class="header-left"><button class="btn-back" @click="goBack">←</button><h1 class="header__title">菜单管理</h1></div>
        <button class="btn-primary" @click="openCreate(0, 0)">+ 添加一级菜单</button>
      </header>

      <div v-if="loading" class="loading-msg">加载中...</div>

      <template v-if="!loading">
        <!-- Tree view: iterate top-level directories -->
        <div v-for="dir in menus" :key="dir.id" class="menu-group">
          <!-- Level 0: Directory -->
          <div class="menu-item menu-item--dir">
            <div class="menu-item__info">
              <span class="menu-item__icon">📁</span>
              <span class="menu-item__name">{{ dir.name }}</span>
              <span class="menu-item__type menu-item__type--dir">目录</span>
              <span class="menu-item__path" v-if="dir.path">{{ dir.path }}</span>
            </div>
            <div class="menu-item__actions">
              <button class="btn-action" @click="openCreate(dir.id, 1)">+ 菜单</button>
              <button class="btn-action" @click="openEdit(dir)">编辑</button>
              <button class="btn-action btn-action--danger" @click="handleDelete(dir.id)">删除</button>
            </div>
          </div>

          <!-- Level 1: Child menus -->
          <div v-if="dir.children && dir.children.length > 0" class="menu-children">
            <div v-for="menu in dir.children" :key="menu.id" class="menu-item menu-item--menu">
              <div class="menu-item__info">
                <span class="menu-item__indent">├─</span>
                <span class="menu-item__icon">📄</span>
                <span class="menu-item__name">{{ menu.name }}</span>
                <span class="menu-item__type menu-item__type--menu">菜单</span>
                <span class="menu-item__path" v-if="menu.path">{{ menu.path }}</span>
              </div>
              <div class="menu-item__actions">
                <button class="btn-action" @click="openCreate(menu.id, 2)">+ 按钮</button>
                <button class="btn-action" @click="openEdit(menu)">编辑</button>
                <button class="btn-action btn-action--danger" @click="handleDelete(menu.id)">删除</button>
              </div>

              <!-- Level 2: Buttons -->
              <div v-if="menu.children && menu.children.length > 0" class="menu-buttons">
                <span v-for="btn in menu.children" :key="btn.id" class="menu-btn-tag">
                  <span class="menu-btn-perm">{{ btn.permission || btn.name }}</span>
                  <span class="menu-btn-del" @click="handleDelete(btn.id)">✕</span>
                </span>
                <span class="menu-btn-add" @click="openCreate(menu.id, 2)">+ 添加</span>
              </div>
            </div>
          </div>

          <div v-else class="menu-empty">暂无子菜单，<a @click="openCreate(dir.id, 1)">添加菜单</a></div>
        </div>

        <div v-if="menus.length === 0" class="loading-msg" style="padding:60px">暂无菜单数据</div>
      </template>
    </main>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal__header">
          <h2 class="modal__title">{{ modalMode === 'create' ? '新增' : '编辑' }}菜单项</h2>
          <button class="modal__close" @click="showModal = false">✕</button>
        </div>
        <div v-if="formError" class="modal__error">{{ formError }}</div>
        <div class="modal__body">
          <div class="form-row-2">
            <div class="field"><label class="field-label">名称 *</label><div class="input-box"><input v-model="form.name" type="text" placeholder="菜单名称" /></div></div>
            <div class="field"><label class="field-label">类型</label><div class="input-box select-box"><select v-model="form.type"><option :value="0">目录</option><option :value="1">菜单</option><option :value="2">按钮</option></select><span class="select-arrow">▼</span></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">权限标识</label><div class="input-box"><input v-model="form.permission" type="text" placeholder="如：book:create" /></div></div>
            <div class="field"><label class="field-label">路由路径</label><div class="input-box"><input v-model="form.path" type="text" placeholder="如：/admin/books" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">组件路径</label><div class="input-box"><input v-model="form.component" type="text" placeholder="如：/admin/books/BookList" /></div></div>
            <div class="field"><label class="field-label">图标</label><div class="input-box"><input v-model="form.icon" type="text" placeholder="如：Book" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">父级ID</label><div class="input-box"><input v-model.number="form.parentId" type="number" placeholder="0=顶级" /></div></div>
            <div class="field"><label class="field-label">排序号</label><div class="input-box"><input v-model.number="form.sort" type="number" placeholder="0" /></div></div>
          </div>
          <div class="form-row-2">
            <div class="field"><label class="field-label">可见</label><div class="input-box select-box"><select v-model="form.visible"><option :value="1">显示</option><option :value="0">隐藏</option></select><span class="select-arrow">▼</span></div></div>
            <div class="field"></div>
          </div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="showModal = false">取消</button>
          <button class="btn-primary" :disabled="saving" @click="submitForm">
            <span v-if="saving" class="spinner"></span><span v-else>{{ modalMode === 'create' ? '创建' : '保存' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-page { flex: 1; display: flex; flex-direction: column; }
.main { flex: 1; padding: 32px 40px; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.btn-back { width: 36px; height: 36px; border-radius: 10px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; color: var(--text-secondary,#666); }
.header__title { font-family: var(--font-sans,Inter); font-size: 24px; font-weight: 700; color: var(--text-primary,#1A1A1A); margin: 0; }
.loading-msg { padding: 40px; text-align: center; color: var(--text-muted,#888); font-size: 14px; }

/* ===== Menu Group ===== */
.menu-group { background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); border: 1px solid var(--border,#E5E7EB); overflow: hidden; }

/* ===== Menu Item ===== */
.menu-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; min-height: 48px; }

.menu-item--dir { background: var(--bg-primary,#FFF); }
.menu-item--menu { padding-left: 28px; border-top: 0.5px solid var(--border,#E5E7EB); background: var(--bg-secondary,#F7F8FA); }

.menu-item__info { display: flex; align-items: center; gap: 8px; flex: 1; min-width: 0; }
.menu-item__icon { font-size: 16px; width: 20px; text-align: center; flex-shrink: 0; }
.menu-item__indent { font-size: 12px; color: var(--text-muted,#888); font-family: var(--font-mono,'Geist Mono',monospace); width: 24px; flex-shrink: 0; }
.menu-item__name { font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; color: var(--text-primary,#1A1A1A); }
.menu-item--menu .menu-item__name { font-weight: 500; font-size: 13px; }
.menu-item__path { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 11px; color: var(--text-muted,#888); margin-left: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.menu-item__type { font-size: 10px; font-weight: 500; padding: 2px 8px; border-radius: 999px; color: #FFF; flex-shrink: 0; }
.menu-item__type--dir { background: #4A9FD8; }
.menu-item__type--menu { background: #34D399; }

.menu-item__actions { display: flex; gap: 4px; flex-shrink: 0; margin-left: 12px; }
.btn-action { padding: 4px 8px; border-radius: 6px; border: none; cursor: pointer; font-family: var(--font-sans,Inter); font-size: 10px; font-weight: 500; background: var(--accent-light,#E8F4FD); color: var(--accent,#4A9FD8); transition: opacity 0.15s; }
.btn-action:hover { opacity: 0.8; }
.btn-action--danger { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }

/* ===== Empty Children ===== */
.menu-empty { padding: 12px 28px 14px; font-family: var(--font-sans,Inter); font-size: 12px; color: var(--text-muted,#888); border-top: 0.5px solid var(--border,#E5E7EB); background: var(--bg-secondary,#F7F8FA); }
.menu-empty a { color: var(--accent,#4A9FD8); cursor: pointer; }

/* ===== Buttons Level ===== */
.menu-buttons { display: flex; flex-wrap: wrap; gap: 6px; padding: 10px 28px 12px; border-top: 0.5px solid var(--border,#E5E7EB); background: var(--bg-secondary,#F7F8FA); }
.menu-btn-tag { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 6px; background: var(--bg-primary,#FFF); border: 1px solid var(--border,#E5E7EB); font-family: var(--font-sans,Inter); font-size: 11px; color: var(--text-secondary,#666); }
.menu-btn-perm { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 10px; color: var(--text-muted,#888); }
.menu-btn-del { cursor: pointer; color: var(--danger,#F87171); font-size: 12px; line-height: 1; margin-left: 2px; }
.menu-btn-del:hover { opacity: 0.7; }
.menu-btn-add { font-family: var(--font-sans,Inter); font-size: 11px; color: var(--accent,#4A9FD8); cursor: pointer; padding: 4px 8px; border-radius: 6px; border: 1px dashed var(--border,#E5E7EB); transition: background 0.15s; }
.menu-btn-add:hover { background: var(--accent-light,#E8F4FD); border-color: var(--accent,#4A9FD8); }

.btn-primary { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; background: var(--accent,#4A9FD8); color: #FFF; font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 600; border: none; cursor: pointer; }
.btn-cancel { padding: 10px 24px; border-radius: 10px; background: var(--bg-primary,#FFF); color: var(--text-secondary,#666); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border,#E5E7EB); cursor: pointer; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 560px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; max-height: 90vh; overflow-y: auto; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; }
.modal__error { margin: 12px 28px 0; padding: 10px 14px; border-radius: 10px; background: rgba(248,113,113,0.1); color: var(--danger,#F87171); font-size: 13px; }
.modal__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }

.field { display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0; }
.field-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.input-box { width: 100%; padding: 10px 14px; border-radius: var(--input-radius,12px); background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--border,#E5E7EB); }
.input-box:focus-within { border-color: var(--accent,#4A9FD8); }
.input-box input, .input-box select { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.select-box { position: relative; padding: 0; }
.select-box select { padding: 10px 14px; cursor: pointer; appearance: none; }
.select-arrow { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); font-size: 10px; color: var(--text-muted,#888); pointer-events: none; }
.form-row-2 { display: flex; gap: 12px; }
.spinner { width: 16px; height: 16px; border: 2px solid #FFF; border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

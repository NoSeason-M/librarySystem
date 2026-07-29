<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyProfile, updateMyProfile } from '../api/readers'
import { changePassword } from '../api/auth'
import NotifBell from '../components/NotifBell.vue'
import AppLogo from '../components/AppLogo.vue'

const router = useRouter()

// Nav
const realName = ref(localStorage.getItem('realName') || '读者')
const userInitials = ref(realName.value.charAt(0).toUpperCase())

// Profile form
const profileForm = ref({ realName: '', email: '', phone: '' })
const readerNo = ref('')
const readerTypeName = ref('')
const profileSaving = ref(false)
const profileMsg = ref('')

// Password form
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdSaving = ref(false)
const pwdMsg = ref('')

// Notifications (local state only)
const notifSettings = ref([
  { label: '逾期提醒', desc: '图书逾期时发送通知', enabled: true },
  { label: '即将到期', desc: '到期前 3 天提醒', enabled: true },
  { label: '预约到书', desc: '预约图书已可领取', enabled: true },
  { label: '系统公告', desc: '图书馆发布的重要通知', enabled: false },
])

onMounted(async () => {
  try {
    const profile = await getMyProfile()
    profileForm.value.realName = profile.realName || ''
    profileForm.value.email = profile.email || ''
    profileForm.value.phone = profile.phone || ''
    readerNo.value = profile.readerNo || ''
    readerTypeName.value = profile.readerTypeName || ''
  } catch {
    // fallback to localStorage name
  }
})

function getInitials(name: string): string {
  return (name || '?').charAt(0)
}

async function saveProfile() {
  if (!profileForm.value.realName.trim()) {
    profileMsg.value = '姓名不能为空'
    return
  }
  profileSaving.value = true
  profileMsg.value = ''
  try {
    await updateMyProfile({
      realName: profileForm.value.realName.trim(),
      email: profileForm.value.email || undefined,
      phone: profileForm.value.phone || undefined,
    })
    localStorage.setItem('realName', profileForm.value.realName.trim())
    realName.value = profileForm.value.realName.trim()
    userInitials.value = getInitials(realName.value)
    profileMsg.value = '保存成功'
    setTimeout(() => { profileMsg.value = '' }, 2000)
  } catch (err: any) {
    profileMsg.value = err.message || '保存失败'
  } finally {
    profileSaving.value = false
  }
}

async function savePassword() {
  const { oldPassword, newPassword, confirmPassword } = pwdForm.value
  if (!oldPassword || !newPassword || !confirmPassword) {
    pwdMsg.value = '请填写所有密码字段'
    return
  }
  if (newPassword.length < 6) {
    pwdMsg.value = '新密码至少 6 位'
    return
  }
  if (newPassword !== confirmPassword) {
    pwdMsg.value = '两次密码输入不一致'
    return
  }
  pwdSaving.value = true
  pwdMsg.value = ''
  try {
    await changePassword({ oldPassword, newPassword, confirmPassword })
    pwdMsg.value = '密码修改成功'
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    setTimeout(() => { pwdMsg.value = '' }, 2000)
  } catch (err: any) {
    pwdMsg.value = err.message || '密码修改失败'
  } finally {
    pwdSaving.value = false
  }
}

function toggleNotif(index: number) {
  notifSettings.value[index].enabled = !notifSettings.value[index].enabled
}

function goToDashboard() {
  router.push('/reader')
}

function goHome() {
  router.push('/home')
}
</script>

<template>
  <div class="settings-page">
    <!-- Nav Bar -->
    <nav class="nav">
      <span class="nav__logo" @click="goHome"><AppLogo /> LibraryOS</span>
      <div class="nav__links">
        <span class="nav__link" @click="goHome">首页</span>
        <span class="nav__link" @click="router.push('/books')">浏览</span>
        <span class="nav__link" @click="goToDashboard">我的</span>
        <span class="nav__link nav__link--active">个人中心</span>
      </div>
      <div class="nav__user">
        <NotifBell />
        <span class="nav__username">{{ realName }}</span>
        <div class="nav__avatar" @click="goToDashboard">{{ userInitials }}</div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="main">
      <!-- Page Header -->
      <div class="page-header">
        <div class="page-header-row">
          <button class="btn-back" @click="goToDashboard">←</button>
          <h1 class="page-title">个人设置</h1>
        </div>
        <p class="page-subtitle">管理你的个人信息和偏好设置</p>
      </div>

      <!-- Two-column Content -->
      <div class="content-row">
        <!-- Left Column -->
        <div class="left-col">
          <!-- Profile Info Card -->
          <div class="card">
            <h2 class="card__title">个人信息</h2>

            <!-- Avatar + Basic Info -->
            <div class="info-row">
              <div class="avatar-large">{{ getInitials(profileForm.realName || realName) }}</div>
              <div class="name-group">
                <span class="display-name">{{ profileForm.realName || realName }}</span>
                <span class="reader-no">{{ readerNo }}</span>
              </div>
            </div>

            <!-- Form -->
            <div class="form-row">
              <div class="field">
                <label class="field-label">姓名</label>
                <div class="input-box">
                  <input v-model="profileForm.realName" type="text" placeholder="姓名" />
                </div>
              </div>
              <div class="field">
                <label class="field-label">邮箱</label>
                <div class="input-box">
                  <input v-model="profileForm.email" type="email" placeholder="邮箱" />
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="field">
                <label class="field-label">电话</label>
                <div class="input-box">
                  <input v-model="profileForm.phone" type="tel" placeholder="电话" />
                </div>
              </div>
              <div class="field">
                <label class="field-label">读者类型</label>
                <div class="input-box input-box--disabled">
                  <span class="input-disabled-text">{{ readerTypeName || '—' }}（不可更改）</span>
                </div>
              </div>
            </div>

            <!-- Save Button -->
            <div class="save-row">
              <div v-if="profileMsg" :class="['msg', profileMsg.includes('成功') ? 'msg--success' : 'msg--error']">{{ profileMsg }}</div>
              <button class="btn-primary" :disabled="profileSaving" @click="saveProfile">
                <span v-if="profileSaving" class="spinner"></span>
                <span v-else>保存修改</span>
              </button>
            </div>
          </div>

          <!-- Password Card -->
          <div class="card">
            <h2 class="card__title">修改密码</h2>

            <div class="form-row">
              <div class="field">
                <label class="field-label">当前密码</label>
                <div class="input-box">
                  <input v-model="pwdForm.oldPassword" type="password" placeholder="当前密码" />
                </div>
              </div>
              <div class="field">
                <label class="field-label">新密码</label>
                <div class="input-box">
                  <input v-model="pwdForm.newPassword" type="password" placeholder="新密码" />
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="field">
                <label class="field-label">确认新密码</label>
                <div class="input-box">
                  <input v-model="pwdForm.confirmPassword" type="password" placeholder="确认新密码" />
                </div>
              </div>
              <div class="field"></div>
            </div>

            <div class="save-row">
              <div v-if="pwdMsg" :class="['msg', pwdMsg.includes('成功') ? 'msg--success' : 'msg--error']">{{ pwdMsg }}</div>
              <button class="btn-secondary" :disabled="pwdSaving" @click="savePassword">
                <span v-if="pwdSaving" class="spinner"></span>
                <span v-else>修改密码</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="right-col">
          <!-- Notification Preferences -->
          <div class="card">
            <h2 class="card__title">通知偏好</h2>
            <p class="card__desc">选择你希望接收的通知类型</p>

            <div v-for="(item, index) in notifSettings" :key="item.label" class="notif-item">
              <div class="notif-text">
                <span class="notif-label">{{ item.label }}</span>
                <span class="notif-desc">{{ item.desc }}</span>
              </div>
              <div
                :class="['toggle', { 'toggle--on': item.enabled }]"
                @click="toggleNotif(index)"
              >
                <div class="toggle-dot"></div>
              </div>
            </div>
          </div>

          <!-- Quick Links -->
          <div class="card">
            <h2 class="card__title">快捷操作</h2>
            <div class="link-item" @click="router.push('/reader')">
              <span class="link-icon">📖</span>
              <span class="link-label">查看借阅记录</span>
            </div>
            <div class="link-item" @click="router.push('/reader')">
              <span class="link-icon">⭐</span>
              <span class="link-label">我的收藏</span>
            </div>
            <div class="link-item" @click="router.push('/reader')">
              <span class="link-icon">📌</span>
              <span class="link-label">我的预约</span>
            </div>
            <div class="link-item" @click="router.push('/reader')">
              <span class="link-icon">💬</span>
              <span class="link-label">联系客服</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.settings-page {
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
  font-weight: 600;
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

/* ===== Main ===== */
.main {
  padding: 32px 80px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  flex: 1;
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-back {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--bg-primary,#FFF);
  border: 1px solid var(--border,#E5E7EB);
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary,#666);
  flex-shrink: 0;
}

.btn-back:hover {
  background: var(--bg-secondary,#F7F8FA);
}

.page-title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}

.page-subtitle {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin: 0;
}

/* ===== Content Row ===== */
.content-row {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.left-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.right-col {
  width: 420px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex-shrink: 0;
}

/* ===== Card ===== */
.card {
  background: var(--bg-primary, #FFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card__title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}

.card__desc {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-secondary, #666);
  margin: 0;
}

/* ===== Avatar Row ===== */
.info-row {
  display: flex;
  gap: 20px;
  align-items: center;
}

.avatar-large {
  width: 72px;
  height: 72px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 28px;
  font-weight: 600;
  color: var(--accent, #4A9FD8);
  flex-shrink: 0;
}

.name-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.display-name {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
}

.reader-no {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 12px;
  color: var(--text-muted, #888);
}

/* ===== Form ===== */
.form-row {
  display: flex;
  gap: 16px;
}

.field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.field-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary, #1A1A1A);
}

.input-box {
  padding: 10px 14px;
  border-radius: var(--input-radius, 12px);
  background: var(--bg-secondary, #F7F8FA);
  border: 1.5px solid var(--border, #E5E7EB);
  transition: border-color 0.2s;
}

.input-box:focus-within {
  border-color: var(--accent, #4A9FD8);
  border-width: 2px;
}

.input-box input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
}

.input-box input::placeholder {
  color: var(--text-muted, #888);
}

.input-box--disabled {
  background: var(--bg-secondary, #F7F8FA);
  opacity: 0.7;
}

.input-disabled-text {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-muted, #888);
}

/* ===== Save Row ===== */
.save-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.msg {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 8px;
}

.msg--success {
  background: rgba(52, 211, 153, 0.1);
  color: var(--success, #34D399);
}

.msg--error {
  background: rgba(248, 113, 113, 0.1);
  color: var(--danger, #F87171);
}

/* ===== Buttons ===== */
.btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: var(--button-radius, 10px);
  background: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFF);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
  min-width: 120px;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: var(--button-radius, 10px);
  background: var(--bg-primary, #FFF);
  color: var(--text-secondary, #666);
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 500;
  border: 1.5px solid var(--border, #E5E7EB);
  cursor: pointer;
  transition: background 0.15s;
  min-width: 120px;
}

.btn-secondary:hover:not(:disabled) {
  background: var(--bg-secondary, #F7F8FA);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Notification Items ===== */
.notif-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.notif-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.notif-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1A1A1A);
}

.notif-desc {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-muted, #888);
}

/* ===== Toggle ===== */
.toggle {
  width: 40px;
  height: 22px;
  border-radius: 11px;
  background: var(--border, #E5E7EB);
  padding: 3px 4px;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  justify-content: flex-start;
}

.toggle--on {
  background: var(--accent, #4A9FD8);
  justify-content: flex-end;
}

.toggle-dot {
  width: 16px;
  height: 16px;
  border-radius: 8px;
  background: #FFFFFF;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* ===== Quick Links ===== */
.link-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  cursor: pointer;
  transition: opacity 0.15s;
}

.link-item:hover {
  opacity: 0.7;
}

.link-icon {
  font-size: 16px;
  line-height: 1;
  width: 20px;
  text-align: center;
  color: var(--text-secondary, #666);
}

.link-label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1A1A1A);
}
</style>

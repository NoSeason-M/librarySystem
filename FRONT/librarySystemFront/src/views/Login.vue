<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi, getCaptchaApi } from '../api/auth'

import AppLogo from '../components/AppLogo.vue'

const router = useRouter()

// ---------- Form State ----------
const form = reactive({
  username: '',
  password: '',
  captchaKey: '',
  captchaCode: '',
})

const loading = ref(false)
const errorMsg = ref('')
const rememberMe = ref(false)
const loginFailedCount = ref(0)
const showCaptcha = ref(false)

// Forgot password
const showForgotModal = ref(false)
const forgotEmail = ref('')
const forgotLoading = ref(false)
const forgotMsg = ref('')
const forgotError = ref('')
const newPassword = ref('')

async function handleForgotPassword() {
  if (!forgotEmail.value.trim()) { forgotError.value = '请输入邮箱地址'; return }
  forgotLoading.value = true
  forgotError.value = ''
  forgotMsg.value = ''
  newPassword.value = ''
  try {
    const http = await import('../api/index')
    const result: any = await http.default.post('/auth/forgot-password', { email: forgotEmail.value.trim() })
    forgotMsg.value = result.message || '密码已重置'
    newPassword.value = result.newPassword || ''
  } catch (err: any) {
    forgotError.value = err.message || '重置失败'
  } finally {
    forgotLoading.value = false
  }
}

function openForgotModal() {
  showForgotModal.value = true
  forgotEmail.value = ''
  forgotMsg.value = ''
  forgotError.value = ''
  newPassword.value = ''
}

function closeForgotModal() {
  showForgotModal.value = false
}

// ---------- Captcha ----------
const captchaImage = ref('')

async function loadCaptcha() {
  try {
    const data = await getCaptchaApi()
    captchaImage.value = data.captchaImage
    form.captchaKey = data.captchaKey
    form.captchaCode = ''
    showCaptcha.value = true
  } catch {
    // captcha server error — non-critical, login still works without it
    showCaptcha.value = false
  }
}

// ---------- Login ----------
async function handleLogin() {
  if (!form.username.trim()) {
    errorMsg.value = 'Please enter your username'
    return
  }
  if (!form.password) {
    errorMsg.value = 'Please enter your password'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const params: any = {
      username: form.username.trim(),
      password: form.password,
    }
    // Add captcha if visible
    if (showCaptcha.value) {
      params.captchaKey = form.captchaKey
      params.captchaCode = form.captchaCode
    }

    const data = await loginApi(params)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('username', data.username)
    localStorage.setItem('realName', data.realName)
    localStorage.setItem('roles', JSON.stringify(data.roles))

    loginFailedCount.value = 0
    // Redirect based on role
    if (data.roles.includes('ROLE_ADMIN') || data.roles.includes('ROLE_LIBRARIAN') || data.roles.includes('ROLE_CATALOGER')) {
      router.push('/admin')
    } else {
      router.push('/home')
    }
  } catch (err: any) {
    loginFailedCount.value++
    // Show captcha after 3 failed attempts (per PRD 5.2 security requirement)
    if (loginFailedCount.value >= 3) {
      loadCaptcha()
    }
    errorMsg.value = err.message || 'Login failed. Please check your credentials.'
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  router.push('/register')
}

// Pre-fill remembered username
onMounted(() => {
  const saved = localStorage.getItem('rememberedUsername')
  if (saved) {
    form.username = saved
    rememberMe.value = true
  }
})
</script>

<template>
  <div class="login-page">
    <!-- Left Panel -->
    <div class="left-panel">
      <div class="panel-top">
        <div class="logo"><AppLogo :size="28" /> LibraryOS</div>
        <div class="tagline">Next-generation library management system</div>
      </div>

      <div class="quote-area">
        <div class="quote-line quote-line--white">"A library is not a</div>
        <div class="quote-line quote-line--white">luxury but one of the</div>
        <div class="quote-line quote-line--accent">necessities of life."</div>
        <div class="quote-author">— Henry Ward Beecher</div>
      </div>

      <div class="panel-bottom">
        <div class="footer">© 2026 LibraryOS. All rights reserved.</div>
      </div>
    </div>

    <!-- Right Panel -->
    <div class="right-panel">
      <div class="form-card">
        <div class="form-header">
          <h1 class="form-title">Welcome back</h1>
          <p class="form-subtitle">Sign in to access the library system</p>
        </div>

        <!-- Error message -->
        <div v-if="errorMsg" class="error-banner">
          {{ errorMsg }}
        </div>

        <!-- Username -->
        <div class="field">
          <label class="field-label">Username</label>
          <div class="input-box">
            <input
              v-model="form.username"
              type="text"
              placeholder="Enter your username"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <!-- Password -->
        <div class="field">
          <label class="field-label">Password</label>
          <div class="input-box">
            <input
              v-model="form.password"
              type="password"
              placeholder="••••••••"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <!-- Captcha (shown after failed attempts) -->
        <div v-if="showCaptcha" class="field">
          <label class="field-label">Verification Code</label>
          <div class="captcha-row">
            <div class="input-box captcha-input">
              <input
                v-model="form.captchaCode"
                type="text"
                placeholder="Enter code"
                maxlength="4"
                @keyup.enter="handleLogin"
              />
            </div>
            <div class="captcha-image" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
              <span v-else class="captcha-fallback">A8K2</span>
            </div>
          </div>
        </div>

        <!-- Options -->
        <div class="options-row">
          <label class="remember-me">
            <div class="checkbox" :class="{ 'checkbox--checked': rememberMe }" @click="rememberMe = !rememberMe">
              <span v-if="rememberMe" class="check-mark">✓</span>
            </div>
            <span class="remember-label">Remember me</span>
          </label>
          <a class="forgot-password" href="#" @click.prevent="openForgotModal">Forgot password?</a>
        </div>

        <!-- Login Button -->
        <button class="btn-primary btn-full" :disabled="loading" @click="handleLogin">
          <span v-if="loading" class="spinner"></span>
          <span v-else>Sign In</span>
        </button>

        <!-- Register -->
        <div class="register-row">
          <span class="register-question">Don't have an account?</span>
          <a class="register-link" @click="goToRegister">Register here</a>
        </div>
      </div>

    </div>

    <!-- Forgot Password Modal -->
    <div v-if="showForgotModal" class="modal-overlay" @click.self="closeForgotModal">
      <div class="modal">
        <div class="modal__header">
          <h2 class="modal__title">Forgot Password</h2>
          <button class="modal__close" @click="closeForgotModal">✕</button>
        </div>
        <div class="modal__body">
          <p class="modal__desc">Enter your registered email address and we'll reset your password.</p>
          <div class="field">
            <label class="field-label">Email</label>
            <div class="input-box">
              <input v-model="forgotEmail" type="email" placeholder="you@example.com" @keyup.enter="handleForgotPassword" />
            </div>
          </div>
          <div v-if="forgotError" class="msg msg--error">{{ forgotError }}</div>
          <div v-if="forgotMsg" class="msg msg--success">
            <p>{{ forgotMsg }}</p>
            <p v-if="newPassword" class="new-pwd">New Password: <strong>{{ newPassword }}</strong></p>
            <p class="pwd-hint">Please change your password after logging in.</p>
          </div>
        </div>
        <div class="modal__footer">
          <button class="btn-cancel" @click="closeForgotModal">Cancel</button>
          <button class="btn-primary" :disabled="forgotLoading" @click="handleForgotPassword">
            <span v-if="forgotLoading" class="spinner"></span>
            <span v-else>Reset Password</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  background: linear-gradient(to right, #0A0A0A 0%, #3A3A3A 35%, #D8D8D8 65%, #F7F8FA 100%);
  padding: 24px;
  box-sizing: border-box;
}

/* ===== Left Panel ===== */
.left-panel {
  width: 560px;
  height: calc(100vh - 48px);
  background: rgba(10, 10, 10, 0.55);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px;
  flex-shrink: 0;
  border-radius: 16px 0 0 16px;
}

.panel-top {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.logo {
  font-family: var(--font-sans);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-inverse);
}

.tagline {
  font-size: 15px;
  color: var(--text-muted);
}

/* ===== Forgot Password Modal ===== */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 24px; }
.modal { width: 100%; max-width: 420px; background: var(--bg-primary,#FFF); border-radius: var(--card-radius,16px); display: flex; flex-direction: column; }
.modal__header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal__title { font-family: var(--font-sans,Inter); font-size: 20px; font-weight: 600; color: var(--text-primary,#1A1A1A); margin: 0; }
.modal__close { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-secondary,#F7F8FA); border: none; font-size: 14px; color: var(--text-muted,#888); cursor: pointer; display: flex; align-items: center; justify-content: center; }
.modal__close:hover { background: var(--border,#E5E7EB); }
.modal__body { padding: 20px 28px; display: flex; flex-direction: column; gap: 14px; }
.modal__footer { display: flex; gap: 12px; justify-content: flex-end; padding: 16px 28px 24px; }
.modal__desc { font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-secondary,#666); margin: 0; line-height: 1.5; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-family: var(--font-sans,Inter); font-size: 13px; font-weight: 500; color: var(--text-primary,#1A1A1A); }
.input-box { padding: 10px 14px; border-radius: var(--input-radius,12px); background: var(--bg-secondary,#F7F8FA); border: 1.5px solid var(--border,#E5E7EB); }
.input-box:focus-within { border-color: var(--accent,#4A9FD8); }
.input-box input { width: 100%; background: transparent; border: none; outline: none; font-family: var(--font-sans,Inter); font-size: 13px; color: var(--text-primary,#1A1A1A); }
.msg { padding: 10px 14px; border-radius: 8px; font-family: var(--font-sans,Inter); font-size: 13px; line-height: 1.5; }
.msg--error { background: rgba(248,113,113,0.1); color: var(--danger,#F87171); }
.msg--success { background: rgba(52,211,153,0.1); color: var(--success,#34D399); }
.new-pwd { font-family: var(--font-mono,'Geist Mono',monospace); font-size: 14px; margin: 4px 0; }
.pwd-hint { font-size: 12px; opacity: 0.7; margin: 2px 0 0; }
.btn-cancel { padding: 10px 24px; border-radius: var(--button-radius,10px); background: var(--bg-primary,#FFF); color: var(--text-secondary,#666); font-family: var(--font-sans,Inter); font-size: 14px; font-weight: 500; border: 1.5px solid var(--border,#E5E7EB); cursor: pointer; }
.btn-cancel:hover { background: var(--bg-secondary,#F7F8FA); }

.quote-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quote-line {
  font-size: 36px;
  font-weight: 500;
  line-height: 1.2;
}

.quote-line--white {
  color: var(--text-inverse);
}

.quote-line--accent {
  color: var(--accent);
}

.quote-author {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 8px;
}

.panel-bottom {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.footer {
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== Right Panel ===== */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 0 16px 16px 0;
}

.form-card {
  width: 100%;
  max-width: 400px;
  background: var(--bg-primary);
  border-radius: var(--card-radius);
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.form-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.error-banner {
  background: rgba(248, 113, 113, 0.1);
  border: 1px solid var(--danger);
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--danger);
}

/* ===== Field ===== */
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.input-box {
  width: 100%;
  padding: 12px 16px;
  border-radius: var(--input-radius);
  background: var(--bg-secondary);
  border: 1.5px solid var(--border);
  transition: border-color 0.2s, background 0.2s;
}

.input-box:focus-within {
  background: var(--bg-primary);
  border-color: var(--accent);
  border-width: 2px;
}

.input-box input {
  font-size: 14px;
  color: var(--text-primary);
  background: transparent;
  width: 100%;
  border: none;
  outline: none;
}

.input-box input::placeholder {
  color: var(--text-muted);
}

/* ===== Captcha ===== */
.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  width: 160px;
  flex-shrink: 0;
}

.captcha-image {
  flex: 1;
  height: 45px;
  border-radius: 10px;
  background: var(--accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.captcha-image img {
  height: 100%;
  width: 100%;
  object-fit: cover;
}

.captcha-fallback {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
  letter-spacing: 6px;
}

/* ===== Options Row ===== */
.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  border: 1.5px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.checkbox--checked {
  background: var(--accent);
  border-color: var(--accent);
}

.check-mark {
  font-size: 11px;
  color: var(--text-inverse);
  font-weight: 600;
}

.remember-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.forgot-password {
  font-size: 13px;
  font-weight: 500;
  color: var(--accent);
  text-decoration: none;
}

/* ===== Button ===== */
.btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 12px 24px;
  border-radius: var(--button-radius);
  background: var(--accent);
  color: var(--text-inverse);
  font-size: 15px;
  font-weight: 600;
  transition: opacity 0.2s;
  border: none;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--text-inverse);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Register Row ===== */
.register-row {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.register-question {
  font-size: 13px;
  color: var(--text-muted);
}

.register-link {
  font-size: 13px;
  font-weight: 500;
  color: var(--accent);
  cursor: pointer;
}

.register-link:hover {
  opacity: 0.8;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .left-panel {
    display: none;
  }
  .right-panel {
    padding: 24px;
  }
  .form-card {
    padding: 28px;
  }
}
</style>

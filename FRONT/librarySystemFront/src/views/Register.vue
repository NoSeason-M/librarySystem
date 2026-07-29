<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '../api/auth'
import type { LoginResult } from '../api/auth'
import AppLogo from '../components/AppLogo.vue'

const router = useRouter()

const form = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  readerType: 'STUDENT',
  password: '',
  confirmPassword: '',
  agreeTerms: false,
})

const loading = ref(false)
const errorMsg = ref('')
const registered = ref<LoginResult | null>(null)

const readerTypes = [
  { value: 'STUDENT', label: 'Student' },
  { value: 'TEACHER', label: 'Teacher' },
  { value: 'STAFF', label: 'Staff' },
  { value: 'EXTERNAL', label: 'External' },
]

function validate(): boolean {
  if (!form.username.trim()) {
    errorMsg.value = 'Please enter your username'
    return false
  }
  if (!form.realName.trim()) {
    errorMsg.value = 'Please enter your real name'
    return false
  }
  if (!form.email.trim()) {
    errorMsg.value = 'Please enter your email'
    return false
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errorMsg.value = 'Please enter a valid email address'
    return false
  }
  if (!form.password) {
    errorMsg.value = 'Please enter a password'
    return false
  }
  if (form.password.length < 6) {
    errorMsg.value = 'Password must be at least 6 characters'
    return false
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = 'Passwords do not match'
    return false
  }
  if (!form.agreeTerms) {
    errorMsg.value = 'Please agree to the Terms of Service'
    return false
  }
  return true
}

async function handleRegister() {
  errorMsg.value = ''
  registered.value = null
  if (!validate()) return

  loading.value = true
  try {
    const result = await registerApi({
      username: form.username.trim(),
      realName: form.realName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      readerType: form.readerType,
      password: form.password,
      confirmPassword: form.confirmPassword,
    })
    registered.value = result
    // Auto-redirect to login after 3 seconds
    setTimeout(() => {
      router.push('/login')
    }, 3000)
  } catch (err: any) {
    errorMsg.value = err.message || 'Registration failed. Please try again.'
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="register-page">
    <!-- Left Panel -->
    <div class="left-panel">
      <div class="panel-top">
        <div class="logo"><AppLogo :size="28" /> LibraryOS</div>
        <div class="tagline">Next-generation library management system</div>
      </div>

      <div class="benefits">
        <div class="benefit-item">
          <span class="benefit-icon"><svg viewBox="0 0 1024 1024" width="28" height="28" style="vertical-align:middle"><path d="M351.1808 59.2896A435.2 435.2 0 0 1 805.376 715.264 460.8 460.8 0 0 1 351.1808 59.3408z" fill="#20C997"/><path d="M754.3808 722.2272a358.4 358.4 0 1 0-267.8272 120.2176 51.2 51.2 0 0 1 0 102.4 460.8 460.8 0 1 1 365.1584-179.712l118.8864 121.2416c23.7568 24.2176 23.552 63.0272-0.4096 87.04l-0.4096 0.4096a61.184 61.184 0 0 1-86.9888-0.4608l-148.0192-150.9376a61.7984 61.7984 0 0 1 0.4096-86.9888l0.4096-0.4096c5.632-5.5808 11.9808-9.8304 18.7904-12.8z m-467.968-364.5952h409.6a51.2 51.2 0 1 1 0 102.4h-409.6a51.2 51.2 0 1 1 0-102.4z m0 204.8h256a51.2 51.2 0 0 1 0 102.4h-256a51.2 51.2 0 1 1 0-102.4z" fill="#2C6DD2"/></svg></span>
          <div class="benefit-text">
            <div class="benefit-title">Search Thousands of Books</div>
            <div class="benefit-desc">Access our extensive collection</div>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon"><svg viewBox="0 0 1024 1024" width="28" height="28" style="vertical-align:middle"><path d="M808.721067 47.650133c0-26.350933-22.391467-47.650133-50.039467-47.650133-27.648 0-50.039467 21.2992-50.039467 47.650133v95.232c0 26.282667 22.391467 47.650133 50.039467 47.650134 27.648 0 50.039467-21.367467 50.039467-47.650134V47.650133zM563.370667 47.650133C563.370667 21.2992 540.910933 0 513.3312 0c-27.648 0-50.039467 21.2992-50.039467 47.650133v95.232c0 26.282667 22.391467 47.650133 50.039467 47.650134 27.579733 0 50.039467-21.367467 50.039467-47.650134V47.650133zM314.743467 47.650133C314.743467 21.2992 292.352 0 264.704 0c-27.648 0-50.039467 21.2992-50.039467 47.650133v95.232c0 26.282667 22.391467 47.650133 50.039467 47.650134 27.648 0 50.039467-21.367467 50.039467-47.650134V47.650133z" fill="#1296db"/><path d="M940.1344 96.8704h-82.397867v47.581867c0 52.565333-44.2368 95.232-98.850133 95.232-54.613333 0-98.850133-42.666667-98.850133-95.232V96.938667h-49.493334v47.581866c0 52.565333-44.2368 95.232-98.850133 95.232-54.613333 0-98.850133-42.666667-98.850133-95.232V96.938667h-49.425067v47.581866c0 52.565333-44.2368 95.232-98.9184 95.232-54.613333 0-98.850133-42.666667-98.850133-95.232V96.938667H83.2512c-27.306667 0-49.425067 21.2992-49.425067 47.581866v825.002667c0 26.282667 22.1184 47.650133 49.425067 47.650133h856.8832c27.306667 0 49.425067-21.367467 49.425067-47.650133V144.452267a48.5376 48.5376 0 0 0-49.425067-47.581867zM305.937067 864.938667c0 22.3232-18.500267 40.3456-41.233067 40.3456a40.7552 40.7552 0 0 1-41.233067-40.277334v-107.656533c0-22.254933 18.432-40.277333 41.233067-40.277333 22.7328 0 41.1648 18.0224 41.1648 40.277333v107.588267z m164.864 0c0 22.3232-18.432 40.3456-41.233067 40.3456a40.7552 40.7552 0 0 1-41.233067-40.277334V649.762133c0-22.3232 18.432-40.3456 41.233067-40.3456 22.801067 0 41.233067 18.0224 41.233067 40.277334v215.176533z m164.864 0c0 22.3232-18.432 40.3456-41.1648 40.3456a40.7552 40.7552 0 0 1-41.233067-40.277334V542.173867c0-22.3232 18.432-40.3456 41.233067-40.3456 22.801067 0 41.1648 18.0224 41.1648 40.277333v322.7648z m164.932266 0c0 22.3232-18.432 40.3456-41.233066 40.3456a40.823467 40.823467 0 0 1-41.1648-40.277334V434.5856c0-22.3232 18.432-40.3456 41.1648-40.3456 22.801067 0 41.233067 18.0224 41.233066 40.277333v430.353067z" fill="#1296db"/></svg></span>
          <div class="benefit-text">
            <div class="benefit-title">Manage Borrowings Online</div>
            <div class="benefit-desc">Renew, reserve, track all in one place</div>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon"><svg viewBox="0 0 1024 1024" width="28" height="28" style="vertical-align:middle"><path d="M544 428.8V275.2a38.4 38.4 0 1 0-76.8 0v192a38.4 38.4 0 0 0 38.4 38.4h128a38.4 38.4 0 0 0 0-76.8H544zM188.3584 328.5504C148.9344 310.2528 121.6 270.3168 121.6 224c0-63.6224 51.5776-115.2 115.2-115.2 41.2672 0 77.4592 21.696 97.8048 54.304a353.3888 353.3888 0 0 0-146.2464 165.4464z m501.0368-165.4464C709.7408 130.496 745.9328 108.8 787.2 108.8c63.6224 0 115.2 51.5776 115.2 115.2 0 46.3168-27.3344 86.2528-66.7584 104.5504a353.3888 353.3888 0 0 0-146.2464-165.4464zM512 780.8c-173.1968 0-313.6-140.4032-313.6-313.6s140.4032-313.6 313.6-313.6 313.6 140.4032 313.6 313.6-140.4032 313.6-313.6 313.6z m-179.8592 145.2288a38.4 38.4 0 0 1-24.2816-72.8576C375.7184 830.5472 443.8144 819.2 512 819.2s136.2816 11.3472 204.1408 33.9712a38.4 38.4 0 0 1-24.2816 72.8576C631.7184 905.984 571.8144 896 512 896s-119.7184 9.984-179.8592 30.0288z" fill="#1296db"/></svg></span>
          <div class="benefit-text">
            <div class="benefit-title">Get Smart Reminders</div>
            <div class="benefit-desc">Never miss a due date again</div>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon"><svg viewBox="0 0 1024 1024" width="28" height="28" style="vertical-align:middle"><path d="M900.7 908.5H271c-105.9 0-191.7-85.8-191.7-191.7V168.2c0-26.6 21.5-48.1 48.1-48.1s48.1 21.5 48.1 48.1v548.6c0 52.8 42.8 95.6 95.6 95.6h629.7c26.6 0 48.1 21.5 48.1 48.1-0.1 26.5-21.7 48-48.2 48z" fill="#BDD2EF"/><path d="M386.9 369.8H285.5c-20.9 0-37.9 17-37.9 37.9V683c0 20.9 17 37.9 37.9 37.9h101.4c20.9 0 37.9-17 37.9-37.9V407.8c0-21-16.9-38-37.9-38zM631 117.9h-77.8c-27.5 0-49.7 22.3-49.7 49.7v503.6c0 27.4 22.3 49.7 49.7 49.7H631c27.4 0 49.7-22.3 49.7-49.7V167.6c0-27.5-22.2-49.7-49.7-49.7zM894.1 278.6H802c-23.5 0-42.6 19.1-42.6 42.6v357.1c0 23.5 19.1 42.6 42.6 42.6h92.1c23.5 0 42.6-19.1 42.6-42.6V321.2c-0.1-23.6-19.1-42.6-42.6-42.6z" fill="#2867CE"/></svg></span>
          <div class="benefit-text">
            <div class="benefit-title">View Reading Analytics</div>
            <div class="benefit-desc">Track your reading journey</div>
          </div>
        </div>
      </div>

      <div class="panel-bottom">
        <div class="footer">© 2026 LibraryOS. All rights reserved.</div>
      </div>
    </div>

    <!-- Right Panel -->
    <div class="right-panel">
      <div class="form-card">
        <div class="form-header">
          <h1 class="form-title">Create an Account</h1>
          <p class="form-subtitle">Join our library community</p>
        </div>

        <!-- Error messages -->
        <div v-if="errorMsg" class="msg msg--error">{{ errorMsg }}</div>

        <!-- Registration Success Result -->
        <div v-if="registered" class="register-result">
          <div class="register-result__icon">✅</div>
          <h2 class="register-result__title">Registration Successful!</h2>
          <div class="register-result__item">
            <span class="register-result__label">Your Username:</span>
            <span class="register-result__value">{{ registered.username }}</span>
          </div>
          <div class="register-result__item">
            <span class="register-result__label">Your Password:</span>
            <span class="register-result__value">{{ form.password }}</span>
          </div>
          <p class="register-result__hint">Redirecting to login page...</p>
          <button class="btn-primary btn-full" @click="router.push('/login')">Go to Login</button>
        </div>

        <!-- Form fields (hidden after registration) -->
        <template v-if="!registered">
        <!-- Username Row -->
        <div class="row-2col">
          <div class="field">
            <label class="field-label">Username</label>
            <div class="input-box">
              <input v-model="form.username" type="text" placeholder="eg: zhangsan" />
            </div>
          </div>
          <div class="field">
            <label class="field-label">Real Name</label>
            <div class="input-box">
              <input v-model="form.realName" type="text" placeholder="eg: Zhang San" />
            </div>
          </div>
        </div>

        <!-- Email -->
        <div class="field">
          <label class="field-label">Email</label>
          <div class="input-box">
            <input v-model="form.email" type="email" placeholder="you@example.com" />
          </div>
        </div>

        <!-- Phone -->
        <div class="field">
          <label class="field-label">Phone Number</label>
          <div class="input-box">
            <input v-model="form.phone" type="tel" placeholder="+86 138 0000 0000" />
          </div>
        </div>

        <!-- Reader Type + Password Row -->
        <div class="row-2col">
          <div class="field">
            <label class="field-label">Reader Type</label>
            <div class="input-box select-box">
              <select v-model="form.readerType">
                <option v-for="t in readerTypes" :key="t.value" :value="t.value">
                  {{ t.label }}
                </option>
              </select>
              <span class="select-arrow">▼</span>
            </div>
          </div>
          <div class="field">
            <label class="field-label">Password</label>
            <div class="input-box">
              <input v-model="form.password" type="password" placeholder="••••••••" />
            </div>
          </div>
        </div>

        <!-- Confirm Password -->
        <div class="field">
          <label class="field-label">Confirm Password</label>
          <div class="input-box">
            <input v-model="form.confirmPassword" type="password" placeholder="••••••••" />
          </div>
        </div>

        <!-- Terms -->
        <label class="terms-row">
          <div class="checkbox" :class="{ 'checkbox--checked': form.agreeTerms }" @click="form.agreeTerms = !form.agreeTerms">
            <span v-if="form.agreeTerms" class="check-mark">✓</span>
          </div>
          <span class="terms-text">I agree to the Terms of Service and Privacy Policy</span>
        </label>

        <!-- Register Button -->
        <button class="btn-primary btn-full" :disabled="loading" @click="handleRegister">
          <span v-if="loading" class="spinner"></span>
          <span v-else>Create Account</span>
        </button>

        <!-- Login Link -->
        <div class="login-row">
          <span class="login-question">Already have an account?</span>
          <a class="login-link" @click="goToLogin">Sign in</a>
        </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  width: 100%;
  height: 100vh;
  display: flex;
  background: linear-gradient(to right, #0A0A0A 0%, #3A3A3A 35%, #D8D8D8 65%, #F7F8FA 100%);
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}

/* ===== Left Panel ===== */
.left-panel {
  width: 560px;
  height: calc(100vh - 48px);
  background: rgba(10, 10, 10, 0.55);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 60px;
  flex-shrink: 0;
  border-radius: 16px 0 0 16px;
}

.panel-top {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.logo {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-inverse);
}

.tagline {
  font-size: 15px;
  color: var(--text-muted);
}

.benefits {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.benefit-item {
  display: flex;
  gap: 14px;
  align-items: center;
}

.benefit-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.benefit-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.benefit-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-inverse);
}

.benefit-desc {
  font-size: 12px;
  color: var(--text-muted);
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
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 0 16px 16px 0;
}

.form-card {
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: var(--card-radius);
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: calc(100vh - 120px);
}

/* ===== Registration Result ===== */
.register-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}
.register-result__icon { font-size: 48px; }
.register-result__title {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin: 0;
}
.register-result__item {
  display: flex;
  gap: 8px;
  align-items: center;
  background: var(--bg-secondary, #F7F8FA);
  padding: 10px 16px;
  border-radius: 10px;
  width: 100%;
}
.register-result__label {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 13px;
  color: var(--text-muted, #888888);
  min-width: 110px;
}
.register-result__value {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 14px;
  font-weight: 600;
  color: var(--accent, #4A9FD8);
}
.register-result__hint {
  font-family: var(--font-sans, 'Inter', sans-serif);
  font-size: 12px;
  color: var(--text-muted, #888888);
  margin: 0;
}

.form-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

/* ===== Messages ===== */
.msg {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
}
.msg--error {
  background: rgba(248, 113, 113, 0.1);
  border: 1px solid var(--danger);
  color: var(--danger);
}
.msg--success {
  background: rgba(52, 211, 153, 0.1);
  border: 1px solid var(--success);
  color: var(--success);
}

/* ===== Two Column Row ===== */
.row-2col {
  display: flex;
  gap: 12px;
}

.row-2col .field {
  flex: 1;
  min-width: 0;
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
  padding: 10px 14px;
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
  font-size: 13px;
  color: var(--text-primary);
  background: transparent;
  width: 100%;
  border: none;
  outline: none;
}

.input-box input::placeholder {
  color: var(--text-muted);
}

/* ===== Select ===== */
.select-box {
  position: relative;
  padding: 0;
}

.select-box select {
  width: 100%;
  height: 100%;
  padding: 10px 14px;
  border: none;
  outline: none;
  background: transparent;
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
}

.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--text-muted);
  pointer-events: none;
}

/* ===== Terms ===== */
.terms-row {
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

.terms-text {
  font-size: 12px;
  color: var(--text-secondary);
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

/* ===== Login Row ===== */
.login-row {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.login-question {
  font-size: 13px;
  color: var(--text-muted);
}

.login-link {
  font-size: 13px;
  font-weight: 500;
  color: var(--accent);
  cursor: pointer;
}

.login-link:hover {
  opacity: 0.8;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .left-panel { display: none; }
  .right-panel { padding: 24px; }
  .form-card { padding: 28px; }
  .row-2col { flex-direction: column; gap: 18px; }
}
</style>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '../api/auth'
import type { LoginResult } from '../api/auth'

const router = useRouter()

const form = reactive({
  firstName: '',
  lastName: '',
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
  if (!form.firstName.trim() || !form.lastName.trim()) {
    errorMsg.value = 'Please enter your full name'
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
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
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
        <div class="logo">📚 LibraryOS</div>
        <div class="tagline">Next-generation library management system</div>
      </div>

      <div class="benefits">
        <div class="benefit-item">
          <span class="benefit-icon">🔍</span>
          <div class="benefit-text">
            <div class="benefit-title">Search Thousands of Books</div>
            <div class="benefit-desc">Access our extensive collection</div>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">📱</span>
          <div class="benefit-text">
            <div class="benefit-title">Manage Borrowings Online</div>
            <div class="benefit-desc">Renew, reserve, track all in one place</div>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">⏰</span>
          <div class="benefit-text">
            <div class="benefit-title">Get Smart Reminders</div>
            <div class="benefit-desc">Never miss a due date again</div>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">📊</span>
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
        <!-- Name Row -->
        <div class="row-2col">
          <div class="field">
            <label class="field-label">First Name</label>
            <div class="input-box">
              <input v-model="form.firstName" type="text" placeholder="John" />
            </div>
          </div>
          <div class="field">
            <label class="field-label">Last Name</label>
            <div class="input-box">
              <input v-model="form.lastName" type="text" placeholder="Doe" />
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
  min-height: 100vh;
  display: flex;
  background: var(--bg-secondary);
  padding: 24px;
  box-sizing: border-box;
}

/* ===== Left Panel ===== */
.left-panel {
  width: 560px;
  height: calc(100vh - 48px);
  background: var(--bg-inverse);
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
  gap: 24px;
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
  background: var(--bg-primary);
  border-radius: 0 16px 16px 0;
}

.form-card {
  width: 100%;
  max-width: 440px;
  background: var(--bg-primary);
  border-radius: var(--card-radius);
  padding: 36px;
  display: flex;
  flex-direction: column;
  gap: 18px;
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

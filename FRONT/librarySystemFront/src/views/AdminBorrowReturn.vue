<script setup lang="ts">
import { ref, computed } from 'vue'
import { checkReader, checkBarcode, borrowBooks, findReturn, returnBooks } from '../api/borrow'

// ======== No tab state — both panels always visible ========

// ======== Borrow state ========
const readerNoInput = ref('')
const readerInfo = ref<any>(null)
const readerLoading = ref(false)
const barcodeInput = ref('')
const scannedBooks = ref<any[]>([])
const borrowLoading = ref(false)
const borrowResult = ref<any>(null)

// ======== Return state ========
const returnBarcodeInput = ref('')
const returnInfo = ref<any>(null)
const returnLoading = ref(false)
const returnResult = ref<any>(null)

const availableSlots = computed(() => {
  if (!readerInfo.value) return 0
  return readerInfo.value.maxBorrow - readerInfo.value.currentBorrowed - scannedBooks.value.length
})

// ======== Borrow: Lookup Reader ========
async function lookupReader() {
  const no = readerNoInput.value.trim()
  if (!no) return
  readerLoading.value = true
  readerInfo.value = null
  scannedBooks.value = []
  borrowResult.value = null
  try {
    readerInfo.value = await checkReader(no)
  } catch (e: any) {
    alert(e.message || 'Reader not found')
  } finally {
    readerLoading.value = false
  }
}

// ======== Borrow: Scan Barcode ========
async function addBook() {
  const bc = barcodeInput.value.trim()
  if (!bc) return
  // Check duplicate
  if (scannedBooks.value.some(b => b.barcode === bc)) {
    alert('Book already scanned')
    return
  }
  // Check slots
  if (scannedBooks.value.length >= (readerInfo.value?.maxBorrow || 5) - (readerInfo.value?.currentBorrowed || 0)) {
    alert('No more slots available')
    return
  }
  try {
    const book = await checkBarcode(bc)
    scannedBooks.value.push(book)
    barcodeInput.value = ''
  } catch (e: any) {
    alert(e.message || 'Book not available')
  }
}

function removeBook(barcode: string) {
  scannedBooks.value = scannedBooks.value.filter(b => b.barcode !== barcode)
}

// ======== Borrow: Confirm ========
async function confirmBorrow() {
  if (!readerInfo.value || scannedBooks.value.length === 0) return
  borrowLoading.value = true
  borrowResult.value = null
  try {
    const result = await borrowBooks(
      readerInfo.value.readerNo,
      scannedBooks.value.map(b => b.barcode)
    )
    borrowResult.value = result
    // Reset
    scannedBooks.value = []
    // Reload reader info to get updated counts
    readerInfo.value = await checkReader(readerInfo.value.readerNo)
  } catch (e: any) {
    alert(e.message || 'Borrow failed')
  } finally {
    borrowLoading.value = false
  }
}

// ======== Return: Find ========
async function lookupReturn() {
  const bc = returnBarcodeInput.value.trim()
  if (!bc) return
  returnLoading.value = true
  returnInfo.value = null
  returnResult.value = null
  try {
    returnInfo.value = await findReturn(bc)
  } catch (e: any) {
    alert(e.message || 'No borrow record found')
  } finally {
    returnLoading.value = false
  }
}

// ======== Return: Confirm ========
async function confirmReturn() {
  if (!returnInfo.value) return
  returnLoading.value = true
  try {
    const result = await returnBooks([returnInfo.value.barcode])
    returnResult.value = result
    returnInfo.value = null
    returnBarcodeInput.value = ''
  } catch (e: any) {
    alert(e.message || 'Return failed')
  } finally {
    returnLoading.value = false
  }
}
</script>

<template>
  <div class="admin-borrow-return">
    <!-- Header -->
    <div class="abr-header">
      <h1 class="abr-title">Circulation</h1>
    </div>

    <div class="abr-content">
      <!-- ============ BORROW PANEL ============ -->
      <div class="abr-panel">
        <div class="abr-panel-header">Borrow</div>

        <!-- Step 1: Reader Lookup -->
        <div class="abr-section">
          <div class="abr-section-label">Step 1: Enter Reader Card</div>
          <div class="abr-row">
            <div class="abr-input-icon">
              <span class="abr-icon">🔍</span>
              <input
                v-model="readerNoInput"
                class="abr-input"
                placeholder="Scan or enter card no..."
                @keyup.enter="lookupReader"
              />
            </div>
            <button class="abr-btn abr-btn--primary" :disabled="readerLoading || !readerNoInput.trim()" @click="lookupReader">
              <span v-if="readerLoading" class="abr-spinner" />
              <span v-else>Lookup</span>
            </button>
          </div>

          <!-- Reader Info Card -->
          <div v-if="readerInfo" class="abr-reader-card">
            <div class="abr-reader-avatar">{{ (readerInfo.realName || '?').charAt(0).toUpperCase() }}</div>
            <div class="abr-reader-details">
              <div class="abr-reader-name">{{ readerInfo.realName }}</div>
              <div class="abr-reader-meta">
                {{ readerInfo.readerNo }} · {{ readerInfo.readerTypeName }} · {{ readerInfo.currentBorrowed }}/{{ readerInfo.maxBorrow }} borrowed
              </div>
              <div v-if="readerInfo.overdueCount > 0" class="abr-reader-warning">
                ⚠ {{ readerInfo.overdueCount }} overdue book(s) — cannot borrow
              </div>
            </div>
          </div>
        </div>

        <!-- Step 2: Scan Books -->
        <div class="abr-section" v-if="readerInfo && readerInfo.overdueCount === 0">
          <div class="abr-section-label">Step 2: Scan Book Barcodes</div>
          <div class="abr-row">
            <div class="abr-input-icon">
              <span class="abr-icon">🔍</span>
              <input
                v-model="barcodeInput"
                class="abr-input"
                placeholder="Scan barcode..."
                @keyup.enter="addBook"
              />
            </div>
            <button class="abr-btn abr-btn--primary" :disabled="!barcodeInput.trim()" @click="addBook">+</button>
          </div>

          <!-- Available slots -->
          <div v-if="readerInfo" class="abr-slots">
            <span :class="['abr-slot-count', { 'abr-slot-count--low': availableSlots <= 2 }]">
              {{ availableSlots }} slot(s) available
            </span>
          </div>

          <!-- Scanned Books List -->
          <div v-if="scannedBooks.length > 0" class="abr-book-list">
            <div class="abr-book-list-header">
              <span class="abr-book-col abr-col-barcode">Barcode</span>
              <span class="abr-book-col abr-col-title">Title</span>
              <span class="abr-book-col abr-col-status">Status</span>
              <span class="abr-book-col abr-col-action"></span>
            </div>
            <div
              v-for="book in scannedBooks"
              :key="book.barcode"
              class="abr-book-row"
            >
              <span class="abr-book-col abr-col-barcode abr-barcode">{{ book.barcode }}</span>
              <span class="abr-book-col abr-col-title">{{ book.title }}</span>
              <span class="abr-book-col abr-col-status"><span class="abr-badge abr-badge--ok">OK</span></span>
              <span class="abr-book-col abr-col-action">
                <button class="abr-remove-btn" @click="removeBook(book.barcode)">✕</button>
              </span>
            </div>
          </div>

          <!-- Confirm Button -->
          <button
            v-if="readerInfo && scannedBooks.length > 0"
            class="abr-confirm-btn"
            :disabled="borrowLoading"
            @click="confirmBorrow"
          >
            <span v-if="borrowLoading" class="abr-spinner" />
            <span v-else>Confirm Borrow ({{ scannedBooks.length }} book{{ scannedBooks.length > 1 ? 's' : '' }})</span>
          </button>

          <!-- Borrow Result -->
          <div v-if="borrowResult" class="abr-result abr-result--success">
            ✅ Successfully borrowed {{ borrowResult.successCount }} book(s)
            <div v-if="borrowResult.failCount > 0" class="abr-result--warning" style="margin-top:4px">
              ❌ {{ borrowResult.failCount }} failed
            </div>
            <div v-for="r in borrowResult.records" :key="r.barcode" class="abr-result-item">
              <template v-if="r.success">{{ r.title }} — Due: {{ r.dueDate }}</template>
              <template v-else>❌ {{ r.barcode }} — failed</template>
            </div>
          </div>
        </div>
      </div>

      <!-- ============ RETURN PANEL ============ -->
      <div class="abr-panel">
        <div class="abr-panel-header">Return</div>

        <!-- Scan Input -->
        <div class="abr-section">
          <div class="abr-row">
            <div class="abr-input-icon abr-input-icon--fill">
              <span class="abr-icon">🔍</span>
              <input
                v-model="returnBarcodeInput"
                class="abr-input"
                placeholder="Scan book barcode to return..."
                @keyup.enter="lookupReturn"
              />
            </div>
            <button class="abr-btn abr-btn--primary" :disabled="returnLoading || !returnBarcodeInput.trim()" @click="lookupReturn">
              <span v-if="returnLoading" class="abr-spinner" />
              <span v-else>Find</span>
            </button>
          </div>
        </div>

        <!-- Return Book Info -->
        <div v-if="returnInfo" class="abr-return-info">
          <div class="abr-return-info-card">
            <div class="abr-cover-thumb">📖</div>
            <div class="abr-return-details">
              <div class="abr-return-title">{{ returnInfo.title }}</div>
              <div class="abr-return-meta">{{ returnInfo.author }}</div>
              <div class="abr-return-meta">Borrowed: {{ returnInfo.borrowDate }} — Due: {{ returnInfo.dueDate }}</div>
              <div class="abr-return-meta abr-return-reader">Borrowed by: {{ returnInfo.readerName }} ({{ returnInfo.readerNo }})</div>
            </div>
          </div>

          <!-- Overdue Warning -->
          <div v-if="returnInfo.isOverdue" class="abr-overdue-warning">
            <div class="abr-overdue-icon">⚠️</div>
            <div class="abr-overdue-text">
              <div class="abr-overdue-title">Overdue by {{ returnInfo.overdueDays }} day(s)</div>
              <div class="abr-overdue-fine">Fine amount: ¥{{ returnInfo.fineAmount }}</div>
            </div>
          </div>

          <!-- Confirm Return -->
          <button
            class="abr-confirm-btn abr-confirm-btn--return"
            :disabled="returnLoading"
            @click="confirmReturn"
          >
            <span v-if="returnLoading" class="abr-spinner" />
            <span v-else>Confirm Return</span>
          </button>
        </div>

        <!-- Return Result -->
        <div v-if="returnResult && returnResult.records" class="abr-result" :class="returnResult.records[0]?.overdueDays > 0 ? 'abr-result--warning' : 'abr-result--success'">
          <template v-for="rec in returnResult.records" :key="rec.barcode">
            <template v-if="rec.overdueDays > 0">
              ⚠️ "{{ rec.title }}" returned with {{ rec.overdueDays }} day(s) overdue — ¥{{ rec.fineAmount }} fine pending
            </template>
            <template v-else>
              ✅ Successfully returned "{{ rec.title }}"
            </template>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-borrow-return {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 28px 32px;
  gap: 24px;
  background: var(--bg-secondary, #F7F8FA);
  min-width: 0;
}

/* ===== Header ===== */
.abr-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.abr-title {
  font-family: var(--font-sans, Inter);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary, #1A1A1A);
}
/* ===== Content ===== */
.abr-content {
  display: flex;
  gap: 20px;
  flex: 1;
}

/* ===== Panel ===== */
.abr-panel {
  flex: 1;
  min-width: 0;
  background: var(--bg-primary, #FFF);
  border-radius: var(--card-radius, 16px);
  border: 1px solid var(--border, #E5E7EB);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-self: flex-start;
}
.abr-panel-header {
  font-family: var(--font-sans, Inter);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
}

/* ===== Section ===== */
.abr-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.abr-section-label {
  font-family: var(--font-sans, Inter);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
}

/* ===== Input Row ===== */
.abr-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.abr-input-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-secondary, #F7F8FA);
  border: 1.5px solid var(--border, #E5E7EB);
  border-radius: var(--input-radius, 12px);
  padding: 10px 14px;
  width: 240px;
  transition: border-color 0.2s;
}
.abr-input-icon:focus-within {
  border-color: var(--accent, #4A9FD8);
  background: var(--bg-primary, #FFF);
}
.abr-input-icon--fill {
  flex: 1;
  width: auto;
}
.abr-icon {
  font-size: 14px;
  line-height: 1;
  flex-shrink: 0;
}
.abr-input {
  flex: 1;
  border: none;
  background: transparent;
  font-family: var(--font-sans, Inter);
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  outline: none;
}
.abr-input::placeholder {
  color: var(--text-muted, #888);
}

/* ===== Buttons ===== */
.abr-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-family: var(--font-sans, Inter);
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
  white-space: nowrap;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.abr-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.abr-btn--primary {
  background: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFF);
}
.abr-btn--primary:hover:not(:disabled) {
  opacity: 0.85;
}

/* ===== Reader Card ===== */
.abr-reader-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: var(--bg-secondary, #F7F8FA);
  border-radius: 10px;
  align-items: center;
}
.abr-reader-avatar {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans, Inter);
  font-size: 14px;
  font-weight: 600;
  color: var(--accent, #4A9FD8);
  flex-shrink: 0;
}
.abr-reader-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.abr-reader-name {
  font-family: var(--font-sans, Inter);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
}
.abr-reader-meta {
  font-family: var(--font-sans, Inter);
  font-size: 12px;
  color: var(--text-muted, #888);
}
.abr-reader-warning {
  font-family: var(--font-sans, Inter);
  font-size: 12px;
  color: var(--danger, #F87171);
  margin-top: 2px;
}

/* ===== Slots ===== */
.abr-slots {
  font-family: var(--font-sans, Inter);
  font-size: 12px;
  color: var(--text-muted, #888);
}
.abr-slot-count--low {
  color: var(--warning, #FBBF24);
  font-weight: 600;
}

/* ===== Book List ===== */
.abr-book-list {
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 10px;
  overflow: hidden;
}
.abr-book-list-header {
  display: flex;
  padding: 8px 14px;
  background: var(--bg-secondary, #F7F8FA);
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.abr-book-list-header .abr-book-col {
  font-family: var(--font-sans, Inter);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted, #888);
}
.abr-book-row {
  display: flex;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border, #E5E7EB);
  align-items: center;
}
.abr-book-row:last-child {
  border-bottom: none;
}
.abr-book-col {
  font-family: var(--font-sans, Inter);
  font-size: 12px;
  color: var(--text-primary, #1A1A1A);
}
.abr-col-barcode { width: 140px; flex-shrink: 0; }
.abr-col-title { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 0 8px; }
.abr-col-status { width: 80px; flex-shrink: 0; }
.abr-col-action { width: 30px; flex-shrink: 0; text-align: right; }
.abr-barcode {
  font-family: var(--font-mono, 'Geist Mono', monospace);
  font-size: 11px;
}

/* ===== Badge ===== */
.abr-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}
.abr-badge--ok {
  background: rgba(52, 211, 153, 0.12);
  color: var(--success, #34D399);
}

/* ===== Remove Button ===== */
.abr-remove-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  color: var(--danger, #F87171);
  padding: 2px;
  line-height: 1;
}
.abr-remove-btn:hover {
  opacity: 0.7;
}

/* ===== Confirm Button ===== */
.abr-confirm-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 28px;
  border-radius: 10px;
  background: var(--accent, #4A9FD8);
  color: var(--text-inverse, #FFF);
  font-family: var(--font-sans, Inter);
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
  line-height: 1;
}
.abr-confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.abr-confirm-btn:hover:not(:disabled) {
  opacity: 0.85;
}
.abr-confirm-btn--return {
  background: var(--success, #34D399);
}

/* ===== Result ===== */
.abr-result {
  padding: 12px 16px;
  border-radius: 10px;
  font-family: var(--font-sans, Inter);
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.abr-result--success {
  background: rgba(52, 211, 153, 0.08);
  color: var(--success, #34D399);
  border: 1px solid rgba(52, 211, 153, 0.3);
}
.abr-result--warning {
  background: rgba(248, 113, 113, 0.08);
  color: var(--danger, #F87171);
  border: 1px solid rgba(248, 113, 113, 0.3);
}
.abr-result-item {
  font-size: 12px;
  padding-left: 20px;
}

/* ===== Return Info ===== */
.abr-return-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.abr-return-info-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: var(--bg-secondary, #F7F8FA);
  border-radius: 10px;
}
.abr-cover-thumb {
  width: 40px;
  height: 56px;
  border-radius: 6px;
  background: var(--accent-light, #E8F4FD);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.abr-return-details {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.abr-return-title {
  font-family: var(--font-sans, Inter);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
}
.abr-return-meta {
  font-family: var(--font-sans, Inter);
  font-size: 12px;
  color: var(--text-muted, #888);
}
.abr-return-reader {
  color: var(--text-secondary, #666);
}

/* ===== Overdue Warning ===== */
.abr-overdue-warning {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(248, 113, 113, 0.08);
  border: 1px solid var(--danger, #F87171);
  border-radius: 10px;
  align-items: flex-start;
}
.abr-overdue-icon {
  font-size: 16px;
  line-height: 1.3;
  flex-shrink: 0;
}
.abr-overdue-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.abr-overdue-title {
  font-family: var(--font-sans, Inter);
  font-size: 13px;
  font-weight: 600;
  color: var(--danger, #F87171);
}
.abr-overdue-fine {
  font-family: var(--font-sans, Inter);
  font-size: 12px;
  color: var(--danger, #F87171);
}

/* ===== Spinner ===== */
.abr-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: abr-spin 0.6s linear infinite;
}
@keyframes abr-spin {
  to { transform: rotate(360deg); }
}
</style>

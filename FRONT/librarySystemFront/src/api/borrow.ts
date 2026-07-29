import http from './index'

export async function getCurrentBorrowing(readerNo: string): Promise<any[]> {
  return http.get('/borrow/current', { params: { readerNo } }) as any
}

export async function getBorrowSummary(readerNo: string): Promise<any> {
  return http.get('/borrow/summary', { params: { readerNo } }) as any
}

export async function getBorrowHistory(readerNo: string, startDate?: string, endDate?: string): Promise<any[]> {
  return http.get('/borrow/history', { params: { readerNo, startDate, endDate } }) as any
}

// ==================== Admin Borrow/Return ====================

export async function checkReader(readerNo: string): Promise<any> {
  return http.post('/borrow/check-reader', { readerNo }) as any
}

export async function checkBarcode(barcode: string): Promise<any> {
  return http.post('/borrow/check-barcode', { barcode }) as any
}

export async function borrowBooks(readerNo: string, barcodes: string[]): Promise<any> {
  return http.post('/borrow', { readerNo, barcodes, remark: '' }) as any
}

export async function findReturn(barcode: string): Promise<any> {
  return http.post('/borrow/find-return', { barcode }) as any
}

export async function returnBooks(barcodes: string[]): Promise<any> {
  return http.put('/borrow/return', { barcodes, damageInfo: null }) as any
}

// ==================== Reservations ====================

export async function getCurrentReservations(readerNo: string): Promise<any[]> {
  return http.get('/reservations/current', { params: { readerNo } }) as any
}

// ==================== Renew ====================

export async function renewBook(recordId: number, readerNo: string): Promise<{ oldDueDate: string; newDueDate: string; renewCount: number }> {
  return http.post(`/borrow/${recordId}/renew`, { readerNo }) as any
}

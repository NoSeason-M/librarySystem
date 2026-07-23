import http from './index'

export async function getCurrentBorrowing(readerNo: string): Promise<any[]> {
  return http.get('/borrow/current', { params: { readerNo } }) as any
}

export async function getBorrowSummary(readerNo: string): Promise<any> {
  return http.get('/borrow/summary', { params: { readerNo } }) as any
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

import http from './index'

export async function getCurrentBorrowing(readerNo: string): Promise<any[]> {
  return http.get('/borrow/current', { params: { readerNo } }) as any
}

export async function getBorrowSummary(readerNo: string): Promise<any> {
  return http.get('/borrow/summary', { params: { readerNo } }) as any
}

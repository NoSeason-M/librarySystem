import http from './index'

export interface FineItem {
  id: number
  readerNo?: string
  readerName?: string
  bookTitle?: string
  fineType: string
  fineTypeCode?: string
  amount: number
  overdueDays?: number
  paid: boolean
  waive?: boolean
  createTime: string
  paidDate?: string
}

export async function listAdminFines(params: {
  keyword?: string
  fineType?: string
  paid?: number
  page?: number
  size?: number
} = {}): Promise<{ records: FineItem[]; total: number; pages: number }> {
  return http.get('/fines', { params }) as any
}

export async function getFinesSummary(): Promise<{
  unpaidCount: number
  unpaidAmount: number
  thisMonthCount: number
  paidThisMonth: number
}> {
  return http.get('/fines/summary') as any
}

export async function payFine(id: number, remark?: string): Promise<void> {
  return http.post(`/fines/${id}/pay`, { remark }) as any
}

export async function waiveFine(id: number, waiveReason?: string): Promise<void> {
  return http.put(`/fines/${id}/waive`, { waiveReason }) as any
}

export async function batchPayFines(ids: number[]): Promise<void> {
  return http.post('/fines/batch-pay', { ids }) as any
}

export async function batchWaiveFines(ids: number[], waiveReason?: string): Promise<void> {
  return http.put('/fines/batch-waive', { ids, waiveReason }) as any
}

// ==================== Reader self-service ====================

export async function getReaderFines(readerNo: string, paid?: number): Promise<any[]> {
  const result: any = await http.get('/fines', { params: { readerNo, paid, page: 1, size: 50 } })
  return result.records || []
}

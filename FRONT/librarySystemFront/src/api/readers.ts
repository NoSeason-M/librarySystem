import http from './index'

export interface ReaderItem {
  id: number
  userId: number
  readerNo: string
  realName: string
  readerTypeName: string
  readerTypeId: number
  cardStatus: number
  cardStatusLabel: string
  currentBorrowed: number
  maxBorrow: number
  totalBorrowed: number
  email: string
  phone: string
  registerDate: string
  expireDate: string
}

export interface ReaderType {
  id: number
  name: string
  code: string
  maxBorrow: number
  borrowDays: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export async function listReaders(
  keyword?: string,
  readerTypeId?: number,
  cardStatus?: number,
  page = 1,
  size = 8
): Promise<PageResult<ReaderItem>> {
  return http.get('/readers', { params: { keyword, readerTypeId, cardStatus, page, size } }) as any
}

export async function getReaderTypes(): Promise<ReaderType[]> {
  return http.get('/readers/types') as any
}

export async function createReader(data: any): Promise<any> {
  return http.post('/readers', data) as any
}

export async function updateReader(id: number, data: any): Promise<void> {
  return http.put(`/readers/${id}`, data) as any
}

export async function cardAction(id: number, action: string): Promise<void> {
  return http.put(`/readers/${id}/card`, { action }) as any
}

export async function freezeAction(id: number, action: string): Promise<void> {
  return http.put(`/readers/${id}/freeze`, { action }) as any
}

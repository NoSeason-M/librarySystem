import http from './index'

export interface BookItem {
  id: number; isbn: string; title: string; subTitle?: string | null
  author: string; translator?: string | null; coverUrl: string | null
  publishDate: string | null; price: number | null; pages?: number | null
  binding?: string | null; language?: string | null; totalCopies: number
  availableCopies: number; borrowCount: number; rating: number | null
  ratingCount?: number | null; summary: string | null
  categoryName: string | null; categoryId?: number | null
  publisherName?: string | null; status?: number; createTime?: string | null
}

export interface BookCopyItem {
  id: number; barcode: string; locationName?: string | null
  status: string; statusLabel: string; price?: number | null
  purchaseDate?: string | null; dueDate?: string | null
}

export interface PageResult<T> {
  records: T[]; total: number; size: number; current: number; pages: number
}

export async function searchBooks(keyword?: string, page = 1, size = 10) {
  return http.get('/books', { params: { keyword, page, size } }) as any
}
export async function getHotBooks(limit = 10) {
  return http.get('/books/hot', { params: { limit } }) as any
}
export async function getNewArrivals(days = 30, limit = 10) {
  return http.get('/books/new-arrivals', { params: { days, limit } }) as any
}
export async function getCategoryTree() {
  return http.get('/categories/tree') as any
}
export async function getBookDetail(id: number) {
  return http.get(`/books/${id}`) as any
}
export async function getBookCopies(id: number) {
  return http.get(`/books/${id}/copies`) as any
}

export async function listAdminBooks(keyword?: string, page = 1, size = 10) {
  return http.get('/books/admin/list', { params: { keyword, page, size } }) as any
}
export async function createBook(data: any) {
  return http.post('/books', data) as any
}
export async function updateBook(id: number, data: any) {
  return http.put(`/books/${id}`, data) as any
}
export async function deleteBook(id: number) {
  return http.delete(`/books/${id}`) as any
}
export async function uploadCover(id: number, file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post(`/books/${id}/cover`, fd, { headers: { 'Content-Type': 'multipart/form-data' } }) as any
}
export async function importBooks(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post('/books/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } }) as any
}
export async function exportBooks(keyword?: string) {
  const token = localStorage.getItem('accessToken')
  const p = new URLSearchParams()
  if (keyword) p.set('keyword', keyword)
  const r = await fetch('/api/books/export?' + p.toString(), { headers: { Authorization: 'Bearer ' + token } })
  const blob = await r.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = 'books_export.xlsx'; a.click()
  URL.revokeObjectURL(url)
}

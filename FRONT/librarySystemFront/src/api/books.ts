import http from './index'

export interface BookItem {
  id: number
  isbn: string
  title: string
  subTitle?: string | null
  author: string
  translator?: string | null
  coverUrl: string | null
  publishDate: string | null
  price: number | null
  pages?: number | null
  binding?: string | null
  language?: string | null
  totalCopies: number
  availableCopies: number
  borrowCount: number
  rating: number | null
  ratingCount?: number | null
  summary: string | null
  categoryName: string | null
  categoryId?: number | null
  publisherName?: string | null
  status?: number
  createTime?: string | null
}

export interface BookCopyItem {
  id: number
  barcode: string
  locationName?: string | null
  status: string
  statusLabel: string
  price?: number | null
  purchaseDate?: string | null
  dueDate?: string | null
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// ==================== Public ====================

export async function searchBooks(keyword?: string, page = 1, size = 10): Promise<PageResult<BookItem>> {
  return http.get('/books', { params: { keyword, page, size } }) as any
}

export async function getHotBooks(limit = 10): Promise<BookItem[]> {
  return http.get('/books/hot', { params: { limit } }) as any
}

export async function getNewArrivals(days = 30, limit = 10): Promise<BookItem[]> {
  return http.get('/books/new-arrivals', { params: { days, limit } }) as any
}

export async function getCategoryTree(): Promise<any[]> {
  return http.get('/categories/tree') as any
}

export async function getBookDetail(id: number): Promise<BookItem> {
  return http.get(`/books/${id}`) as any
}

export async function getBookCopies(id: number): Promise<BookCopyItem[]> {
  return http.get(`/books/${id}/copies`) as any
}

// ==================== Admin CRUD ====================

export async function listAdminBooks(keyword?: string, page = 1, size = 10): Promise<PageResult<BookItem>> {
  return http.get('/books/admin/list', { params: { keyword, page, size } }) as any
}

export async function createBook(data: Partial<BookItem>): Promise<{ bookId: number }> {
  return http.post('/books', data) as any
}

export async function updateBook(id: number, data: Partial<BookItem>): Promise<void> {
  return http.put(`/books/${id}`, data) as any
}

export async function deleteBook(id: number): Promise<void> {
  return http.delete(`/books/${id}`) as any
}

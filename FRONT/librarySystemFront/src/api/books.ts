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

export async function getBookDetail(id: number): Promise<BookItem> {
  return http.get(`/books/${id}`) as any
}

export async function getBookCopies(id: number): Promise<BookCopyItem[]> {
  return http.get(`/books/${id}/copies`) as any
}

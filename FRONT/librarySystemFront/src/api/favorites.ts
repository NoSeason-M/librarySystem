import http from './index'

export async function getFavorites(readerNo: string): Promise<{ records: any[]; total: number }> {
  return http.get('/favorites', { params: { readerNo, page: 1, size: 50 } }) as any
}

export async function checkFavorite(bookInfoId: number, readerNo: string): Promise<{ favorited: boolean }> {
  return http.get('/favorites/check', { params: { bookInfoId, readerNo } }) as any
}

export async function addFavorite(bookInfoId: number, readerNo: string): Promise<void> {
  return http.post('/favorites', { bookInfoId, readerNo }) as any
}

export async function removeFavorite(bookInfoId: number, readerNo: string): Promise<void> {
  return http.delete('/favorites', { params: { bookInfoId, readerNo } }) as any
}

import http from './index'

export async function getFines(readerNo: string, paid?: number): Promise<any[]> {
  return http.get('/fines', { params: { readerNo, paid } }) as any
}

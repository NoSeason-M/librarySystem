import http from './index'

export async function getCirculationStats(): Promise<any> {
  return http.get('/statistics/circulation') as any
}

export async function getCollectionStats(): Promise<any> {
  return http.get('/statistics/collection') as any
}

export async function getReaderStats(): Promise<any> {
  return http.get('/statistics/readers') as any
}

export async function getRecentActivity(): Promise<any[]> {
  return http.get('/statistics/recent-activity') as any
}

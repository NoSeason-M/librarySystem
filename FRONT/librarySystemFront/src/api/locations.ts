import http from './index'

export interface LocationItem {
  id: number
  name: string
  code: string
  description?: string
  floor?: string
  enabled: boolean
  sort: number
}

export async function listLocations(): Promise<LocationItem[]> {
  return http.get('/locations') as any
}

export async function createLocation(data: Partial<LocationItem>): Promise<void> {
  return http.post('/locations', data) as any
}

export async function updateLocation(id: number, data: Partial<LocationItem>): Promise<void> {
  return http.put(`/locations/${id}`, data) as any
}

export async function deleteLocation(id: number): Promise<void> {
  return http.delete(`/locations/${id}`) as any
}

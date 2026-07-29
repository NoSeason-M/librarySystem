import http from './index'

export interface NotifItem {
  id: number
  title: string
  content: string
  type: string
  typeLabel: string
  readFlag: boolean
  createTime: string
}

export async function getNotifications(page = 1, size = 10, readFlag?: number): Promise<{
  records: NotifItem[]; total: number; unreadCount: number; pages: number
}> {
  return http.get('/notifications', { params: { page, size, readFlag } }) as any
}

export async function getUnreadCount(): Promise<number> {
  const r: any = await http.get('/notifications/unread-count')
  return r.unreadCount || 0
}

export async function markNotifRead(id: number): Promise<void> {
  return http.put(`/notifications/${id}/read`) as any
}

export async function markAllNotifRead(): Promise<void> {
  return http.put('/notifications/read-all') as any
}

export async function deleteNotif(id: number): Promise<void> {
  return http.delete(`/notifications/${id}`) as any
}

export async function batchDeleteNotifs(ids: number[]): Promise<void> {
  return http.delete('/notifications', { data: { ids } }) as any
}

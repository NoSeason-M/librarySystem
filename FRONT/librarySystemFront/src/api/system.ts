import http from './index'

export async function listUsers(keyword?: string, page = 1, size = 10) {
  return http.get('/system/users', { params: { keyword, page, size } }) as any
}
export async function createUser(data: any) { return http.post('/system/users', data) as any }
export async function updateUser(id: number, data: any) { return http.put(`/system/users/${id}`, data) as any }
export async function resetPassword(id: number) { return http.put(`/system/users/${id}/reset-pwd`) as any }

export async function listRoles() { return http.get('/system/roles') as any }
export async function createRole(data: any) { return http.post('/system/roles', data) as any }
export async function updateRole(id: number, data: any) { return http.put(`/system/roles/${id}`, data) as any }
export async function deleteRole(id: number) { return http.delete(`/system/roles/${id}`) as any }

export async function listMenus() { return http.get('/system/menus') as any }
export async function createMenu(data: any) { return http.post('/system/menus', data) as any }
export async function updateMenu(id: number, data: any) { return http.put(`/system/menus/${id}`, data) as any }
export async function deleteMenu(id: number) { return http.delete(`/system/menus/${id}`) as any }

export async function listConfigs(keyword?: string, page = 1, size = 20) {
  return http.get('/system/config', { params: { keyword, page, size } }) as any
}
export async function updateConfig(id: number, data: any) { return http.put(`/system/config/${id}`, data) as any }
export async function listPublicConfigs() { return http.get('/system/config/public') as any }

export async function listDicts() { return http.get('/system/dicts') as any }
export async function getDictWithItems(code: string) { return http.get(`/system/dicts/${code}`) as any }
export async function createDictItem(data: any) { return http.post('/system/dicts/items', data) as any }
export async function updateDictItem(id: number, data: any) { return http.put(`/system/dicts/items/${id}`, data) as any }
export async function deleteDictItem(id: number) { return http.delete(`/system/dicts/items/${id}`) as any }

export async function listLogs(keyword?: string, module?: string, page = 1, size = 15) {
  return http.get('/system/logs', { params: { keyword, module, page, size } }) as any
}

export async function listAnnouncements(page = 1, size = 10) {
  return http.get('/system/announcements', { params: { page, size } }) as any
}
export async function createAnnouncement(data: any) { return http.post('/system/announcements', data) as any }
export async function updateAnnouncement(id: number, data: any) { return http.put(`/system/announcements/${id}`, data) as any }
export async function deleteAnnouncement(id: number) { return http.delete(`/system/announcements/${id}`) as any }

export async function listBackups() { return http.get('/system/backup') as any }
export async function createBackup() { return http.post('/system/backup') as any }

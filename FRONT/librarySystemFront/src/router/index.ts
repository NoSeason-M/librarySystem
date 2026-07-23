import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
    { path: '/books/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue'), meta: { requiresAuth: true } },
    { path: '/home', name: 'ReaderHome', component: () => import('../views/ReaderHome.vue'), meta: { requiresAuth: true } },
    { path: '/reader', name: 'ReaderDashboard', component: () => import('../views/ReaderDashboard.vue'), meta: { requiresAuth: true } },
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'AdminDashboard', component: () => import('../views/AdminDashboard.vue') },
        { path: 'books', name: 'AdminBooks', component: () => import('../views/AdminBooks.vue') },
        { path: 'borrow-return', name: 'AdminBorrowReturn', component: () => import('../views/AdminBorrowReturn.vue') },
        { path: 'readers', name: 'AdminReaders', component: () => import('../views/AdminReaders.vue') },
        { path: 'statistics', name: 'AdminStatistics', component: () => import('../views/AdminStatistics.vue') },
        { path: 'settings', name: 'AdminSettings', component: () => import('../views/AdminSettings.vue') },
        { path: 'settings/users', name: 'AdminSettingsUsers', component: () => import('../views/AdminSettingsUsers.vue') },
        { path: 'settings/roles', name: 'AdminSettingsRoles', component: () => import('../views/AdminSettingsRoles.vue') },
        { path: 'settings/menus', name: 'AdminSettingsMenus', component: () => import('../views/AdminSettingsMenus.vue') },
        { path: 'settings/config', name: 'AdminSettingsConfig', component: () => import('../views/AdminSettingsConfig.vue') },
        { path: 'settings/dicts', name: 'AdminSettingsDicts', component: () => import('../views/AdminSettingsDicts.vue') },
        { path: 'settings/logs', name: 'AdminSettingsLogs', component: () => import('../views/AdminSettingsLogs.vue') },
        { path: 'settings/announcements', name: 'AdminSettingsAnnouncements', component: () => import('../views/AdminSettingsAnnouncements.vue') },
        { path: 'settings/backup', name: 'AdminSettingsBackup', component: () => import('../views/AdminSettingsBackup.vue') },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.meta.requiresAuth && !token) next('/login')
  else next()
})

export default router

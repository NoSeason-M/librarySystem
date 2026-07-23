import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
    },
    {
      path: '/books/:id',
      name: 'BookDetail',
      component: () => import('../views/BookDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/home',
      name: 'ReaderHome',
      component: () => import('../views/ReaderHome.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/reader',
      name: 'ReaderDashboard',
      component: () => import('../views/ReaderDashboard.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: () => import('../views/AdminDashboard.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/books',
      name: 'AdminBooks',
      component: () => import('../views/AdminBooks.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/readers',
      name: 'AdminReaders',
      component: () => import('../views/AdminReaders.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/statistics',
      name: 'AdminStatistics',
      component: () => import('../views/AdminStatistics.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

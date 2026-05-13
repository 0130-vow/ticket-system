import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/tickets',
      name: 'TicketList',
      component: () => import('../views/TicketList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/tickets/create',
      name: 'CreateTicket',
      component: () => import('../views/CreateTicket.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/tickets/:id',
      name: 'TicketDetail',
      component: () => import('../views/TicketDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/kanban',
      name: 'Kanban',
      component: () => import('../views/Kanban.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stats',
      name: 'Statistics',
      component: () => import('../views/Statistics.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('../components/ErrorPage.vue'),
      props: { type: '403' }
    },
    {
      path: '/500',
      name: 'ServerError',
      component: () => import('../components/ErrorPage.vue'),
      props: { type: '500', showRetry: true }
    },
    {
      path: '/network-error',
      name: 'NetworkError',
      component: () => import('../components/ErrorPage.vue'),
      props: { type: 'network', showRetry: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../components/ErrorPage.vue'),
      props: { type: '404' }
    }
  ]
})

export default router

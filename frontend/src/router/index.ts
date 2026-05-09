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
    }
  ]
})

export default router

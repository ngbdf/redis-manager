import Vue from 'vue'
import Router from 'vue-router'
import Index from '@/components/Index'
import Dashboard from '@/components/dashboard/Dashboard'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Index',
      component: Index,
      children: [
        {
          name: 'dashboard',
          path: '/dashboard',
          component: Dashboard
        }
      ]
    }
  ]
})

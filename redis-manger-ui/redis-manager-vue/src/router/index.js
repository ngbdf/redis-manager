import Vue from 'vue'
import Router from 'vue-router'
import Index from '@/components/Index'
import Dashboard from '@/components/dashboard/Dashboard'
import Monitor from '@/components/monitor/Monitor'
import Installation from '@/components/install/Installation'

Vue.use(Router)

// const originalPush = Router.prototype.push
// Router.prototype.push = function push(location) {
//   return originalPush.call(this, location).catch(err => err)
// }

export default new Router({
  routes: [
    {
      path: '/',
      name: 'index',
      component: Index,
      children: [
        {
          name: 'dashboard',
          path: '/dashboard',
          component: Dashboard,
          meta: {
            title: 'Dashboard',
            icon: 'el-icon-discover',
            noCache: false,
            affix: true
          }
        },
        {
          name: 'monitor',
          path: '/monitor',
          component: Monitor,
          meta: {
            title: 'Monitor',
            icon: 'el-icon-view',
            noCache: false,
            affix: true
          }
        },
        {
          name: 'installation',
          path: '/installation',
          component: Installation,
          meta: {
            title: 'Installation',
            icon: 'el-icon-view',
            noCache: true,
            affix: true
          }
        }
      ]
    }
  ]
})

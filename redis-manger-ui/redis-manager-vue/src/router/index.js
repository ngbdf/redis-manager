import Vue from 'vue'
import Router from 'vue-router'
import Index from '@/components/Index'
import ClusterCards from '@/components/dashboard/ClusterCards'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Index',
      component: Index,
      children: [
        {
          name: 'cluster-cards',
          path: '/cluster-cards',
          component: ClusterCards
      },
      ]
    }
  ]
})

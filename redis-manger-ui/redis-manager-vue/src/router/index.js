import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/components/Login'
import Index from '@/components/Index'
import Dashboard from '@/components/dashboard/Dashboard'
import RedisMonitor from '@/components/monitor/RedisMonitor'
import NodeManage from '@/components/manage/NodeManage'
import Installation from '@/components/install/Installation'
import ChannelManage from '@/components/alert/ChannelManage'
import RuleManage from '@/components/alert/RuleManage'
import AlertManage from '@/components/alert/AlertManage'
import MachineManage from '@/components/machine/MachineManage'
import GroupManage from '@/components/group/GroupManage'
import UserManage from '@/components/user/UserManage'
import Profile from '@/components/user/Profile'
import DataOperation from '@/components/tool/DataOperation'

import { store } from '@/vuex/store.js'
import { isEmpty } from '@/utils/validate.js'

Vue.use(Router)

// const originalPush = Router.prototype.push
// Router.prototype.push = function push(location) {
//   return originalPush.call(this, location).catch(err => err)
// }

const router = new Router({
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      hidden: true
    },
    {
      path: '/',
      name: 'index',
      component: Index,
      children: [
        {
          name: 'dashboard',
          path: '/dashboard/group/:groupId',
          component: Dashboard
        },
        {
          name: 'redis-monitor',
          path: '/redis-monitor/cluster/:clusterId',
          component: RedisMonitor
        },
        {
          name: 'redis-manage',
          path: '/redis-manage/cluster/:clusterId',
          component: NodeManage
        },
        {
          name: 'installation',
          path: '/installation/group/:groupId',
          component: Installation
        },
        {
          name: 'channel-manage',
          path: '/channel-manage/group/:groupId',
          component: ChannelManage
        }, {
          name: 'rule-manage',
          path: '/rule-manage/group/:groupId',
          component: RuleManage
        }, {
          name: 'alert-manage',
          path: '/alert-manage/group/:clusterId',
          component: AlertManage
        }, {
          name: 'machine-manage',
          path: '/machine-manage/group/:groupId',
          component: MachineManage
        }, {
          name: 'group-manage',
          path: '/group-manage',
          component: GroupManage
        },
        {
          name: 'user-manage',
          path: '/user-manage/:groupId',
          component: UserManage
        },
        {
          name: 'profile',
          path: '/profile',
          component: Profile
        },
        {
          name: 'data-operation',
          path: '/data-operation',
          component: DataOperation
        }
      ]
    }
    // {
    //   path: '*',
    //   hidden: true,
    //   redirect: { path: '/login' }
    // }
  ]
})

export default router

router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    store.dispatch('setUser', null)
  }
  let user = store.getters.getUser
  if (to.path !== '/login' && (isEmpty(user) || isEmpty(user.userId) || isEmpty(user.userName) || isEmpty(user.userRole))) {
    next({ path: '/login' })
  } else {
    next()
  }
})

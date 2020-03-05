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
import NotFound from '@/components/error/404'
import EditHistory from '@/components/history/EditHistory'
import Config from '@/components/rct/Config'
import TaskProgress from '@/components/rct/TaskProgress'
import JobList from '@/components/rct/JobList'
import JobResultDetail from '@/components/rct/JobResultDetail'

import API from '@/api/api.js'
import { store } from '@/vuex/store.js'
import { isEmpty } from '@/utils/validate.js'

Vue.use(Router)

// const originalPush = Router.prototype.push
// Router.prototype.push = function push (location) {
//   return originalPush.call(this, location).catch(err => err)
// }

const router = new Router({
  mode: 'history',
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
          path: '/alert-manage/cluster/:clusterId',
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
          path: '/user-manage/group/:groupId',
          component: UserManage
        },
        {
          name: 'edit-history',
          path: '/history/group/:groupId',
          component: EditHistory
        },
        {
          name: 'profile',
          path: '/profile/:userId',
          component: Profile
        },
        {
          name: 'data-operation',
          path: '/data-operation/group/:groupId',
          component: DataOperation
        }, {
          name: 'Config',
          path: '/rct/config',
          component: Config

        }, {
          name: 'TaskProgress',
          path: '/rct/taskProgress/:clusterId',
          component: TaskProgress

        }, {
          name: 'jobList',
          path: '/rct/jobList',
          component: JobList
        }, {
          name: 'jobResultDetail',
          path: '/rct/jobResultDetail',
          component: JobResultDetail
        }
      ]
    },
    {
      path: '/404',
      component: NotFound,
      name: '404',
      hidden: true
    },
    {
      path: '*',
      hidden: true,
      redirect: { path: '/404' }
    }
  ]
})

export default router

router.beforeEach((to, from, next) => {
  let user = store.getters.getUser
  let toPath = to.path
  if (toPath !== '/login' && (isEmpty(user) || isEmpty(user.userId) || isEmpty(user.userName) || isEmpty(user.userRole))) {
    let code = to.query.code
    if (!isEmpty(code)) {
      let url = '/user/oauth2Login?code=' + code
      API.get(
        url,
        null,
        response => {
          let result = response.data
          if (result.code === 0) {
            store.dispatch('setUser', result.data)
            next()
          } else {
            next({ path: 'login' })
          }
        },
        () => {
          next({ path: 'login' })
        }
      )
    } else {
      next({ path: 'login' })
    }

    // admin
  } else if (user.userRole === 1 && toPath.indexOf('group-manage') > 0) {
    next({ name: 'dashboard', params: { groupId: store.getters.getCurrentGroup.groupId } })
  } else if (user.userRole === 2 && (toPath.indexOf('group-manage') > 0 || toPath.indexOf('user-manage') > 0 ||
    toPath.indexOf('machine-manage') > 0 || toPath.indexOf('installation') > 0 ||
    toPath.indexOf('channel-manage') > 0 || toPath.indexOf('rule-manage') > 0 ||
    toPath.indexOf('redis-manage') > 0 || toPath.indexOf('alert-manage') > 0 ||
    toPath.indexOf('edit-history') > 0)) {
    next({ name: 'dashboard', params: { groupId: store.getters.getCurrentGroup.groupId } })
  } else {
    next()
  }
})

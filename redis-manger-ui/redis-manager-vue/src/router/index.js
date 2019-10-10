import Vue from 'vue'
import Router from 'vue-router'
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
          path: '/dashboard/:groupId',
          component: Dashboard,
          props: true,
          meta: {
            title: 'Dashboard',
            icon: 'el-icon-discover',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'redis-monitor',
          path: '/redis-monitor/:clusterId',
          component: RedisMonitor,
          meta: {
            title: 'Redis Monitor',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'redis-manage',
          path: '/redis-manage/:clusterId',
          component: NodeManage,
          meta: {
            title: 'Redis Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'installation',
          path: '/installation/:groupId',
          component: Installation,
          meta: {
            title: 'Installation',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'channel-manage',
          path: '/channel-manage/:groupId',
          component: ChannelManage,
          meta: {
            title: 'Channel Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        }, {
          name: 'rule-manage',
          path: '/rule-manage/:groupId',
          component: RuleManage,
          meta: {
            title: 'Rule Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        }, {
          name: 'alert-manage',
          path: '/alert-manage/:clusterId',
          component: AlertManage,
          meta: {
            title: 'Alert Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        }, {
          name: 'machine-manage',
          path: '/machine-manage/:groupId',
          component: MachineManage,
          meta: {
            title: 'Machine Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        }, {
          name: 'group-manage',
          path: '/group-manage',
          component: GroupManage,
          meta: {
            title: 'Group Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'user-manage',
          path: '/user-manage/:groupId',
          component: UserManage,
          meta: {
            title: 'User Manage',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'profile',
          path: '/profile',
          component: Profile,
          meta: {
            title: 'User Profile',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        },
        {
          name: 'data-operation',
          path: '/data-operation',
          component: DataOperation,
          meta: {
            title: 'Data Operation',
            icon: 'el-icon-view',
            noCache: true,
            affix: false
          }
        }
      ]
    }
  ]
})

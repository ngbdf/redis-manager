import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import API from "@/api/api.js";

Vue.use(Vuex)

// 应用初始状态
const state = {
  currentGroup: {},
  groupList: [],
  user: {
    avatar: '/user/image/jay.png',
    userRole: 2
  }
}

// 定义所需的 mutations，状态变更操作
const mutations = {
  setCurrentGroup(state, currentGroup) {
    getUserRole(currentGroup.groupId, state.user.userId)
    state.currentGroup = currentGroup
  },
  setGroupList(state, groupList) {
    state.groupList = groupList
  },
  setUser(state, user) {
    state.user = user
  },
  setUserRole(state, userRole) {
    state.user.userRole = userRole
  }

}

// 获取数据操作
const getters = {
  getCurrentGroup: state => {
    return state.currentGroup
  },
  getGroupList: state => {
    return state.groupList
  },
  getUser: state => {
    return state.user
  },
  getUserId: state => {
    return state.user.userId
  },
  getUserRole: state => {
    return state.user.userRole
  }
}

// 分发 Action
const actions = {
  setCurrentGroup(context, currentGroup) {
    context.commit('setCurrentGroup', currentGroup)
  },
  setGroupList(context, groupList) {
    context.commit('setGroupList', groupList)
  },
  setUser(context, user) {
    context.commit('setUser', user)
  },
  setUserRole(context, userRole) {
    context.commit('setUserRole', userRole)
  }
}

// 创建 store 实例
export const store = new Vuex.Store({
  state,
  getters,
  actions,
  mutations,
  plugins: [createPersistedState(
    {
      reducer(val) {
        return {
          user: val.user,
          currentGroup: val.currentGroup,
          groupList: val.groupList
        }
      }
    }
  )]
})

function getUserRole(groupId, userId) {
  let url = '/user/getUserRole/'
  let user = {
    groupId: groupId,
    userId: userId
  }
  API.post(url, user, response => {
    let userRole = response.data.data
    state.user.userRole = userRole
  }, err => {
    console.log(err)
    state.user.userRole = 2
  })
}

import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import API from "@/api/api.js";
import { isEmpty } from '@/utils/validate.js'
import apiConfig from '@/api/apiConfig.js'

Vue.use(Vuex)

// 应用初始状态
const state = {
  currentGroup: {},
  groupList: [],
  user: {
    userRole: 2
  },
  humpbackEnabled: false
}

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
  },
  setHumpbackEnabled(state, humpbackEnabled) {
    state.humpbackEnabled = humpbackEnabled
  }

}

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
  },
  getHumpbackEnabled: state => {
    return state.humpbackEnabled
  }
}

const actions = {
  setCurrentGroup (context, currentGroup) {
    context.commit('setCurrentGroup', currentGroup)
  },
  setGroupList (context, groupList) {
    context.commit('setGroupList', groupList)
  },
  setUser (context, user) {
    let avatar = user.avatar
    if (!isEmpty(apiConfig.baseUrl) && !isEmpty(avatar) && !avatar.startsWith(apiConfig.baseUrl)) {
      user.avatar = apiConfig.baseUrl + avatar
    }
    context.commit('setUser', user)
  },
  setUserRole (context, userRole) {
    context.commit('setUserRole', userRole)
  },
  setHumpbackEnabled (context, humpbackEnabled) {
    context.commit('setHumpbackEnabled', humpbackEnabled)
  }

}

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
  if (isEmpty(groupId) || isEmpty(userId)) {
    return
  }
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

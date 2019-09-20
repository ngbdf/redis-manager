import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'

Vue.use(Vuex)

// 应用初始状态
const state = {
    group: {
        groupId: 2,
        groupName: "Test"
    },
    user: {
        userId: 1,
        groupId: 2,
        userName: "Jay",
        headPic: "/user/image/jay.png"
    },
    userRole: 'ADMIN'
}

// 定义所需的 mutations，状态变更操作
const mutations = {
    setGroup(state, group) {
        console.log(group)
        state.group = group
    },
    setUser(state, user) {
        state.user = user
    },
    setUserRole(state, userRole) {
        state.userRole = userRole
    }
}

// 获取数据操作
const getters = {
    getGroup: state => {
        return state.group
    },
    getGroupId: state => {
        return state.group.groupId
    },
    getUser: state => {
        return state.user
    },
    getUserId: state => {
        return state.user.userId
    },
    getUserRole: state => {
        return state.userRole
    }
}

// 分发 Action
const actions = {
    setGroup(context, group) {
        context.commit('setGroup', group)
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
                    // siteRateMap
                    // siteRateMap: val.groupId
                }
            }
        }
    )]
})

import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'

Vue.use(Vuex)

// 应用初始状态
const state = {
    groupId: '',
    user: {
        userName: "Jay",
        userRole: "ADMIN",
        headPic: "/user/image/jay.png"
    }
}

// 定义所需的 mutations，状态变更操作
const mutations = {
    setGroupId(state, groupId) {
        state.groupId = groupId
    },
    setUser(state, user) {
        state.user = user
    }
}

// 获取数据操作
const getters = {
    getGroupId: state => {
        return state.groupId
    },
    getUser: state => {
        return state.user
    }
}

// 分发 Action
const actions = {
    setGroupId(context, groupId) {
        context.commit('setGroupId', groupId)
    },
    setUser(context, user) {
        context.commit('setUser', user)
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

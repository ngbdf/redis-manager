import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'

Vue.use(Vuex)

// 应用初始状态
const state = {
    currentGroup: {},
    groupList: [],
    user: {
        userId: 0,
        groupId: 10,
        userName: "Jay",
        headPic: "/user/image/jay.png"
    },
    userRole: 'ADMIN'
}

// 定义所需的 mutations，状态变更操作
const mutations = {
    setCurrentGroup(state, currentGroup) {
        state.currentGroup = currentGroup
    },
    setCurrentGroupById(state, groupId) {
        state.groupList.forEach(group => {
            if (groupId == group.groupId) {
                state.currentGroup = group
            }
        });
    },
    setGroupList(state, groupList) {
        state.groupList = groupList
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
    getCurrentGroup: state => {
        return state.currentGroup
    },
    getCurrentGroupId: state => {
        return state.currentGroup.groupId
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
        return state.userRole
    }
}

// 分发 Action
const actions = {
    setCurrentGroup(context, currentGroup) {
        context.commit('setCurrentGroup', currentGroup)
    },
    setCurrentGroupById(context, groupId) {
        context.commit('setCurrentGroupById', groupId)
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
                    // siteRateMap
                    // siteRateMap: val.groupId
                }
            }
        }
    )]
})

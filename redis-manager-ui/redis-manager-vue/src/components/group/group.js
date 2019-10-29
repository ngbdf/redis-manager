import { store } from '@/vuex/store.js'
import { formatTime } from '@/utils/time.js'
import API from '@/api/api.js'
import message from '@/utils/message.js'

export function getGroupList (user) {
  let url = '/group/getGroupList'
  API.post(
    url,
    user,
    response => {
      if (response.data.code === 0) {
        let groupList = response.data.data
        groupList.forEach(group => {
          group.time = formatTime(group.updateTime)
        })
        store.dispatch('setGroupList', groupList)
      } else {
        message.err('Get group list failed')
      }
    },
    err => {
      message.err(err)
    }
  )
}

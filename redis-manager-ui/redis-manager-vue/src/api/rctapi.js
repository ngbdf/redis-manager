import axios from 'axios'
import message from '@/utils/message.js'
import router from '@/router'
import { store } from '@/vuex/store.js'

// function ajax (url, data = {}, type = 'GET') {
//   return new Promise((resolve, reject) => {
//     let promise
//     if (type === 'GET') {
//       promise = axios.get(url, { params: data })
//     } else {
//       promise = axios.post(url, data)
//     }
//     promise.then(response => {
//       resolve(response)
//     }).catch(error => {
//       message.error(error.message)
//     })
//   })
// }

function ajax (url, params = {}, method = 'GET') {
  return new Promise((resolve, reject) => {
    axios({
      method: method,
      url: url,
      headers: {'UserIp': store.getters.getUserIp},
      data: method === 'POST' || method === 'PUT' ? params : null,
      params: method === 'GET' || method === 'DELETE' ? params : null
      // baseURL: root,
      // withCredentials: true //开启cookies
    }).then(res => {
      resolve(res)
    }).catch(err => {
      if (err.response.status === 401) {
        message.warn('Session timeout')
        router.push({ name: 'login' })
      } else {
        message.error(err.message)
      }
    })
  })
}

export const getDemo = (username, pass) => ajax('/test', {username, pass})

import axios from 'axios'
import message from '@/utils/message.js'
import { store } from '@/vuex/store.js'

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
      reject(err)
      message.error(err.message)
    })
  })
}

if (process.env.NODE_ENV === 'development') {
  axios.defaults.baseURL = '/apis'
} else if (process.env.NODE_ENV === 'production') {
  axios.defaults.baseURL = '/'
}

const service = axios.create({
  timeout: 10000
})

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      message.error(res.message || 'error')
      return Promise.reject(new Error(res.message || 'error'))
    } else {
      return Promise.resolve(res)
    }
  },
  error => {
    // if (error.response.code === 401) {
    //   message.warning('Session timeout')
    //   router.push({ name: 'login' })
    // } else {
    //   return Promise.reject(error)
    // }
    return Promise.reject(error)
  }
)

export const getAnalysisList = () => {
  return service({
    url: `/rdb`,
    method: 'get'

  })
}

export const addAnalysisList = (data) => {
  return service({
    url: `/rdb`,
    method: 'post',
    data
  })
}

export const updateAnalyzeList = (data) => {
  return service({
    url: `/rdb`,
    method: 'put',
    data
  })
}

export const deletAnalyze = (id) => {
  return service({
    url: `/rdb?id=${id}`,
    method: 'delete'
  })
}

export const getAnalyzeResults = () => ajax('/rdb/results')

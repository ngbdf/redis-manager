import axios from 'axios'
import message from '@/utils/message.js'
import { store } from '@/vuex/store.js'

function API (url, method = 'GET', params = {}) {
  return service({
    method: method,
    url: url,
    headers: {'UserIp': store.getters.getUserIp},
    data: method === 'POST' || method === 'PUT' ? params : null,
    params: method === 'GET' || method === 'DELETE' ? params : null
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

export const getCluster = (id) => API(`/cluster/getClusterList/${id}`)

export const getAnalysisList = () => API(`/rdb`)

export const addAnalysisList = (data) => API(`/rdb`, 'POST', data)

export const updateAnalyzeList = (data) => API(`/rdb`, 'PUT', data)

export const deletAnalyze = (id) => API(`/rdb?id=${id}`, 'DELETE')

export const getAnalyzeResults = () => API('/rdb/results')

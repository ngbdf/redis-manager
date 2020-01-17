import axios from 'axios'
import message from '@/utils/message.js'

function ajax (url, data = {}, type = 'GET') {
  return new Promise((resolve, reject) => {
    let promise
    if (type === 'GET') {
      promise = axios.get(url, { params: data })
    } else {
      promise = axios.post(url, data)
    }
    promise.then(response => {
      resolve(response)
    }).catch(error => {
      reject(error)
      message.error(error.message)
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

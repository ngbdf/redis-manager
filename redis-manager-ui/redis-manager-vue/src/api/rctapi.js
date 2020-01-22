import axios from 'axios'
import message from '@/utils/message.js'
import { store } from '@/vuex/store.js'

/**
 * use RCTAPI or API
 * eg: getAnalyseResults
 * async getAnalyseResults (groupId) {
 *   const res = await getAnalyzeResults()
 *   //处理数据
 *   console.log(res.data)
 * }
 * @param {*} url url
 * @param {*} method GET | POST | DELETE | PUT, default GET
 * @param {*} params {}
 */
function RCTAPI (url, method = 'GET', params = {}) {
  return new Promise((resolve, reject) => {
    service({
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
      // reject(err)
      message.error(err.message)
    })
  })
}

/**
 * eg: deletAnalyze
 * async deletAnalyze (groupId) {
 *   const res = await deletAnalyze(1).then(res => {
 *   //处理数据
 *   console.log(res.data.data)
 *   })
 * }
 * @param {*} url url
 * @param {*} method GET | POST | DELETE | PUT, default GET
 * @param {*} params {}
 */
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

export const cronExpress = (cron) => API(`/rdb/test_corn`, 'POST', cron)

export const analyzeJob = (data) => API(`/rdb/allocation_job`, 'POST', data)

export const getClusterNodes = (id) => API(`/node-manage/getAllNodeList/${id}`)

export const getCluster = (id) => API(`/cluster/getClusterList/${id}`)

export const getAnalysisList = () => API(`/rdb`)

export const addAnalysisList = (data) => API(`/rdb`, 'POST', data)

export const updateAnalyzeList = (data) => API(`/rdb`, 'PUT', data)

export const deletAnalyze = (id) => API(`/rdb?id=${id}`, 'DELETE')

export const getAnalyzeResults = () => RCTAPI('/rdb/results')

export const getPieByType = (analyzeResultId) => RCTAPI('/rdb/chart/DataTypeAnalyze', 'GET', {analyzeResultId})

export const getPrefixKeysCount = (analyzeResultId) => RCTAPI('/rdb/line/prefix/PrefixKeyByCount', 'GET', {analyzeResultId})

export const getPrefixKeysMemory = (analyzeResultId) => RCTAPI('/rdb/line/prefix/PrefixKeyByMemory', 'GET', {analyzeResultId})

export const getTop1000KeysByPrefix = (analyzeResultId) => API('/rdb/table/prefix', 'GET', {analyzeResultId})

export const getKeysTTLInfo = (analyzeResultId) => API('/rdb/chart/TTLAnalyze', 'GET', {analyzeResultId})

export const getTop1000KeysByType = (analyzeResultId, type) => RCTAPI('/rdb/top_key', 'GET', { analyzeResultId, type })

export const getTop1000KeysByString = (analyzeResultId) => RCTAPI('/rdb/top_key', 'GET', { analyzeResultId, type: 0 })

export const getTop1000KeysByHash = (analyzeResultId) => RCTAPI('/rdb/top_key', 'GET', { analyzeResultId, type: 5 })

export const getTop1000KeysByList = (analyzeResultId) => RCTAPI('/rdb/top_key', 'GET', { analyzeResultId, type: 10 })

export const getTop1000KeysBySet = (analyzeResultId) => RCTAPI('/rdb/top_key', 'GET', { analyzeResultId, type: 15 })

export const getTimeData = (analyzeResultId) => RCTAPI('/rdb/all/schedule_id', 'GET', { analyzeResultId })

export const getScheduleDetail = (id) => API(`/rdb/schedule_detail/${id}`)

export const cancelAnalyzeTask = (id, scheduleID) => API(`/rdb/cance_job/${id}/${scheduleID}`)

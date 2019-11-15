/* eslint-disable indent */
import axios from 'axios'

// 封装axios
// eslint-disable-next-line space-before-function-paren
function apiAxios(method, url, params, successHandler, failureHandler) {
    axios({
        method: method,
        url: url,
        data: method === 'POST' || method === 'PUT' ? params : null,
        params: method === 'GET' || method === 'DELETE' ? params : null
        // baseURL: root,
        // withCredentials: true //开启cookies
    }).then(function (res) {
        successHandler(res)
    }).catch(function (err) {
        failureHandler(err)
    })
}

export default {
    get: function (url, params, successHandler, failureHandler) {
        return apiAxios('GET', url, params, successHandler, failureHandler)
    },
    post: function (url, params, successHandler, failureHandler) {
        return apiAxios('POST', url, params, successHandler, failureHandler)
    },
    put: function (url, params, successHandler, failureHandler) {
        return apiAxios('PUT', url, params, successHandler, failureHandler)
    },
    delete: function (url, params, successHandler, failureHandler) {
        return apiAxios('DELETE', url, params, successHandler, failureHandler)
    }
}

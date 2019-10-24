// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import 'element-ui/lib/theme-chalk/index.css'
import '../theme/index.css'
import './assets/icon/iconfont.css'
import axios from 'axios'
import apiConfig from '@/api/apiConfig.js'
import vuex from 'vuex'

Vue.use(ElementUI, { locale })
Vue.config.productionTip = false

Vue.prototype.$axios = axios
axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8'
axios.defaults.baseURL = apiConfig.baseUrl
axios.defaults.TIMEOUT = 5 * 60 * 1000
// axios.defaults.withCredentials = true
// axios.defaults.headers.post['Access-Control-Allow-Origin'] = '*'

/* eslint-disable no-new */
new Vue({
  el: '#app',
  vuex,
  router,
  components: { App },
  template: '<App/>'
})

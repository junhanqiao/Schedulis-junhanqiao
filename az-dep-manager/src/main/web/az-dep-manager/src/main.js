import Vue from 'vue'
import Antd from 'ant-design-vue';
import App from './App.vue'
import 'ant-design-vue/dist/antd.css';

import './mock/mock.js'
import axios from 'axios'
import './config/axios'

Vue.prototype.$axios = axios
Vue.config.productionTip = false
Vue.use(Antd)
new Vue({
  render: h => h(App),
}).$mount('#app')

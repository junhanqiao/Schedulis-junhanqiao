import axios from 'axios'

axios.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8'

// 请求拦截器
axios.interceptors.request.use(function(config) {
//  config.baseURL="http://192.168.3.179:8080/"
//  config.params["session.id"]="6bd4499a-15bd-4c35-894e-76f7c71ab65e"
/*  config.headers["Access-Control-Allow-Origin"]="*";
  config.headers["Access-Control-Allow-Headers"]= "X-Requested-With,Content-Type";
  config.headers["Access-Control-Allow-Methods"]="PUT,POST,GET,DELETE,OPTIONS";
*/
  return config
}, function(error) {
  return Promise.reject(error)
})
// 响应拦截器
axios.interceptors.response.use(function(response) {

  return response
}, function(error) {
  return Promise.reject(error)
})
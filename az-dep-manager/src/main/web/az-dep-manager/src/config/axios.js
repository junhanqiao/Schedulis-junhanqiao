import axios from 'axios'

axios.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8'

// 请求拦截器
axios.interceptors.request.use(function(config) {
 config.baseURL="http://192.168.3.179:8080/"
config.params["session.id"]="61c07a4e-0d23-4e87-b493-a6f44ce2b9ab"
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
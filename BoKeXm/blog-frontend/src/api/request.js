import axios from 'axios'
import { Message } from 'element-ui'
import { getToken, removeToken } from '../utils/auth'
import router from '../router'
import store from '../store'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error(error)
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  removeToken()
  try { store.commit('CLEAR_USER') } catch (e) {}
  const currentPath = router.currentRoute && router.currentRoute.path
  if (currentPath !== '/login' && currentPath !== '/register') {
    router.replace({ path: '/login', query: { redirect: currentPath ? router.currentRoute.fullPath : undefined } })
  }
}

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      if (res.code === 401 || res.code === 1006 || res.code === 1007) {
        handleUnauthorized()
      } else {
        Message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('请求错误:', error)
    if (error.response && error.response.status === 401) {
      handleUnauthorized()
    } else {
      Message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service

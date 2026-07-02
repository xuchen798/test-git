import Vue from 'vue'
import Vuex from 'vuex'
import { getToken, setToken, removeToken } from '../utils/auth'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/user'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: getToken(),
    userInfo: null
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = null
    }
  },
  actions: {
    login({ commit }, loginForm) {
      return new Promise((resolve, reject) => {
        loginApi(loginForm).then(res => {
          const token = res.token
          commit('SET_TOKEN', token)
          setToken(token)
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },
    logout({ commit }) {
      return new Promise((resolve) => {
        commit('CLEAR_USER')
        removeToken()
        resolve()
      })
    },
    getUserInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        if (!state.token) {
          commit('CLEAR_USER')
          reject(new Error('未登录'))
          return
        }
        getUserInfoApi().then(res => {
          commit('SET_USER_INFO', res)
          resolve(res)
        }).catch(error => {
          commit('CLEAR_USER')
          removeToken()
          reject(error)
        })
      })
    }
  },
  getters: {
    isLogin: state => !!state.token,
    isAdmin: state => state.userInfo && state.userInfo.role === 1
  }
})

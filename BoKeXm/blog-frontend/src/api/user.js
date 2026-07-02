import request from './request'

export function getCaptcha() {
  return request({
    url: '/user/captcha',
    method: 'get'
  })
}

export function sendForgotPasswordCode(data) {
  return request({
    url: '/user/forgot/send-code',
    method: 'post',
    data
  })
}

export function resetPassword(data) {
  return request({
    url: '/user/forgot/reset',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

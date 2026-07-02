import request from './request'

export function search(params) {
  return request({
    url: '/search',
    method: 'get',
    params
  })
}

export function getSearchHistory() {
  return request({
    url: '/search/history',
    method: 'get'
  })
}

export function clearSearchHistory() {
  return request({
    url: '/search/history',
    method: 'delete'
  })
}

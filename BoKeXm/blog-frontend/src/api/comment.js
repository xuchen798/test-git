import request from './request'

export function getCommentList(params) {
  return request({
    url: '/comment/list',
    method: 'get',
    params
  })
}

export function addComment(data) {
  return request({
    url: '/comment/add',
    method: 'post',
    data
  })
}

export function deleteComment(id) {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

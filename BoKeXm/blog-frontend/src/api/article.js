import request from './request'

export function getArticleList(params) {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

export function getArticleDetail(id) {
  return request({
    url: `/article/${id}`,
    method: 'get'
  })
}

export function publishArticle(data) {
  return request({
    url: '/article/publish',
    method: 'post',
    data
  })
}

export function updateArticle(id, data) {
  return request({
    url: `/article/${id}`,
    method: 'put',
    data
  })
}

export function deleteArticle(id) {
  return request({
    url: `/article/${id}`,
    method: 'delete'
  })
}

export function getHotArticles() {
  return request({
    url: '/article/hot',
    method: 'get'
  })
}

export function getUserArticles(userId, params) {
  return request({
    url: `/article/user/${userId}`,
    method: 'get',
    params
  })
}

export function getCategoryList() {
  return request({
    url: '/article/categories',
    method: 'get'
  })
}

export function likeArticle(id) {
  return request({
    url: `/article/${id}/like`,
    method: 'post'
  })
}

export function favoriteArticle(id) {
  return request({
    url: `/article/${id}/favorite`,
    method: 'post'
  })
}

export function getFavoriteList(params) {
  return request({
    url: '/article/favorites',
    method: 'get',
    params
  })
}

export function getLikeStatus(id) {
  return request({
    url: `/article/${id}/like/status`,
    method: 'get'
  })
}

export function getFavoriteStatus(id) {
  return request({
    url: `/article/${id}/favorite/status`,
    method: 'get'
  })
}

export function updateArticleStatus(id, status) {
  return request({
    url: `/article/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function uploadArticleImage(file) {
  const formData = new FormData()
  formData.append('image', file)
  return request({
    url: '/article/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

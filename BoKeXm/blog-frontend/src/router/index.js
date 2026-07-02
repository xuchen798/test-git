import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import UserProfile from '../views/UserProfile.vue'
import Settings from '../views/Settings.vue'
import WriteArticle from '../views/WriteArticle.vue'
import Search from '../views/Search.vue'
import Admin from '../views/Admin.vue'
import Favorites from '../views/Favorites.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import { getToken } from '../utils/auth'
import store from '../store'

Vue.use(VueRouter)

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => err)
}

const WHITE_LIST = ['/login', '/register', '/forgot-password']

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Login,
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { title: '找回密码' }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail
  },
  {
    path: '/user/:id',
    name: 'UserProfile',
    component: UserProfile
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requireAuth: true, title: '个人设置' }
  },
  {
    path: '/write',
    name: 'WriteArticle',
    component: WriteArticle,
    meta: { requireAuth: true, title: '写文章' }
  },
  {
    path: '/write/:id',
    name: 'EditArticle',
    component: WriteArticle,
    meta: { requireAuth: true, title: '编辑文章' }
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: { title: '搜索' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: Favorites,
    meta: { requireAuth: true, title: '我的收藏' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requireAuth: true, requireAdmin: true, title: '管理后台' }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  
  if (to.meta.title) {
    document.title = to.meta.title + ' - 博客系统'
  } else {
    document.title = '博客系统'
  }

  if (token && WHITE_LIST.includes(to.path)) {
    next({ path: '/' })
    return
  }

  if (WHITE_LIST.includes(to.path)) {
    next()
    return
  }

  if (!token) {
    Vue.prototype.$message.error('未登录')
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requireAdmin) {
    if (store.state.userInfo && store.state.userInfo.role === 1) {
      next()
    } else {
      store.dispatch('getUserInfo').then(() => {
        if (store.state.userInfo && store.state.userInfo.role === 1) {
          next()
        } else {
          Vue.prototype.$message.error('无权访问')
          next({ path: '/' })
        }
      }).catch(() => {
          next({ path: '/login', query: { redirect: to.fullPath } })
        })
    }
    return
  }

  next()
})

export default router

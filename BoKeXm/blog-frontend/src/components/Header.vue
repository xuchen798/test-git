<template>
  <header class="header">
    <div class="header-container">
      <div class="logo" @click="goHome">
        <h1>博客系统</h1>
      </div>
      <nav class="nav-menu">
        <el-menu mode="horizontal" :default-active="activeMenu" background-color="transparent" text-color="#1a3a32" active-text-color="#c9a959" :router="true">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/search">搜索</el-menu-item>
          <el-menu-item v-if="isLogin" index="/write">写文章</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin">管理后台</el-menu-item>
        </el-menu>
      </nav>
      <div class="user-area">
        <template v-if="isLogin">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar">
                {{ userInfo.username ? userInfo.username.charAt(0) : 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo.username }}</span>
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人主页</el-dropdown-item>
              <el-dropdown-item command="settings">个人设置</el-dropdown-item>
              <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="text" @click="goLogin">登录</el-button>
          <el-button type="primary" @click="goRegister">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Header',
  computed: {
    ...mapGetters(['isLogin', 'isAdmin']),
    userInfo() {
      return this.$store.state.userInfo || {}
    },
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    goLogin() {
      this.$router.push('/login')
    },
    goRegister() {
      this.$router.push('/register')
    },
    handleCommand(command) {
      const userId = this.userInfo.id
      switch (command) {
        case 'profile':
          this.$router.push(`/user/${userId}`)
          break
        case 'settings':
          this.$router.push('/settings')
          break
        case 'favorites':
          this.$router.push('/favorites')
          break
        case 'logout':
          this.$confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$store.dispatch('logout').then(() => {
              this.$message.success('退出成功')
              this.$router.replace('/login')
            })
          }).catch(() => {})
          break
      }
    }
  },
  mounted() {
    if (this.isLogin && !this.$store.state.userInfo) {
      this.$store.dispatch('getUserInfo').catch(() => {})
    }
  }
}
</script>

<style scoped>
.header {
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo h1 {
  font-size: 22px;
  font-weight: 700;
  color: #1a3a32;
  margin: 0;
  cursor: pointer;
  font-family: 'Noto Serif SC', serif;
}

.logo h1:hover {
  color: #c9a959;
}

.nav-menu {
  flex: 1;
  margin-left: 40px;
}

.nav-menu .el-menu {
  border-bottom: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.username {
  color: #1a3a32;
  font-size: 14px;
  font-weight: 500;
}

.user-info:hover .username {
  color: #c9a959;
}
</style>

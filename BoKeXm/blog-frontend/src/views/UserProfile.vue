<template>
  <div class="user-profile-page">
    <div class="page-layout">
      <div class="main-content">
        <div class="user-header card">
          <div class="user-avatar">
            <el-avatar :size="80" :src="userInfo.avatar">
              {{ userInfo.username ? userInfo.username.charAt(0) : 'U' }}
            </el-avatar>
          </div>
          <div class="user-info">
            <h2 class="username">{{ userInfo.username }}</h2>
            <p class="bio" v-if="userInfo.bio">{{ userInfo.bio }}</p>
            <p class="bio" v-else>这个人很懒，什么都没写~</p>
            <div class="user-stats">
              <span><strong>{{ articleCount }}</strong> 文章</span>
              <span><strong>{{ userInfo.likeCount || 0 }}</strong> 获赞</span>
              <span><strong>{{ userInfo.favoriteCount || 0 }}</strong> 收藏</span>
            </div>
          </div>
          <div class="user-actions" v-if="isOwnProfile">
            <el-button type="primary" icon="el-icon-setting" @click="goSettings">
              编辑资料
            </el-button>
          </div>
        </div>

        <div class="article-section">
          <div class="section-header">
            <h3>TA的文章</h3>
          </div>

          <div v-for="article in articleList" :key="article.id" class="article-card card">
            <div class="article-card-header">
              <h4 class="article-title">
                <router-link :to="`/article/${article.id}`">{{ article.title }}</router-link>
              </h4>
              <div class="article-actions" v-if="isOwnProfile">
                <el-tag v-if="article.status !== 1" type="info" size="small" effect="plain">已隐藏</el-tag>
                <el-tag v-else type="success" size="small" effect="plain">公开</el-tag>
                <el-button
                  size="mini"
                  :type="article.status === 1 ? 'warning' : 'success'"
                  @click="handleToggleStatus(article)"
                >
                  {{ article.status === 1 ? '隐藏' : '公开' }}
                </el-button>
                <el-button size="mini" type="danger" @click="handleDelete(article)">删除</el-button>
              </div>
            </div>
            <div class="article-summary" v-if="article.summary">
              {{ article.summary }}
            </div>
            <div class="article-meta">
              <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
              <span class="date">{{ formatDate(article.createTime) }}</span>
              <span class="view-count"><i class="el-icon-view"></i> {{ article.viewCount || 0 }}</span>
              <span class="like-count"><i class="el-icon-thumb"></i> {{ article.likeCount || 0 }}</span>
            </div>
          </div>

          <div v-if="articleList.length === 0 && !loading" class="empty-state card">
            <el-empty description="暂无文章" :image-size="80" />
          </div>

          <div v-if="loading" class="loading-state card">
            <el-skeleton :rows="3" animated />
          </div>

          <div class="pagination" v-if="total > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page.sync="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>

      <Sidebar :author="userInfo" />
    </div>
  </div>
</template>

<script>
import { getUserArticles, deleteArticle, updateArticleStatus } from '../api/article'
import { formatDate } from '../utils/format'
import { mapGetters } from 'vuex'
import Sidebar from '../components/Sidebar.vue'

export default {
  name: 'UserProfile',
  components: {
    Sidebar
  },
  data() {
    return {
      userInfo: {},
      articleList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      articleCount: 0
    }
  },
  computed: {
    ...mapGetters(['isLogin']),
    isOwnProfile() {
      if (!this.isLogin || !this.$store.state.userInfo) return false
      return this.$store.state.userInfo.id === parseInt(this.$route.params.id)
    }
  },
  watch: {
    '$route.params.id'() {
      this.loadUserArticles()
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadUserArticles()
  },
  methods: {
    formatDate,
    loadUserInfo() {
      const userId = parseInt(this.$route.params.id)
      if (this.isOwnProfile && this.$store.state.userInfo) {
        this.userInfo = { ...this.$store.state.userInfo }
      }
      // 后续可扩展：调用 /user/{id} 接口获取其他用户信息
    },
    loadUserArticles() {
      const userId = this.$route.params.id
      if (!userId) return
      
      this.loading = true
      getUserArticles(userId, {
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }).then(res => {
        this.articleList = res.records || []
        this.total = res.total || 0
        this.articleCount = res.total || 0
        if (this.articleList.length > 0) {
          const first = this.articleList[0]
          this.userInfo = {
            ...this.userInfo,
            id: first.userId,
            username: first.username || this.userInfo.username,
            avatar: first.avatar || this.userInfo.avatar,
            bio: first.bio || this.userInfo.bio
          }
        }
      }).catch(() => {}).finally(() => {
        this.loading = false
      })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadUserArticles()
      window.scrollTo(0, 0)
    },
    goSettings() {
      this.$router.push('/settings')
    },
    handleToggleStatus(article) {
      const newStatus = article.status === 1 ? 0 : 1
      const tip = newStatus === 0 ? '隐藏后别人将无法看到这篇文章，确定隐藏吗？' : '公开后所有人都能看到这篇文章，确定公开吗？'
      this.$confirm(tip, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateArticleStatus(article.id, newStatus).then(() => {
          this.$message.success(newStatus === 0 ? '已隐藏' : '已公开')
          article.status = newStatus
        }).catch(() => {})
      }).catch(() => {})
    },
    handleDelete(article) {
      this.$confirm('删除文章后不可恢复，确定删除《' + article.title + '》吗？', '警告', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        deleteArticle(article.id).then(() => {
          this.$message.success('删除成功')
          this.currentPage = 1
          this.loadUserArticles()
        }).catch(() => {})
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-profile-page {
  min-height: 100%;
}

.page-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 24px;
  color: #1a3a32;
  margin-bottom: 8px;
  font-family: 'Noto Serif SC', serif;
}

.bio {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.6;
}

.user-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #666;
}

.user-stats strong {
  color: #1a3a32;
  font-size: 18px;
  margin-right: 4px;
}

.user-actions {
  flex-shrink: 0;
}

.section-header {
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 18px;
  margin: 0;
  font-family: 'Noto Serif SC', serif;
}

.article-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.article-card:hover {
  transform: translateY(-2px);
}

.article-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.article-card-header .article-title {
  flex: 1;
  margin-bottom: 0;
}

.article-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.article-title {
  font-size: 18px;
  margin-bottom: 10px;
  font-family: 'Noto Serif SC', serif;
}

.article-title a {
  color: #1a3a32;
  text-decoration: none;
  transition: color 0.3s;
}

.article-title a:hover {
  color: #c9a959;
}

.article-summary {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.article-meta .category {
  background-color: #f0f5f3;
  color: #1a3a32;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state, .loading-state {
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

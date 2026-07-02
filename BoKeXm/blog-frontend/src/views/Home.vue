<template>
  <div class="home-page">
    <div class="page-layout">
      <div class="main-content">
        <div class="article-list">
          <div v-for="article in articleList" :key="article.id" class="article-card card">
            <div class="article-header">
              <h2 class="article-title">
                <router-link :to="`/article/${article.id}`">{{ article.title }}</router-link>
              </h2>
            </div>
            <div class="article-meta">
              <span class="author">
                <el-avatar :size="24" :src="article.avatar">
                  {{ article.username ? article.username.charAt(0) : 'U' }}
                </el-avatar>
                <router-link :to="`/user/${article.userId}`">{{ article.username }}</router-link>
              </span>
              <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
              <span class="date">{{ formatRelativeTime(article.createTime) }}</span>
            </div>
            <div class="article-summary" v-if="article.summary">
              {{ article.summary }}
            </div>
            <div class="article-footer">
              <span class="view-count">
                <i class="el-icon-view"></i> {{ article.viewCount || 0 }}
              </span>
              <span class="like-count">
                <i class="el-icon-thumb"></i> {{ article.likeCount || 0 }}
              </span>
              <span class="comment-count">
                <i class="el-icon-chat-dot-square"></i> {{ article.commentCount || 0 }}
              </span>
            </div>
          </div>

          <div v-if="articleList.length === 0 && !loading" class="empty-state card">
            <el-empty description="暂无文章" />
          </div>

          <div v-if="loading" class="loading-state card">
            <el-skeleton :rows="5" animated />
          </div>
        </div>

        <div class="pagination" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="total"
            :page-size="pageSize"
            :current-page.sync="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <Sidebar />
    </div>
  </div>
</template>

<script>
import { getArticleList } from '../api/article'
import { formatRelativeTime } from '../utils/format'
import Sidebar from '../components/Sidebar.vue'

export default {
  name: 'Home',
  components: {
    Sidebar
  },
  data() {
    return {
      articleList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false
    }
  },
  mounted() {
    this.loadArticles()
  },
  methods: {
    formatRelativeTime,
    loadArticles() {
      this.loading = true
      getArticleList({
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }).then(res => {
        this.articleList = res.records || []
        this.total = res.total || 0
      }).catch(() => {}).finally(() => {
        this.loading = false
      })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadArticles()
      window.scrollTo(0, 0)
    }
  }
}
</script>

<style scoped>
.home-page {
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

.article-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.article-card:hover {
  transform: translateY(-2px);
}

.article-header {
  margin-bottom: 12px;
}

.article-title {
  font-size: 20px;
  margin: 0;
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

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.article-meta .author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.article-meta .author a {
  color: #666;
  text-decoration: none;
}

.article-meta .author a:hover {
  color: #c9a959;
}

.article-meta .category {
  background-color: #f0f5f3;
  color: #1a3a32;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.article-summary {
  color: #666;
  line-height: 1.7;
  margin-bottom: 16px;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #999;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.article-footer span {
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
  margin-top: 30px;
}
</style>

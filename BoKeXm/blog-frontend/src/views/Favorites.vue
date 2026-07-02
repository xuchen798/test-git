<template>
  <div class="favorites-page">
    <div class="favorites-container">
      <div class="favorites-header card">
        <h2>我的收藏</h2>
        <span class="favorites-count" v-if="total > 0">共 {{ total }} 篇</span>
      </div>

      <div class="favorites-list" v-if="articleList.length > 0">
        <div v-for="article in articleList" :key="article.id" class="article-card card">
          <h3 class="article-title">
            <router-link :to="`/article/${article.id}`">{{ article.title }}</router-link>
          </h3>
          <div class="article-summary" v-if="article.summary">
            {{ article.summary }}
          </div>
          <div class="article-meta">
            <span class="author">
              <el-avatar :size="20" :src="article.avatar">
                {{ article.username ? article.username.charAt(0) : 'U' }}
              </el-avatar>
              <router-link :to="`/user/${article.userId}`">{{ article.username }}</router-link>
            </span>
            <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
            <span class="date">{{ formatRelativeTime(article.createTime) }}</span>
          </div>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state card">
        <el-empty description="暂无收藏文章" :image-size="80" />
      </div>

      <div v-if="loading" class="loading-state card">
        <el-skeleton :rows="5" animated />
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
</template>

<script>
import { getFavoriteList } from '../api/article'
import { formatRelativeTime } from '../utils/format'

export default {
  name: 'Favorites',
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
    this.loadFavorites()
  },
  methods: {
    formatRelativeTime,
    loadFavorites() {
      this.loading = true
      getFavoriteList({
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }).then(res => {
        this.articleList = res.records || []
        this.total = res.total || 0
      }).catch(err => {
        if (err && err.message) {
          this.$message.error(err.message)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadFavorites()
      window.scrollTo(0, 0)
    }
  }
}
</script>

<style scoped>
.favorites-page {
  min-height: 100%;
}

.favorites-container {
  max-width: 800px;
  margin: 0 auto;
}

.favorites-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.favorites-header h2 {
  margin: 0;
  font-size: 20px;
  color: #1a3a32;
  font-family: 'Noto Serif SC', serif;
}

.favorites-count {
  font-size: 14px;
  color: #666;
}

.article-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.article-card:hover {
  transform: translateY(-2px);
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
  flex-wrap: wrap;
}

.article-meta .author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.article-meta .author a {
  color: #666;
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

.empty-state,
.loading-state {
  text-align: center;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

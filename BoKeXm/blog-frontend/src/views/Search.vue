<template>
  <div class="search-page">
    <div class="search-container">
      <div class="search-header card">
        <div class="search-bar-row">
          <el-select v-model="categoryId" placeholder="全部分类" size="large" class="category-select" clearable @change="handleSearch">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
          <el-input
            v-model="keyword"
            placeholder="搜索文章标题、摘要、内容或分类..."
            size="large"
            clearable
            class="search-input"
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          >
            <el-button slot="append" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          </el-input>
        </div>
      </div>

      <div class="search-history card" v-if="searchHistory.length > 0 && !keyword">
        <div class="history-header">
          <span class="history-title">搜索历史</span>
          <el-button type="text" size="mini" @click="clearHistory">清空历史</el-button>
        </div>
        <div class="history-tags">
          <el-tag
            v-for="(item, index) in searchHistory"
            :key="index"
            closable
            @click="searchFromHistory(item)"
            @close="removeHistory(index)"
            type="info"
            effect="plain"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>

      <div class="search-results" v-if="keyword">
        <div class="results-header card">
          <span class="results-count">共找到 {{ total }} 条结果</span>
          <el-radio-group v-model="searchType" size="mini" @change="handleSearch">
            <el-radio-button label="article">文章</el-radio-button>
            <el-radio-button label="user">用户</el-radio-button>
          </el-radio-group>
        </div>

        <template v-if="searchType === 'article'">
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
        </template>

        <template v-else>
          <div v-for="user in userList" :key="user.id" class="user-card card">
            <el-avatar :size="60" :src="user.avatar">
              {{ user.username ? user.username.charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-info">
              <h3 class="username">
                <router-link :to="`/user/${user.id}`">{{ user.username }}</router-link>
              </h3>
              <p class="bio" v-if="user.bio">{{ user.bio }}</p>
              <div class="user-stats">
                <span>{{ user.articleCount || 0 }} 文章</span>
                <span>{{ user.likeCount || 0 }} 获赞</span>
              </div>
            </div>
          </div>
        </template>

        <div v-if="(searchType === 'article' ? articleList : userList).length === 0 && !loading" class="empty-state card">
          <el-empty description="没有找到相关结果" :image-size="80" />
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
  </div>
</template>

<script>
import { search, getSearchHistory, clearSearchHistory } from '../api/search'
import { getCategoryList } from '../api/article'
import { formatRelativeTime } from '../utils/format'

export default {
  name: 'Search',
  data() {
    return {
      keyword: '',
      searchType: 'article',
      articleList: [],
      userList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      searchHistory: [],
      categoryId: null,
      categories: []
    }
  },
  mounted() {
    this.loadSearchHistory()
    this.loadCategories().then(() => {
      const query = this.$route.query
      if (query.categoryId) {
        this.categoryId = query.categoryId
        this.resolveCategorySearch()
      } else if (query.keyword) {
        this.keyword = query.keyword
        this.handleSearch()
      }
    })
  },
  methods: {
    formatRelativeTime,
    loadSearchHistory() {
      if (!this.$store.state.token) {
        this.searchHistory = []
        return
      }
      getSearchHistory().then(res => {
        this.searchHistory = res || []
      }).catch(() => {})
    },
    loadCategories() {
      return getCategoryList().then(res => {
        this.categories = res || []
      }).catch(() => {
        this.categories = []
      })
    },
    resolveCategorySearch() {
      if (!this.categoryId || this.categories.length === 0) return
      const category = this.categories.find(c => String(c.id) === String(this.categoryId))
      if (category) {
        this.searchType = 'article'
        this.handleSearch()
      }
    },
    handleSearch() {
      const hasKeyword = !!(this.keyword && this.keyword.trim())
      const hasCategory = this.searchType === 'article' && !!this.categoryId
      if (!hasKeyword && !hasCategory) {
        this.articleList = []
        this.userList = []
        this.total = 0
        return
      }
      this.currentPage = 1
      this.doSearch()
    },
    doSearch() {
      this.loading = true
      const params = {
        keyword: this.keyword,
        type: this.searchType,
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }
      if (this.searchType === 'article' && this.categoryId) {
        params.categoryId = this.categoryId
      }
      search(params).then(res => {
        if (this.searchType === 'article') {
          this.articleList = res.records || []
        } else {
          this.userList = res.records || []
        }
        this.total = res.total || 0
      }).catch(() => {}).finally(() => {
        this.loading = false
      })
    },
    searchFromHistory(keyword) {
      this.keyword = keyword
      this.handleSearch()
    },
    removeHistory(index) {
      this.searchHistory.splice(index, 1)
    },
    clearHistory() {
      this.$confirm('确定清空搜索历史吗？', '提示', {
        type: 'warning'
      }).then(() => {
        clearSearchHistory().then(() => {
          this.searchHistory = []
          this.$message.success('已清空')
        }).catch(() => {})
      }).catch(() => {})
    },
    handlePageChange(page) {
      this.currentPage = page
      this.doSearch()
      window.scrollTo(0, 0)
    }
  }
}
</script>

<style scoped>
.search-page {
  min-height: 100%;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 20px;
}

.search-bar-row {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.search-bar-row .category-select {
  width: 200px;
  flex-shrink: 0;
}

.search-bar-row .search-input {
  flex: 1;
}

.search-history {
  margin-bottom: 20px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a3a32;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.history-tags :deep(.el-tag) {
  cursor: pointer;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.results-count {
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

.user-card {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-card:hover {
  transform: translateY(-2px);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 18px;
  margin-bottom: 6px;
  font-family: 'Noto Serif SC', serif;
}

.username a {
  color: #1a3a32;
  text-decoration: none;
  transition: color 0.3s;
}

.username a:hover {
  color: #c9a959;
}

.bio {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.user-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
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

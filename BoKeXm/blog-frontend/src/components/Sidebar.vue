<template>
  <aside class="sidebar">
    <div class="card author-card" v-if="author">
      <div class="author-info">
        <el-avatar :size="64" :src="author.avatar">
          {{ author.username ? author.username.charAt(0) : 'U' }}
        </el-avatar>
        <h3 class="author-name">{{ author.username }}</h3>
        <p class="author-bio" v-if="author.bio">{{ author.bio }}</p>
      </div>
    </div>

    <div class="card hot-articles">
      <h3 class="sidebar-title">热门文章</h3>
      <ul class="article-list">
        <li v-for="(item, index) in hotArticles" :key="item.id" class="article-item">
          <span class="rank" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
          <router-link :to="`/article/${item.id}`" class="article-title">
            {{ item.title }}
          </router-link>
        </li>
        <li v-if="hotArticles.length === 0" class="empty">暂无热门文章</li>
      </ul>
    </div>

    <div class="card categories">
      <h3 class="sidebar-title">文章分类</h3>
      <ul class="category-list">
        <li v-for="category in categories" :key="category.id" class="category-item">
          <router-link :to="{ path: '/search', query: { categoryId: category.id }}">
            <span>{{ category.name }}</span>
            <span class="count">{{ category.articleCount || 0 }}</span>
          </router-link>
        </li>
        <li v-if="categories.length === 0" class="empty">暂无分类</li>
      </ul>
    </div>
  </aside>
</template>

<script>
import { getHotArticles, getCategoryList } from '../api/article'

export default {
  name: 'Sidebar',
  props: {
    author: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      hotArticles: [],
      categories: []
    }
  },
  mounted() {
    this.loadHotArticles()
    this.loadCategories()
  },
  methods: {
    loadHotArticles() {
      getHotArticles().then(res => {
        this.hotArticles = res || []
      }).catch(() => {})
    },
    loadCategories() {
      getCategoryList().then(res => {
        this.categories = res || []
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #1a3a32;
  font-family: 'Noto Serif SC', serif;
}

.author-card {
  text-align: center;
}

.author-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.author-name {
  margin-top: 12px;
  margin-bottom: 8px;
  font-size: 18px;
  color: #1a3a32;
}

.author-bio {
  color: #666;
  font-size: 13px;
  margin: 0;
}

.article-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.article-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.article-item:last-child {
  border-bottom: none;
}

.rank {
  display: inline-block;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background-color: #f0f0f0;
  color: #999;
  font-size: 12px;
  border-radius: 3px;
  margin-right: 10px;
  flex-shrink: 0;
}

.rank.top-three {
  background-color: #c9a959;
  color: #fff;
}

.article-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.article-title:hover {
  color: #c9a959;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item a {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
  font-size: 14px;
}

.category-item:last-child a {
  border-bottom: none;
}

.category-item a:hover {
  color: #c9a959;
}

.count {
  background-color: #f0f0f0;
  color: #999;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.empty {
  text-align: center;
  color: #999;
  padding: 20px 0;
  font-size: 14px;
}
</style>

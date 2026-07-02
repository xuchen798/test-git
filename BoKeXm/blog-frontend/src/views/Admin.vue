<template>
  <div class="admin-page">
    <div class="admin-container">
      <div class="admin-header card">
        <h2>管理后台</h2>
        <p>欢迎回来，管理员</p>
      </div>

      <div class="admin-stats">
        <div class="stat-card card">
          <div class="stat-icon article-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.articleCount || 0 }}</div>
            <div class="stat-label">文章总数</div>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-icon user-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-icon comment-icon">
            <i class="el-icon-chat-dot-square"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.commentCount || 0 }}</div>
            <div class="stat-label">评论总数</div>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-icon view-icon">
            <i class="el-icon-view"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.viewCount || 0 }}</div>
            <div class="stat-label">总浏览量</div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="border-card" class="admin-tabs">
        <el-tab-pane label="文章管理" name="articles">
          <div class="tab-toolbar">
            <el-input
              v-model="articleSearch"
              placeholder="搜索文章标题"
              size="small"
              style="width: 250px;"
              clearable
              @clear="loadArticles"
              @keyup.enter.native="loadArticles"
            >
              <el-button slot="append" icon="el-icon-search" @click="loadArticles"></el-button>
            </el-input>
          </div>
          <el-table :data="articleList" v-loading="loadingArticles" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="username" label="作者" width="120" />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="viewCount" label="浏览量" width="90" />
            <el-table-column prop="likeCount" label="点赞数" width="90" />
            <el-table-column label="状态" width="90">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
                  {{ scope.row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="viewArticle(scope.row.id)">查看</el-button>
                <el-button type="text" size="mini" @click="editArticle(scope.row.id)">编辑</el-button>
                <el-button type="text" size="mini" style="color: #f56c6c;" @click="deleteArticle(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="articleTotal"
              :page-size="pageSize"
              :current-page.sync="articlePage"
              @current-change="loadArticles"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="用户管理" name="users">
          <div class="tab-toolbar">
            <el-input
              v-model="userSearch"
              placeholder="搜索用户名/昵称"
              size="small"
              style="width: 250px;"
              clearable
              @clear="loadUsers"
              @keyup.enter.native="loadUsers"
            >
              <el-button slot="append" icon="el-icon-search" @click="loadUsers"></el-button>
            </el-input>
          </div>
          <el-table :data="userList" v-loading="loadingUsers" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="头像" width="80">
              <template slot-scope="scope">
                <el-avatar :size="36" :src="scope.row.avatar">
                  {{ scope.row.username ? scope.row.username.charAt(0) : 'U' }}
                </el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column label="角色" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.role === 1 ? 'danger' : 'primary'" size="mini">
                  {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="articleCount" label="文章数" width="90" />
            <el-table-column prop="createTime" label="注册时间" width="160" />
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="viewUser(scope.row.id)">查看</el-button>
                <el-button v-if="scope.row.role !== 1" type="text" size="mini" style="color: #f56c6c;" @click="deleteUser(scope.row)">禁用</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="userTotal"
              :page-size="pageSize"
              :current-page.sync="userPage"
              @current-change="loadUsers"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="评论管理" name="comments">
          <el-table :data="commentList" v-loading="loadingComments" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="articleTitle" label="文章" min-width="150" show-overflow-tooltip />
            <el-table-column prop="userNickname" label="评论者" width="120" />
            <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="160" />
            <el-table-column label="操作" width="100" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" style="color: #f56c6c;" @click="deleteComment(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="commentTotal"
              :page-size="pageSize"
              :current-page.sync="commentPage"
              @current-change="loadComments"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { getArticleList, deleteArticle } from '../api/article'
import { deleteComment } from '../api/comment'

export default {
  name: 'Admin',
  data() {
    return {
      activeTab: 'articles',
      statistics: {},
      articleList: [],
      articleTotal: 0,
      articlePage: 1,
      articleSearch: '',
      loadingArticles: false,
      userList: [],
      userTotal: 0,
      userPage: 1,
      userSearch: '',
      loadingUsers: false,
      commentList: [],
      commentTotal: 0,
      commentPage: 1,
      loadingComments: false,
      pageSize: 10
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadArticles()
  },
  methods: {
    loadStatistics() {
      this.statistics = {
        articleCount: 0,
        userCount: 0,
        commentCount: 0,
        viewCount: 0
      }
    },
    loadArticles() {
      this.loadingArticles = true
      getArticleList({
        pageNum: this.articlePage,
        pageSize: this.pageSize,
        keyword: this.articleSearch
      }).then(res => {
        this.articleList = res.records || []
        this.articleTotal = res.total || 0
      }).catch(() => {}).finally(() => {
        this.loadingArticles = false
      })
    },
    loadUsers() {
      this.loadingUsers = true
      this.userList = []
      this.userTotal = 0
      this.loadingUsers = false
    },
    loadComments() {
      this.loadingComments = true
      this.commentList = []
      this.commentTotal = 0
      this.loadingComments = false
    },
    viewArticle(id) {
      this.$router.push(`/article/${id}`)
    },
    editArticle(id) {
      this.$router.push(`/write/${id}`)
    },
    deleteArticle(row) {
      this.$confirm(`确定删除文章《${row.title}》吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        deleteArticle(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadArticles()
        }).catch(() => {})
      }).catch(() => {})
    },
    viewUser(id) {
      this.$router.push(`/user/${id}`)
    },
    deleteUser(row) {
      this.$confirm(`确定禁用用户 ${row.username} 吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        this.$message.success('操作成功')
      }).catch(() => {})
    },
    deleteComment(row) {
      this.$confirm('确定删除这条评论吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteComment(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadComments()
        }).catch(() => {})
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.admin-page {
  min-height: 100%;
}

.admin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1a3a32 0%, #245045 100%);
  color: #fff;
}

.admin-header h2 {
  color: #fff;
  margin-bottom: 8px;
  font-family: 'Noto Serif SC', serif;
}

.admin-header p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.article-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.comment-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.view-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a3a32;
  font-family: 'Noto Serif SC', serif;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.admin-tabs {
  margin-bottom: 20px;
}

.tab-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .admin-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

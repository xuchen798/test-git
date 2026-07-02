<template>
  <div class="article-detail-page">
    <div class="page-layout">
      <div class="main-content">
        <div v-if="loading" class="card">
          <el-skeleton :rows="10" animated />
        </div>

        <div v-else-if="!article" class="empty-state card">
          <el-empty description="文章不存在或已被作者隐藏" :image-size="100" />
        </div>

        <div v-else class="article-card card">
          <el-alert
            v-if="isOwner && article.status !== 1"
            type="info"
            :closable="false"
            show-icon
            title="此文章已隐藏，仅您自己可见"
            style="margin-bottom: 20px;"
          />

          <h1 class="article-title">{{ article.title }}</h1>
          
          <div class="article-meta">
            <span class="author">
              <el-avatar :size="32" :src="article.avatar">
                {{ article.username ? article.username.charAt(0) : 'U' }}
              </el-avatar>
              <div class="author-info">
                <router-link :to="`/user/${article.userId}`" class="author-name">
                  {{ article.username }}
                </router-link>
                <span class="publish-time">{{ formatDate(article.createTime) }}</span>
              </div>
            </span>
            <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
          </div>

          <div class="article-stats">
            <span><i class="el-icon-view"></i> {{ article.viewCount || 0 }} 阅读</span>
            <span><i class="el-icon-thumb"></i> {{ article.likeCount || 0 }} 点赞</span>
            <span><i class="el-icon-collection"></i> {{ article.favoriteCount || 0 }} 收藏</span>
            <span><i class="el-icon-chat-dot-square"></i> {{ article.commentCount || 0 }} 评论</span>
          </div>

          <div class="article-content">
            <mavon-editor
              v-if="article.content"
              :value="article.content"
              :subfield="false"
              :defaultOpen="'preview'"
              :toolbarsFlag="false"
              :editable="false"
              :scrollStyle="true"
              :ishljs="true"
              style="min-height: 300px;"
            />
          </div>

          <div class="article-actions">
            <el-button :type="isLiked ? 'primary' : ''" icon="el-icon-thumb" @click="handleLike">
              {{ isLiked ? '已点赞' : '点赞' }}
            </el-button>
            <el-button :type="isFavorited ? 'primary' : ''" icon="el-icon-star-off" @click="handleFavorite">
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button icon="el-icon-share" @click="handleShare">分享</el-button>
          </div>
        </div>

        <div class="comment-section card" v-if="article">
          <h3>评论 ({{ totalComments }})</h3>
          
          <div class="comment-input" v-if="isLogin">
            <el-input
              type="textarea"
              v-model="commentContent"
              :rows="4"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
            />
            <div class="comment-submit">
              <el-button type="primary" :loading="submittingComment" @click="submitComment">
                发表评论
              </el-button>
            </div>
          </div>
          <div class="comment-input" v-else>
            <el-alert type="info" :closable="false">
              请先<router-link to="/login">登录</router-link>后发表评论
            </el-alert>
          </div>

          <div class="comment-list">
            <div v-for="comment in commentList" :key="comment.id" class="comment-item">
              <el-avatar :size="40" :src="comment.userAvatar">
                {{ comment.userNickname ? comment.userNickname.charAt(0) : 'U' }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <router-link :to="`/user/${comment.userId}`" class="comment-author">
                    {{ comment.userNickname || comment.userUsername }}
                  </router-link>
                  <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-actions">
                  <el-button type="text" size="mini" v-if="canDelete(comment)" @click="deleteComment(comment.id)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>

            <div v-if="commentList.length === 0 && !loadingComments" class="empty-comments">
              <el-empty description="暂无评论，快来抢沙发吧~" :image-size="80" />
            </div>
          </div>

          <div class="pagination" v-if="totalComments > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="totalComments"
              :page-size="commentPageSize"
              :current-page.sync="commentPage"
              @current-change="loadComments"
            />
          </div>
        </div>
      </div>

      <Sidebar :author="authorInfo" />
    </div>
  </div>
</template>

<script>
import { getArticleDetail, likeArticle, favoriteArticle, getLikeStatus, getFavoriteStatus } from '../api/article'
import { getCommentList, addComment, deleteComment } from '../api/comment'
import { formatDate, formatRelativeTime } from '../utils/format'
import { mapGetters } from 'vuex'
import Sidebar from '../components/Sidebar.vue'

export default {
  name: 'ArticleDetail',
  components: {
    Sidebar
  },
  data() {
    return {
      article: null,
      loading: false,
      commentList: [],
      totalComments: 0,
      commentPage: 1,
      commentPageSize: 10,
      loadingComments: false,
      commentContent: '',
      submittingComment: false,
      isLiked: false,
      isFavorited: false,
      authorInfo: null
    }
  },
  computed: {
    ...mapGetters(['isLogin']),
    isOwner() {
      if (!this.isLogin || !this.$store.state.userInfo || !this.article) return false
      return this.$store.state.userInfo.id === this.article.userId
    }
  },
  watch: {
    '$route.params.id'() {
      this.loadArticle()
    }
  },
  mounted() {
    this.loadArticle()
  },
  methods: {
    formatDate,
    formatRelativeTime,
    loadArticle() {
      const id = this.$route.params.id
      if (!id) return
      
      this.loading = true
      getArticleDetail(id).then(res => {
        this.article = res
        this.authorInfo = {
          id: res.userId,
          nickname: res.nickname,
          username: res.username,
          avatar: res.avatar,
          bio: res.bio
        }
        if (this.isLogin) {
          this.loadLikeStatus()
          this.loadFavoriteStatus()
        }
        this.loadComments()
      }).catch(() => {}).finally(() => {
        this.loading = false
      })
    },
    loadLikeStatus() {
      getLikeStatus(this.article.id).then(res => {
        this.isLiked = !!(res && res.liked)
      }).catch(() => {})
    },
    loadFavoriteStatus() {
      getFavoriteStatus(this.article.id).then(res => {
        this.isFavorited = !!(res && res.favorited)
      }).catch(() => {})
    },
    loadComments() {
      if (!this.article) return
      this.loadingComments = true
      getCommentList({
        articleId: this.article.id,
        pageNum: this.commentPage,
        pageSize: this.commentPageSize
      }).then(res => {
        this.commentList = res.records || []
        this.totalComments = res.total || 0
      }).catch(() => {}).finally(() => {
        this.loadingComments = false
      })
    },
    handleLike() {
      if (!this.isLogin) {
        this.$router.push('/login')
        return
      }
      likeArticle(this.article.id).then(res => {
        const isLiked = !!(res && res.liked)
        this.isLiked = isLiked
        this.article.likeCount = isLiked
          ? (this.article.likeCount || 0) + 1
          : Math.max(0, (this.article.likeCount || 0) - 1)
        this.$message.success(isLiked ? '点赞成功' : '取消点赞')
      }).catch(() => {})
    },
    handleFavorite() {
      if (!this.isLogin) {
        this.$router.push('/login')
        return
      }
      favoriteArticle(this.article.id).then(res => {
        const isFavorited = !!(res && res.favorited)
        this.isFavorited = isFavorited
        this.article.favoriteCount = isFavorited
          ? (this.article.favoriteCount || 0) + 1
          : Math.max(0, (this.article.favoriteCount || 0) - 1)
        this.$message.success(isFavorited ? '收藏成功' : '取消收藏')
      }).catch(() => {})
    },
    handleShare() {
      this.$message.success('链接已复制到剪贴板')
    },
    submitComment() {
      if (!this.commentContent.trim()) {
        this.$message.warning('请输入评论内容')
        return
      }
      this.submittingComment = true
      addComment({
        articleId: this.article.id,
        content: this.commentContent.trim()
      }).then(() => {
        this.$message.success('评论成功')
        this.commentContent = ''
        this.commentPage = 1
        this.loadComments()
      }).catch(() => {}).finally(() => {
        this.submittingComment = false
      })
    },
    canDelete(comment) {
      if (!this.isLogin || !this.$store.state.userInfo) return false
      const userId = this.$store.state.userInfo.id
      return comment.userId === userId || (this.article && this.article.userId === userId)
    },
    deleteComment(id) {
      this.$confirm('确定删除这条评论吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteComment(id).then(() => {
          this.$message.success('删除成功')
          this.loadComments()
        }).catch(() => {})
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.article-detail-page {
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
  margin-bottom: 24px;
}

.article-title {
  font-size: 28px;
  color: #1a3a32;
  margin-bottom: 20px;
  line-height: 1.4;
  font-family: 'Noto Serif SC', serif;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 12px;
}

.article-meta .author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  color: #1a3a32;
  font-weight: 500;
  font-size: 14px;
}

.publish-time {
  color: #999;
  font-size: 12px;
}

.article-meta .category {
  background-color: #f0f5f3;
  color: #1a3a32;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
}

.article-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
}

.article-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #333;
  margin-bottom: 30px;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  margin-top: 24px;
  margin-bottom: 16px;
  color: #1a3a32;
  font-family: 'Noto Serif SC', serif;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #c9a959;
  padding-left: 16px;
  color: #666;
  margin: 16px 0;
  background-color: #fafaf7;
  padding: 12px 16px;
}

.article-content :deep(code) {
  background-color: #f5f5f0;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.article-content :deep(pre) {
  background-color: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 16px 0;
}

.article-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: inherit;
}

.article-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.comment-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  font-family: 'Noto Serif SC', serif;
}

.comment-input {
  margin-bottom: 30px;
}

.comment-submit {
  text-align: right;
  margin-top: 10px;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #1a3a32;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-text {
  color: #333;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}

.comment-actions {
  margin-top: 8px;
}

.empty-comments {
  padding: 40px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

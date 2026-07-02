<template>
  <div class="write-article-page">
    <div class="write-container">
      <div class="write-header card">
        <el-input
          v-model="articleForm.title"
          placeholder="请输入文章标题"
          class="title-input"
          maxlength="100"
          show-word-limit
        />
        <div class="header-actions">
          <el-select v-model="articleForm.categoryId" placeholder="选择分类" class="category-select">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
          <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
          <el-button type="primary" @click="publishArticle" :loading="publishing">
            {{ isEdit ? '更新文章' : '发布文章' }}
          </el-button>
        </div>
      </div>

      <div class="write-content card">
        <mavon-editor
          v-model="articleForm.content"
          :ishljs="true"
          :toolbars="toolbars"
          ref="mdEditor"
          @imgAdd="handleImgAdd"
          style="min-height: 500px;"
        />
      </div>

      <div class="write-summary card">
        <el-form label-width="80px">
          <el-form-item label="文章摘要">
            <el-input
              v-model="articleForm.summary"
              type="textarea"
              :rows="3"
              placeholder="文章摘要，用于列表页展示，不填则自动截取"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { publishArticle, updateArticle, getArticleDetail, getCategoryList, uploadArticleImage } from '../api/article'

export default {
  name: 'WriteArticle',
  data() {
    return {
      articleForm: {
        title: '',
        content: '',
        summary: '',
        categoryId: null
      },
      categories: [],
      saving: false,
      publishing: false,
      isEdit: false,
      articleId: null,
      toolbars: {
        bold: true,
        italic: true,
        header: true,
        underline: true,
        strikethrough: true,
        mark: true,
        quote: true,
        ol: true,
        ul: true,
        link: true,
        imagelink: true,
        code: true,
        table: true,
        fullscreen: true,
        undo: true,
        redo: true,
        trash: true,
        save: true,
        navigation: true,
        alignleft: true,
        aligncenter: true,
        alignright: true,
        subfield: true,
        preview: true
      }
    }
  },
  mounted() {
    this.loadCategories()
    const id = this.$route.params.id
    if (id) {
      this.isEdit = true
      this.articleId = id
      this.loadArticle(id)
    }
  },
  methods: {
    loadCategories() {
      getCategoryList().then(res => {
        this.categories = res || []
      }).catch(() => {})
    },
    loadArticle(id) {
      getArticleDetail(id).then(res => {
        this.articleForm.title = res.title
        this.articleForm.content = res.content
        this.articleForm.summary = res.summary || ''
        this.articleForm.categoryId = res.categoryId
      }).catch(() => {})
    },
    validateForm() {
      if (!this.articleForm.title.trim()) {
        this.$message.warning('请输入文章标题')
        return false
      }
      if (!this.articleForm.content.trim()) {
        this.$message.warning('请输入文章内容')
        return false
      }
      if (!this.articleForm.categoryId) {
        this.$message.warning('请选择文章分类')
        return false
      }
      return true
    },
    saveDraft() {
      if (!this.validateForm()) return
      this.saving = true
      const data = {
        title: this.articleForm.title,
        content: this.articleForm.content,
        summary: this.articleForm.summary,
        categoryIds: String(this.articleForm.categoryId),
        status: 0
      }
      this.submitArticle(data, '草稿保存成功')
    },
    publishArticle() {
      if (!this.validateForm()) return
      this.publishing = true
      const data = {
        title: this.articleForm.title,
        content: this.articleForm.content,
        summary: this.articleForm.summary,
        categoryIds: String(this.articleForm.categoryId),
        status: 1
      }
      this.submitArticle(data, '文章发布成功')
    },
    submitArticle(data, successMsg) {
      const request = this.isEdit 
        ? updateArticle(this.articleId, data)
        : publishArticle(data)
      
      request.then(res => {
        this.$message.success(successMsg)
        const articleId = this.isEdit ? this.articleId : (typeof res === 'object' ? res.id : res)
        if (articleId) {
          this.$router.push(`/article/${articleId}`)
        } else {
          this.$message.error('获取文章ID失败，请稍后重试')
        }
      }).catch(err => {
        if (err && err.message) {
          this.$message.error(err.message)
        }
      }).finally(() => {
        this.saving = false
        this.publishing = false
      })
    },
    handleImgAdd(pos, file) {
      uploadArticleImage(file).then(url => {
        this.$refs.mdEditor.$img2Url(pos, url)
      }).catch(err => {
        this.$message.error('图片上传失败：' + (err && err.message ? err.message : '请重试'))
      })
    }
  }
}
</script>

<style scoped>
.write-article-page {
  min-height: 100%;
}

.write-container {
  max-width: 960px;
  margin: 0 auto;
}

.write-header {
  margin-bottom: 20px;
}

.title-input {
  font-size: 20px;
  margin-bottom: 16px;
}

.title-input :deep(.el-input__inner) {
  font-size: 20px;
  height: 50px;
  font-family: 'Noto Serif SC', serif;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  align-items: center;
}

.category-select {
  width: 200px;
  margin-right: auto;
}

.write-content {
  margin-bottom: 20px;
  padding: 0;
}

.write-content :deep(.v-note-wrapper) {
  border-radius: 8px;
  border: none;
}

.write-summary {
  margin-bottom: 20px;
}
</style>

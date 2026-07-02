<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box card">
        <div class="login-header">
          <h1>{{ isLogin ? '欢迎回来' : '注册账号' }}</h1>
          <p>{{ isLogin ? '登录您的博客账号' : '创建一个新的博客账号' }}</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <template v-if="isLogin">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入注册邮箱" prefix-icon="el-icon-message" />
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="邮箱（登录账号，不可修改）" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱（用于登录）" prefix-icon="el-icon-message" />
            </el-form-item>
            <el-form-item label="用户名（显示名）" prop="username">
              <el-input v-model="form.username" placeholder="请输入显示用的用户名" prefix-icon="el-icon-user" />
            </el-form-item>
          </template>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password />
          </el-form-item>

          <template v-if="!isLogin">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="el-icon-lock" show-password />
            </el-form-item>
          </template>

          <template v-if="isLogin">
            <el-form-item label="验证码" prop="captcha">
              <div style="display: flex; gap: 10px;">
                <el-input v-model="form.captcha" placeholder="请输入验证码" prefix-icon="el-icon-key" style="flex: 1;" />
                <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" alt="验证码" title="点击刷新" />
              </div>
            </el-form-item>
          </template>

          <template v-else>
            <el-form-item label="验证码" prop="captcha">
              <div style="display: flex; gap: 10px;">
                <el-input v-model="form.captcha" placeholder="请输入验证码" prefix-icon="el-icon-key" style="flex: 1;" />
                <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" alt="验证码" title="点击刷新" />
              </div>
            </el-form-item>
          </template>

          <el-form-item>
            <el-button type="primary" style="width: 100%;" :loading="submitting" @click="handleSubmit">
              {{ isLogin ? '登录' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
          <a @click="toggleMode">{{ isLogin ? '立即注册' : '立即登录' }}</a>
        </div>
        <div v-if="isLogin" class="login-footer" style="margin-top: 10px;">
          <a @click="goForgot">忘记密码？</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { register, getCaptcha } from '../api/user'

export default {
  name: 'Login',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      isLogin: true,
      form: {
        username: '',
        password: '',
        email: '',
        confirmPassword: '',
        captcha: '',
        captchaKey: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: '请输入验证码', trigger: 'blur' }
        ]
      },
      submitting: false,
      captchaUrl: ''
    }
  },
  computed: {
    formRef() {
      return this.$refs.formRef
    }
  },
  watch: {
    '$route.path'() {
      this.isLogin = this.$route.path === '/login'
      this.resetForm()
    }
  },
  mounted() {
    this.isLogin = this.$route.path === '/login'
    this.refreshCaptcha()
  },
  methods: {
    toggleMode() {
      if (this.isLogin) {
        this.$router.push('/register')
      } else {
        this.$router.push('/login')
      }
    },
    goForgot() {
      this.$router.push('/forgot-password')
    },
    resetForm() {
      this.form = {
        username: '',
        password: '',
        email: '',
        confirmPassword: '',
        captcha: '',
        captchaKey: ''
      }
      if (this.formRef) {
        this.formRef.clearValidate()
      }
      this.refreshCaptcha()
    },
    refreshCaptcha() {
      getCaptcha().then(data => {
        this.form.captchaKey = data.key
        this.captchaUrl = data.image
      }).catch(() => {
        this.captchaUrl = ''
      })
    },
    handleSubmit() {
      this.formRef.validate((valid, invalidFields) => {
        if (valid) {
          this.submitting = true
          if (this.isLogin) {
            this.handleLogin()
          } else {
            this.handleRegister()
          }
        } else {
          const firstKey = invalidFields ? Object.keys(invalidFields)[0] : null
          if (firstKey && invalidFields[firstKey] && invalidFields[firstKey].length > 0) {
            this.$message.error(invalidFields[firstKey][0].message)
          } else {
            this.$message.error(this.isLogin ? '请填写邮箱、密码和验证码' : '请完整填写注册信息')
          }
        }
      })
    },
    handleLogin() {
      this.$store.dispatch('login', {
        email: this.form.email,
        password: this.form.password,
        captchaKey: this.form.captchaKey,
        captcha: this.form.captcha
      }).then(() => {
        this.$message.success('登录成功')
        this.$store.dispatch('getUserInfo').then(() => {
          const redirect = this.$route.query.redirect || '/'
          this.$router.push(redirect)
        }).catch(() => {
          const redirect = this.$route.query.redirect || '/'
          this.$router.push(redirect)
        })
      }).catch(() => {
        this.refreshCaptcha()
      }).finally(() => {
        this.submitting = false
      })
    },
    handleRegister() {
      register({
        username: this.form.username,
        password: this.form.password,
        email: this.form.email,
        captchaKey: this.form.captchaKey,
        captcha: this.form.captcha
      }).then(() => {
        this.$message.success('注册成功，请登录')
        this.$router.push('/login')
      }).catch(() => {
        this.refreshCaptcha()
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-box {
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  color: #1a3a32;
  margin-bottom: 8px;
  font-family: 'Noto Serif SC', serif;
}

.login-header p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.captcha-img {
  width: 110px;
  height: 38px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  object-fit: cover;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-footer a {
  color: #1a3a32;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer a:hover {
  color: #c9a959;
}
</style>

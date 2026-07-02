<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box card">
        <div class="login-header">
          <h1>找回密码</h1>
          <p>通过用户名和邮箱验证重置您的密码</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入注册时的用户名" prefix-icon="el-icon-user" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入注册时的邮箱" prefix-icon="el-icon-message" />
          </el-form-item>

          <el-form-item label="验证码" prop="captcha">
            <div style="display: flex; gap: 10px;">
              <el-input v-model="form.captcha" placeholder="请输入验证码" prefix-icon="el-icon-key" style="flex: 1;" :disabled="!captchaSent" />
              <el-button
                v-if="!captchaSent"
                type="primary"
                :loading="sendingCode"
                plain
                @click="handleSendCode"
                style="min-width: 130px;"
              >获取验证码</el-button>
              <img
                v-else
                :src="captchaUrl"
                @click="refreshCaptcha"
                class="captcha-img"
                alt="验证码"
                title="点击刷新"
              />
            </div>
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码（6-20位）" prefix-icon="el-icon-lock" show-password />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" prefix-icon="el-icon-lock" show-password />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" style="width: 100%;" :loading="submitting" @click="handleReset">确认找回</el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <a @click="goLogin">返回登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { sendForgotPasswordCode, resetPassword } from '../api/user'

export default {
  name: 'ForgotPassword',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的新密码不一致'))
      } else {
        callback()
      }
    }

    return {
      form: {
        username: '',
        email: '',
        captcha: '',
        captchaKey: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: '请输入验证码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      captchaSent: false,
      sendingCode: false,
      submitting: false,
      captchaUrl: ''
    }
  },
  computed: {
    formRef() {
      return this.$refs.formRef
    }
  },
  methods: {
    goLogin() {
      this.$router.push('/login')
    },
    handleSendCode() {
      if (!this.form.username) {
        this.$message.error('请输入用户名')
        return
      }
      if (!this.form.email) {
        this.$message.error('请输入邮箱')
        return
      }
      const emailRule = this.rules.email[1]
      if (emailRule && emailRule.type === 'email') {
        const reg = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/
        if (!reg.test(this.form.email)) {
          this.$message.error('邮箱格式不正确')
          return
        }
      }
      this.sendingCode = true
      sendForgotPasswordCode({
        username: this.form.username,
        email: this.form.email
      }).then(data => {
        this.form.captchaKey = data.key
        this.captchaUrl = data.image
        this.captchaSent = true
        this.$message.success('身份验证成功，请输入图片中的验证码并设置新密码')
      }).catch(() => {
        this.captchaSent = false
      }).finally(() => {
        this.sendingCode = false
      })
    },
    refreshCaptcha() {
      if (!this.form.username || !this.form.email) {
        this.$message.error('请先填写用户名和邮箱')
        return
      }
      this.sendingCode = true
      sendForgotPasswordCode({
        username: this.form.username,
        email: this.form.email
      }).then(data => {
        this.form.captchaKey = data.key
        this.captchaUrl = data.image
      }).finally(() => {
        this.sendingCode = false
      })
    },
    handleReset() {
      if (!this.captchaSent) {
        this.$message.error('请先点击获取验证码完成身份验证')
        return
      }
      this.formRef.validate((valid, invalidFields) => {
        if (valid) {
          this.submitting = true
          resetPassword({
            username: this.form.username,
            email: this.form.email,
            captchaKey: this.form.captchaKey,
            captcha: this.form.captcha,
            newPassword: this.form.newPassword,
            confirmPassword: this.form.confirmPassword
          }).then(() => {
            this.$message.success('密码重置成功，请使用新密码登录')
            this.$router.push('/login')
          }).finally(() => {
            this.submitting = false
          })
        } else {
          const firstKey = invalidFields ? Object.keys(invalidFields)[0] : null
          if (firstKey && invalidFields[firstKey] && invalidFields[firstKey].length > 0) {
            this.$message.error(invalidFields[firstKey][0].message)
          } else {
            this.$message.error('请完整填写找回密码表单')
          }
        }
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
  width: 130px;
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
}

.login-footer a:hover {
  color: #c9a959;
}
</style>

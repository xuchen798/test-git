<template>
  <div class="settings-page">
    <div class="settings-container">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="基本信息" name="profile">
          <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
            <el-form-item label="头像">
              <el-avatar :size="80" :src="profileForm.avatar">
                {{ profileForm.username ? profileForm.username.charAt(0) : 'U' }}
              </el-avatar>
              <el-upload
                class="avatar-uploader"
                action="/api/user/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-button size="small" type="primary" icon="el-icon-upload">上传头像</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" disabled>
                <template slot="append">登录账号，不可修改</template>
              </el-input>
            </el-form-item>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" placeholder="请输入显示用的用户名" />
            </el-form-item>
            <el-form-item label="个人简介" prop="bio">
              <el-input v-model="profileForm.bio" type="textarea" :rows="4" placeholder="介绍一下自己吧" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPassword" @click="savePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { updateUserInfo, updatePassword } from '../api/user'
import { getToken } from '../utils/auth'

export default {
  name: 'Settings',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      activeTab: 'profile',
      profileForm: {
        username: '',
        bio: '',
        avatar: '',
        email: ''
      },
      profileRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
        ]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
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
      savingProfile: false,
      savingPassword: false
    }
  },
  computed: {
    uploadHeaders() {
      const token = getToken()
      return {
        'Authorization': token ? 'Bearer ' + token : ''
      }
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      if (this.$store.state.userInfo) {
        this.profileForm.username = this.$store.state.userInfo.username || ''
        this.profileForm.bio = this.$store.state.userInfo.bio || ''
        this.profileForm.avatar = this.$store.state.userInfo.avatar || ''
        this.profileForm.email = this.$store.state.userInfo.email || ''
      } else {
        this.$store.dispatch('getUserInfo').then(res => {
          this.profileForm.username = res.username || ''
          this.profileForm.bio = res.bio || ''
          this.profileForm.avatar = res.avatar || ''
          this.profileForm.email = res.email || ''
        }).catch(() => {})
      }
    },
    saveProfile() {
      this.$refs.profileFormRef.validate(valid => {
        if (valid) {
          this.savingProfile = true
          updateUserInfo({
            username: this.profileForm.username,
            bio: this.profileForm.bio,
            avatar: this.profileForm.avatar
          }).then(res => {
            this.$message.success('保存成功')
            this.$store.dispatch('getUserInfo')
          }).catch(() => {}).finally(() => {
            this.savingProfile = false
          })
        }
      })
    },
    savePassword() {
      this.$refs.passwordFormRef.validate(valid => {
        if (valid) {
          this.savingPassword = true
          updatePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          }).then(() => {
            this.$message.success('密码修改成功，请重新登录')
            this.$store.dispatch('logout').then(() => {
              this.$router.push('/login')
            })
          }).catch(() => {}).finally(() => {
            this.savingPassword = false
          })
        }
      })
    },
    handleAvatarSuccess(response) {
      if (response.code === 200) {
        this.profileForm.avatar = response.data
        this.$message.success('头像上传成功')
        this.$store.dispatch('getUserInfo')
      } else {
        this.$message.error(response.message || '上传失败')
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    }
  }
}
</script>

<style scoped>
.settings-page {
  min-height: 100%;
  display: flex;
  justify-content: center;
}

.settings-container {
  width: 100%;
  max-width: 700px;
}

.avatar-uploader {
  margin-left: 20px;
}
</style>

<template>
  <div class="register-page">
    <div class="register-container">
      <a-card class="register-card" :bordered="false">
        <div class="register-header">
          <img src="@/assets/vue.svg" alt="logo" class="register-logo" />
          <h1 class="register-title">注册新账号</h1>
          <p class="register-subtitle">加入校园招聘平台，开启你的求职之旅</p>
        </div>

        <a-form
          :model="form"
          :rules="rules"
          ref="formRef"
          layout="vertical"
          @finish="handleRegister"
        >
          <a-form-item name="username" label="用户名">
            <a-input
              v-model:value="form.username"
              placeholder="请输入用户名"
              size="large"
              allow-clear
            />
          </a-form-item>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item name="password" label="密码">
                <a-input-password
                  v-model:value="form.password"
                  placeholder="请输入密码"
                  size="large"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item name="confirmPassword" label="确认密码">
                <a-input-password
                  v-model:value="form.confirmPassword"
                  placeholder="请再次输入密码"
                  size="large"
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item name="nickname" label="昵称">
            <a-input
              v-model:value="form.nickname"
              placeholder="请输入昵称"
              size="large"
              allow-clear
            />
          </a-form-item>

          <a-form-item name="role" label="注册身份">
            <a-radio-group v-model:value="form.role" size="large" button-style="solid">
              <a-radio-button value="STUDENT">学生</a-radio-button>
              <a-radio-button value="COMPANY">企业</a-radio-button>
            </a-radio-group>
          </a-form-item>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item name="email" label="邮箱">
                <a-input
                  v-model:value="form.email"
                  placeholder="请输入邮箱"
                  size="large"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item name="phone" label="手机号">
                <a-input
                  v-model:value="form.phone"
                  placeholder="请输入手机号"
                  size="large"
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="submitLoading"
            >
              注 册
            </a-button>
          </a-form-item>
        </a-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  role: 'STUDENT',
  email: '',
  phone: ''
})

const validatePassword = async (_rule, value) => {
  if (value && form.password && value !== form.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}

async function handleRegister() {
  submitLoading.value = true
  try {
    const { confirmPassword, ...data } = form
    await register(data)
    message.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 50%, #0050b3 100%);
  padding: 24px;
}

.register-container {
  width: 100%;
  max-width: 520px;
}

.register-card {
  border-radius: 12px;
  padding: 20px 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-logo {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #999;
}

.register-footer a {
  color: #1890ff;
  font-weight: 500;
  margin-left: 4px;
}
</style>

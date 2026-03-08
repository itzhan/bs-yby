<template>
  <div class="login-page">
    <div class="login-container">
      <a-card class="login-card" :bordered="false">
        <div class="login-header">
          <img src="@/assets/vue.svg" alt="logo" class="login-logo" />
          <h1 class="login-title">校园招聘平台</h1>
          <p class="login-subtitle">欢迎回来，请登录你的账号</p>
        </div>

        <a-form
          :model="form"
          :rules="rules"
          ref="formRef"
          layout="vertical"
          @finish="handleLogin"
        >
          <a-form-item name="username" label="用户名">
            <a-input
              v-model:value="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix="h(UserOutlined)"
              allow-clear
            />
          </a-form-item>
          <a-form-item name="password" label="密码">
            <a-input-password
              v-model:value="form.password"
              placeholder="请输入密码"
              size="large"
              :prefix="h(LockOutlined)"
              allow-clear
            />
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loginLoading"
            >
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loginLoading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  loginLoading.value = true
  try {
    await userStore.login(form.username, form.password)
    message.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch {
    // handled by interceptor
  } finally {
    loginLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 50%, #0050b3 100%);
  padding: 24px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  border-radius: 12px;
  padding: 20px 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #999;
}

.login-footer a {
  color: #1890ff;
  font-weight: 500;
  margin-left: 4px;
}
</style>

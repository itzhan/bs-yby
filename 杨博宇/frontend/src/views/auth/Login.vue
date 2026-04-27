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
/* 全局字体优化 - 和首页统一的现代无衬线字体 */
:deep(*) {
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* 登录页整体容器 - 高级渐变背景+半透明招聘主题底图 */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fbff 0%, #edf5ff 100%);
  position: relative;
  overflow: hidden;
}

/* 替换为更贴合校园招聘的背景图（职场/校园办公场景），透明度保持0.12 */
.login-page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 新背景图：校园招聘/职场办公场景，更贴合主题 */
  background: url("https://picsum.photos/id/1076/2000/1000") center/cover no-repeat;
  opacity: 0.12; /* 保持原有透明度，不调整 */
  pointer-events: none;
}

/* 登录卡片 - 圆角+轻阴影+悬浮动效，和首页卡片风格统一 */
.login-card {
  width: 420px;
  padding: 48px 40px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border: 1px solid #f0f4f8;
  text-align: center;
  position: relative;
  z-index: 10;
  transition: all 0.3s ease;
}

.login-card:hover {
  box-shadow: 0 12px 32px rgba(0,0,0,0.08);
}

/* 登录logo/头像样式 */
.login-avatar {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  background-color: #e8f4f8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #3182ce;
}

/* 登录标题 */
.login-title {
  font-size: 28px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

/* 登录副标题 */
.login-subtitle {
  font-size: 14px;
  color: #718096;
  margin-bottom: 32px;
}

/* 表单样式 - 和首页搜索框风格统一 */
.login-form {
  text-align: left;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.ant-form-item-label> label) {
  font-size: 14px;
  color: #4a5568;
  font-weight: 500;
}

.login-form :deep(.ant-input) {
  height: 52px;
  font-size: 15px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  padding: 0 16px;
}

.login-form :deep(.ant-input-password) {
  height: 52px;
}

/* 登录按钮 - 和首页按钮风格统一 */
.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background-color: #3182ce;
  border: none;
  margin-top: 8px;
}

.login-btn:hover {
  background-color: #2b6cb0;
}

/* 注册链接 */
.register-link {
  margin-top: 20px;
  font-size: 14px;
  color: #718096;
}

.register-link a {
  color: #3182ce;
  font-weight: 500;
  text-decoration: none;
}

.register-link a:hover {
  color: #2b6cb0;
}
</style>

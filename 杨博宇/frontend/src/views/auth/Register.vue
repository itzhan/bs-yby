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
/* 全局字体+盒模型 - 统一基础样式 */
:deep(*) {
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
  box-sizing: border-box; /* 彻底解决宽度溢出 */
}

/* 注册页整体容器 - 和登录页完全一致 */
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fbff 0%, #edf5ff 100%);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

/* 背景图 - 和登录页完全一致 */
.register-page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url("https://picsum.photos/id/1076/2000/1000") center/cover no-repeat;
  opacity: 0.12;
  pointer-events: none;
}

/* 注册容器 - 层级+宽度控制 */
.register-container {
  width: 100%;
  max-width: 520px;
  position: relative;
  z-index: 10;
}

/* 注册卡片 - 和登录页完全一致的样式 */
.register-card {
  width: 100%;
  padding: 48px 40px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border: 1px solid #f0f4f8;
  text-align: center;
  transition: all 0.3s ease;
}

.register-card:hover {
  box-shadow: 0 12px 32px rgba(0,0,0,0.08);
}

/* 注册头部 - 和登录页对齐 */
.register-header {
  text-align: center;
  margin-bottom: 32px;
}

/* Logo容器 - 替换img，和登录页样式一致 */
.register-logo-box {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  background-color: #e8f4f8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #1890ff;
}

/* 注册标题 - 和登录页一致 */
.register-title {
  font-size: 28px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

/* 注册副标题 - 和登录页一致 */
.register-subtitle {
  font-size: 14px;
  color: #718096;
  margin-bottom: 0;
}

/* 表单容器 - 统一对齐 */
.register-form {
  text-align: left;
  width: 100%;
}

/* 单行表单项 - 统一间距和宽度 */
:deep(.register-form .ant-form-item) {
  margin-bottom: 20px;
  width: 100%;
}

/* 一行两列表单布局 - 核心修复：替换栅格为flex，避免冲突 */
.form-row {
  display: flex;
  gap: 16px; /* 两列间距，避免重叠 */
  width: 100%;
  margin-bottom: 20px;
}

.form-row :deep(.ant-form-item) {
  flex: 1; /* 两列平分宽度 */
  margin-bottom: 0 !important; /* 取消行内表单项底部间距 */
}

/* 标签样式 - 和登录页一致 */
:deep(.register-form .ant-form-item-label) {
  font-size: 14px;
  color: #4a5568;
  font-weight: 500;
  padding: 0 0 8px 0;
}

/* 输入框核心修复：移除size="large"，统一固定样式，解决内嵌问题 */
:deep(.register-form .ant-input) {
  height: 52px !important; /* 固定高度，覆盖AntD默认值 */
  font-size: 15px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  padding: 0 16px;
  line-height: 52px; /* 行高和高度一致，文字垂直居中 */
}

/* 密码框样式 - 同步修复 */
:deep(.register-form .ant-input-password) {
  height: 52px !important;
}

/* 密码框内部input - 彻底解决内嵌小框 */
:deep(.register-form .ant-input-password .ant-input) {
  height: 100% !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
}

/* 单选按钮样式 - 贴合登录页风格 */
:deep(.register-form .ant-radio-button-wrapper) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  border-color: #e2e8f0;
  color: #4a5568;
}

:deep(.register-form .ant-radio-button-wrapper-checked) {
  background-color: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

/* 注册按钮 - 和登录页完全一致 */
.register-btn {
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background-color: #1890ff;
  border: none;
  margin-top: 8px;
}

:deep(.register-btn:hover) {
  background-color: #096dd9;
}

/* 底部链接 - 和登录页一致 */
.register-footer {
  text-align: center;
  font-size: 14px;
  color: #718096;
  margin-top: 20px;
}

.register-footer a {
  color: #1890ff;
  font-weight: 500;
  margin-left: 4px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.register-footer a:hover {
  color: #096dd9;
}
</style>

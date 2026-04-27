<template>
  <a-layout class="default-layout">
    <!-- Header -->
    <a-layout-header class="layout-header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <img src="@/assets/vue.svg" class="login-logo" />
          <span class="logo-text">&nbsp&nbsp校园招聘平台</span>
        </div>
        <a-menu
          mode="horizontal"
          :selected-keys="selectedKeys"
          class="nav-menu"
        >
          <a-menu-item key="/">
            <router-link to="/">首页</router-link>
          </a-menu-item>
          <a-menu-item key="/jobs">
            <router-link to="/jobs">岗位招聘</router-link>
          </a-menu-item>
          <a-menu-item key="/companies">
            <router-link to="/companies">企业名录</router-link>
          </a-menu-item>
          <a-menu-item key="/jobfairs">
            <router-link to="/jobfairs">宣讲会</router-link>
          </a-menu-item>
          <a-menu-item key="/announcements">
            <router-link to="/announcements">公告通知</router-link>
          </a-menu-item>
        </a-menu>
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <a-dropdown>
              <a class="user-info" @click.prevent>
                <a-avatar :size="32" style="background-color: #1890ff">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </a-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              </a>
              <template #overlay>
                <a-menu>
                  <!-- 学生菜单 -->
                  <template v-if="userStore.role === 'STUDENT'">
                    <a-menu-item @click="router.push('/my/profile')">
                      <UserOutlined /> 个人档案
                    </a-menu-item>
                    <a-menu-item @click="router.push('/my/resumes')">
                      <FileTextOutlined /> 我的简历
                    </a-menu-item>
                    <a-menu-item @click="router.push('/my/applications')">
                      <SendOutlined /> 我的投递
                    </a-menu-item>
                    <a-menu-item @click="router.push('/my/interviews')">
                      <PhoneOutlined /> 我的面试
                    </a-menu-item>
                    <a-menu-item @click="router.push('/my/offers')">
                      <TrophyOutlined /> 我的Offer
                    </a-menu-item>
                    <a-menu-item @click="router.push('/my/messages')">
                      <BellOutlined /> 消息中心
                    </a-menu-item>
                  </template>
                  <!-- 企业菜单 -->
                  <template v-if="userStore.role === 'COMPANY'">
                    <a-menu-item @click="router.push('/company/dashboard')">
                      <DashboardOutlined /> 企业工作台
                    </a-menu-item>
                    <a-menu-item @click="router.push('/company/profile')">
                      <BankOutlined /> 企业信息
                    </a-menu-item>
                    <a-menu-item @click="router.push('/company/jobs')">
                      <SolutionOutlined /> 岗位管理
                    </a-menu-item>
                    <a-menu-item @click="router.push('/company/applications')">
                      <FileSearchOutlined /> 收到的投递
                    </a-menu-item>
                    <a-menu-item @click="router.push('/company/messages')">
                      <BellOutlined /> 消息中心
                    </a-menu-item>
                  </template>
                  <a-menu-divider />
                  <a-menu-item @click="handleLogout">
                    <LogoutOutlined /> 退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <template v-else>
            <a-button type="primary" @click="router.push('/login')">登录</a-button>
            <a-button style="margin-left: 8px" @click="router.push('/register')">注册</a-button>
          </template>
        </div>
      </div>
    </a-layout-header>

    <!-- Content -->
    <a-layout-content class="layout-content">
      <router-view />
    </a-layout-content>

    <!-- Footer -->
    <a-layout-footer class="layout-footer">
      <div class="footer-content">
        <p>校园招聘平台 &copy; {{ new Date().getFullYear() }} All Rights Reserved</p>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  LogoutOutlined, UserOutlined, FileTextOutlined, SendOutlined,
  PhoneOutlined, TrophyOutlined, BellOutlined, DashboardOutlined,
  BankOutlined, SolutionOutlined, FileSearchOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = computed(() => {
  const path = route.path
  if (path === '/') return ['/']
  if (path.startsWith('/jobs')) return ['/jobs']
  if (path.startsWith('/companies')) return ['/companies']
  if (path.startsWith('/jobfairs')) return ['/jobfairs']
  if (path.startsWith('/announcements')) return ['/announcements']
  return ['/']
})

function handleLogout() {
  userStore.logout()
  message.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.default-layout {
  min-height: 100vh;
}

.layout-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
  height: 64px;
  line-height: 64px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 40px;
  flex-shrink: 0;
}

.logo-img {
  height: 32px;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1890ff;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  line-height: 64px;
}

.nav-menu :deep(.ant-menu-item) {
  font-size: 15px;
}

.nav-menu :deep(.ant-menu-item a) {
  color: inherit;
}

.header-right {
  display: flex;
  align-items: center;
  margin-left: auto;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
}

.username {
  font-size: 14px;
}

.layout-content {
  background: #f5f7fa;
  min-height: calc(100vh - 64px - 70px);
}

.layout-footer {
  text-align: center;
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  padding: 24px 50px;
}

.footer-content p {
  margin: 0;
  font-size: 14px;
}
</style>

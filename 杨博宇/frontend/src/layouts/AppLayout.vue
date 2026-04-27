<template>
  <a-layout class="app-layout">
    <!-- Fixed Header -->
    <a-layout-header class="app-header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/home')">
          <span class="logo-icon">🎓</span>
          <span class="logo-text">校园招聘</span>
        </div>

        <!-- Navigation Menu -->
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          class="nav-menu"
          @click="onMenuClick"
        >
          <a-menu-item key="/home">首页</a-menu-item>
          <a-menu-item key="/jobs">岗位</a-menu-item>
          <a-menu-item key="/companies">企业</a-menu-item>
          <a-menu-item key="/job-fairs">宣讲会</a-menu-item>
          <a-menu-item key="/announcements">公告</a-menu-item>
        </a-menu>

        <!-- Right Side -->
        <div class="header-right">
          <template v-if="!userStore.isLoggedIn">
            <a-button type="link" @click="$router.push('/login')">登录</a-button>
            <a-button type="primary" size="small" @click="$router.push('/register')">注册</a-button>
          </template>
          <template v-else>
            <a-badge :count="unreadCount" :offset="[-5, 5]">
              <a-button type="text" @click="goMessages">
                <template #icon><BellOutlined /></template>
              </a-button>
            </a-badge>
            <a-dropdown>
              <a class="user-dropdown" @click.prevent>
                <a-avatar :size="28" style="background-color: #1890ff">
                  {{ displayName }}
                </a-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
                <DownOutlined style="font-size: 12px" />
              </a>
              <template #overlay>
                <a-menu @click="onUserMenuClick">
                  <a-menu-item v-if="isStudent" key="my-center">
                    <UserOutlined /> 个人中心
                  </a-menu-item>
                  <a-menu-item v-if="isCompany" key="company-center">
                    <DashboardOutlined /> 企业工作台
                  </a-menu-item>
                  <a-menu-item key="messages">
                    <MailOutlined /> 消息
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout">
                    <LogoutOutlined /> 退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </div>
      </div>
    </a-layout-header>

    <!-- Content -->
    <a-layout-content class="app-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </a-layout-content>

    <!-- Footer -->
    <a-layout-footer class="app-footer">
      <div class="footer-content">
        <p>校园招聘平台 &copy; {{ currentYear }} All Rights Reserved</p>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  BellOutlined,
  DownOutlined,
  UserOutlined,
  DashboardOutlined,
  MailOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/message'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = ref([])
const unreadCount = ref(0)
const currentYear = new Date().getFullYear()

const isStudent = computed(() => userStore.role === 'student')
const isCompany = computed(() => userStore.role === 'company')

const displayName = computed(() => {
  const info = userStore.userInfo
  if (!info) return '用'
  const name = info.nickname || info.username || ''
  return name.charAt(0) || '用'
})

// Sync selected menu key with current route
watch(
  () => route.path,
  (path) => {
    const menuPaths = ['/home', '/jobs', '/companies', '/job-fairs', '/announcements']
    const matched = menuPaths.find((p) => path.startsWith(p))
    selectedKeys.value = matched ? [matched] : []
  },
  { immediate: true }
)

function onMenuClick({ key }) {
  router.push(key)
}

function onUserMenuClick({ key }) {
  switch (key) {
    case 'my-center':
      router.push('/my/profile')
      break
    case 'company-center':
      router.push('/company/dashboard')
      break
    case 'messages':
      goMessages()
      break
    case 'logout':
      userStore.logout()
      break
  }
}

function goMessages() {
  if (isCompany.value) {
    router.push('/company/messages')
  } else {
    router.push('/my/messages')
  }
}

async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data ?? res ?? 0
  } catch {
    // silent
  }
}

onMounted(() => {
  fetchUnreadCount()
  // Refresh user info if logged in but no userInfo loaded
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.getUserInfo().catch(() => {})
  }
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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

.logo-icon {
  font-size: 24px;
  margin-right: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  line-height: 64px;
  background: transparent;
}

.nav-menu :deep(.ant-menu-item) {
  font-size: 15px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.85);
  padding: 0 4px;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-content {
  margin-top: 64px;
  min-height: calc(100vh - 64px - 70px);
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.app-footer {
  text-align: center;
  background: #fff;
  padding: 24px;
  border-top: 1px solid #f0f0f0;
}

.footer-content p {
  margin: 0;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }

  .logo {
    margin-right: 16px;
  }

  .logo-text {
    display: none;
  }

  .username {
    display: none;
  }

  .content-wrapper {
    padding: 16px;
  }
}
</style>

<template>
  <div class="message-list-page">
    <a-card title="消息中心" :bordered="false" class="main-card">
      <template #extra>
        <a-space>
          <a-badge :count="unreadCount" :offset="[-6, 0]">
            <span style="font-size: 14px; color: #666">未读消息</span>
          </a-badge>
          <a-button type="link" @click="handleMarkAllRead" :disabled="unreadCount === 0">
            全部标记已读
          </a-button>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <a-list
          :data-source="messageList"
          :pagination="listPagination"
          item-layout="horizontal"
        >
          <template #renderItem="{ item }">
            <a-list-item
              :class="['message-item', { unread: !item.isRead }]"
              @click="handleRead(item)"
            >
              <a-list-item-meta>
                <template #avatar>
                  <a-badge :dot="!item.isRead">
                    <a-avatar :style="{ backgroundColor: getTypeColor(item.type) }">
                      <template #icon>
                        <BellOutlined v-if="item.type === 'system'" />
                        <FileTextOutlined v-else-if="item.type === 'application'" />
                        <TeamOutlined v-else-if="item.type === 'interview'" />
                        <CheckCircleOutlined v-else-if="item.type === 'offer'" />
                        <MessageOutlined v-else />
                      </template>
                    </a-avatar>
                  </a-badge>
                </template>
                <template #title>
                  <div class="message-title">
                    <span :class="{ 'font-bold': !item.isRead }">{{ item.title }}</span>
                    <a-tag :color="getTypeColor(item.type)" size="small" style="margin-left: 8px">
                      {{ getTypeLabel(item.type) }}
                    </a-tag>
                  </div>
                </template>
                <template #description>
                  <div class="message-content">{{ item.content }}</div>
                  <div class="message-time">{{ formatDate(item.createTime) }}</div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>

          <template #empty>
            <a-empty description="暂无消息" />
          </template>
        </a-list>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message as antMessage } from 'ant-design-vue'
import {
  BellOutlined,
  FileTextOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  MessageOutlined
} from '@ant-design/icons-vue'
import { getMessages, markAsRead, markAllAsRead, getUnreadCount } from '@/api/message'
import dayjs from 'dayjs'

const loading = ref(false)
const messageList = ref([])
const unreadCount = ref(0)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const listPagination = computed(() => ({
  current: currentPage.value,
  pageSize: pageSize.value,
  total: total.value,
  onChange: (page, size) => {
    currentPage.value = page
    pageSize.value = size
    fetchMessages()
  }
}))

const typeColorMap = {
  system: '#1890ff',
  application: '#52c41a',
  interview: '#722ed1',
  offer: '#faad14'
}

const typeLabelMap = {
  system: '系统通知',
  application: '投递消息',
  interview: '面试消息',
  offer: 'Offer消息'
}

function getTypeColor(type) {
  return typeColorMap[type] || '#1890ff'
}

function getTypeLabel(type) {
  return typeLabelMap[type] || '通知'
}

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessages({
      page: currentPage.value,
      size: pageSize.value
    })
    const data = res.data || {}
    messageList.value = data.records || data.list || data.content || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch {
    // handled
  }
}

async function handleRead(item) {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {
      // handled
    }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    messageList.value.forEach((m) => (m.isRead = true))
    unreadCount.value = 0
    antMessage.success('已全部标记为已读')
  } catch {
    // handled
  }
}

onMounted(() => {
  fetchMessages()
  fetchUnreadCount()
})
</script>

<style scoped>
.message-list-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.message-item {
  cursor: pointer;
  transition: background-color 0.2s;
  padding: 12px 16px;
}
.message-item:hover {
  background-color: #f5f5f5;
}
.message-item.unread {
  background-color: #e6f7ff;
}
.message-title {
  display: flex;
  align-items: center;
}
.font-bold {
  font-weight: 600;
}
.message-content {
  color: #666;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 600px;
}
.message-time {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>

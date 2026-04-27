<template>
  <div class="announcement-detail-page">
    <div class="page-container">
      <!-- Breadcrumb -->
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item><router-link to="/">首页</router-link></a-breadcrumb-item>
        <a-breadcrumb-item><router-link to="/announcements">公告列表</router-link></a-breadcrumb-item>
        <a-breadcrumb-item>公告详情</a-breadcrumb-item>
      </a-breadcrumb>

      <a-spin :spinning="loading">
        <a-card class="detail-card" v-if="announcement">
          <div class="detail-header">
            <h1 class="detail-title">{{ announcement.title }}</h1>
            <div class="detail-meta">
              <a-tag v-if="announcement.type" color="processing">{{ announcement.type }}</a-tag>
              <span class="detail-date">
                <CalendarOutlined /> {{ announcement.publishDate || announcement.createTime }}
              </span>
            </div>
          </div>
          <a-divider />
          <div class="detail-content" v-html="formatContent(announcement.content)"></div>
        </a-card>
        <a-empty v-if="!loading && !announcement" description="公告不存在" />
      </a-spin>

      <div class="back-wrap">
        <a-button @click="router.push('/announcements')">
          <ArrowLeftOutlined /> 返回公告列表
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CalendarOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getAnnouncementDetail } from '@/api/announcement'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const announcement = ref(null)

function formatContent(text) {
  if (!text) return ''
  // If content looks like HTML, use it directly; otherwise convert newlines
  if (text.includes('<p>') || text.includes('<div>') || text.includes('<br')) {
    return text
  }
  return text.replace(/\n/g, '<br/>')
}

async function fetchDetail() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await getAnnouncementDetail(id)
    announcement.value = res.data
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.announcement-detail-page {
  padding: 24px 0;
}

.page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.detail-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.detail-header {
  text-align: center;
  margin-bottom: 8px;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px;
}

.detail-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.detail-date {
  font-size: 14px;
  color: #999;
}

.detail-content {
  font-size: 15px;
  color: #444;
  line-height: 1.8;
  min-height: 200px;
}

.back-wrap {
  margin-top: 24px;
  text-align: center;
}
</style>

<template>
  <div class="announcement-list-page">
    <div class="page-container">
      <div class="page-header">
        <h1 class="page-title">公告通知</h1>
        <p class="page-desc">获取最新的招聘政策和平台公告</p>
      </div>

      <a-spin :spinning="loading">
        <div class="announcement-list">
          <div
            v-for="item in announcementList"
            :key="item.id"
            class="announcement-item"
            @click="router.push(`/announcements/${item.id}`)"
          >
            <div class="announcement-item-left">
              <div class="announcement-dot"></div>
              <div class="announcement-info">
                <h3 class="announcement-title">{{ item.title }}</h3>
                <div class="announcement-meta">
                  <a-tag v-if="item.type" color="processing" size="small">{{ item.type }}</a-tag>
                  <span class="announcement-date">{{ item.publishDate || item.createTime }}</span>
                </div>
              </div>
            </div>
            <RightOutlined class="announcement-arrow" />
          </div>
        </div>
        <a-empty v-if="!loading && announcementList.length === 0" description="暂无公告" style="padding: 60px 0" />
      </a-spin>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination
          v-model:current="pagination.page"
          :total="total"
          :page-size="pagination.size"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { RightOutlined } from '@ant-design/icons-vue'
import { getAnnouncementList } from '@/api/announcement'

const router = useRouter()

const loading = ref(false)
const announcementList = ref([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })

async function fetchAnnouncements() {
  loading.value = true
  try {
    const res = await getAnnouncementList({
      page: pagination.page,
      size: pagination.size,
      status: 1
    })
    announcementList.value = res.data?.records || res.data?.list || res.data || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function onPageChange(page) {
  pagination.page = page
  fetchAnnouncements()
}

onMounted(fetchAnnouncements)
</script>

<style scoped>
.announcement-list-page {
  padding: 24px 0;
}

.page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 15px;
  color: #999;
  margin: 0;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.announcement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.announcement-item:hover {
  background: #f0f7ff;
  border-color: #1890ff;
  transform: translateX(4px);
}

.announcement-item-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.announcement-dot {
  width: 8px;
  height: 8px;
  background: #1890ff;
  border-radius: 50%;
  flex-shrink: 0;
}

.announcement-info {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.announcement-date {
  font-size: 13px;
  color: #999;
}

.announcement-arrow {
  color: #ccc;
  font-size: 12px;
  flex-shrink: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
</style>

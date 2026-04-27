<template>
  <div class="interview-list-page">
    <a-card title="面试列表" :bordered="false" class="main-card">
      <a-spin :spinning="loading">
        <a-row :gutter="[16, 16]">
          <a-col v-for="interview in interviewList" :key="interview.id" :xs="24" :sm="12" :md="12" :lg="8">
            <a-card class="interview-card">
              <template #title>
                <div class="interview-title">
                  <span>{{ interview.jobTitle }}</span>
                  <a-tag :color="statusMap[interview.status]?.color" size="small">
                    {{ statusMap[interview.status]?.text }}
                  </a-tag>
                </div>
              </template>
              <div class="interview-info">
                <div class="info-item">
                  <span class="label">公司：</span>
                  <span class="value">{{ interview.companyName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">面试时间：</span>
                  <span class="value">{{ formatDateTime(interview.interviewTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">面试地点：</span>
                  <span class="value">{{ interview.location }}</span>
                </div>
                <div class="info-item">
                  <span class="label">面试方式：</span>
                  <span class="value">{{ interview.interviewType }}</span>
                </div>
                <div class="info-item" v-if="interview.contact">
                  <span class="label">联系方式：</span>
                  <span class="value">{{ interview.contact }}</span>
                </div>
                <div class="info-item" v-if="interview.description">
                  <span class="label">说明：</span>
                  <span class="value description">{{ interview.description }}</span>
                </div>
              </div>
              <div class="interview-actions" v-if="interview.status === 0">
                <a-button type="primary" @click="handleAccept(interview)">接受</a-button>
                <a-button danger @click="handleReject(interview)">拒绝</a-button>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-empty v-if="!loading && interviewList.length === 0" description="暂无面试安排" />

        <div class="pagination-wrapper" v-if="interviewList.length > 0">
          <a-pagination
            v-model:current="pagination.current"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            show-size-changer
            show-total
            @change="handlePageChange"
            @showSizeChange="handlePageChange"
          />
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getMyInterviews, updateInterviewStatus } from '@/api/interview'
import dayjs from 'dayjs'

const loading = ref(false)
const interviewList = ref([])
const pagination = reactive({ current: 1, pageSize: 9, total: 0 })

const statusMap = {
  0: { text: '待确认', color: 'orange' },
  1: { text: '已接受', color: 'green' },
  2: { text: '已拒绝', color: 'red' },
  3: { text: '已完成', color: 'blue' }
}

function formatDateTime(val) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchInterviews() {
  loading.value = true
  try {
    const res = await getMyInterviews({
      page: pagination.current,
      size: pagination.pageSize
    })
    const data = res.data || {}
    interviewList.value = data.records || data.list || data.content || []
    pagination.total = data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handlePageChange() {
  fetchInterviews()
}

function handleAccept(interview) {
  Modal.confirm({
    title: '确认接受',
    content: '确定要接受该面试邀请吗？',
    onOk: async () => {
      try {
        await updateInterviewStatus(interview.id, { status: 1 })
        message.success('已接受面试邀请')
        fetchInterviews()
      } catch {
        // handled by interceptor
      }
    }
  })
}

function handleReject(interview) {
  Modal.confirm({
    title: '确认拒绝',
    content: '确定要拒绝该面试邀请吗？',
    okType: 'danger',
    onOk: async () => {
      try {
        await updateInterviewStatus(interview.id, { status: 2 })
        message.success('已拒绝面试邀请')
        fetchInterviews()
      } catch {
        // handled by interceptor
      }
    }
  })
}

onMounted(fetchInterviews)
</script>

<style scoped>
.interview-list-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.interview-card {
  height: 100%;
  transition: all 0.3s;
  border: 1px solid #e8e8e8;
}
.interview-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.interview-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.interview-info {
  margin-top: 16px;
}
.info-item {
  margin-bottom: 8px;
  display: flex;
  align-items: flex-start;
}
.label {
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}
.value {
  color: #333;
  flex: 1;
}
.value.description {
  white-space: pre-wrap;
  word-break: break-word;
}
.interview-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>

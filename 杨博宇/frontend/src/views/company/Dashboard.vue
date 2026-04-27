<template>
  <div class="dashboard-page">
    <a-spin :spinning="loading">
      <!-- Welcome Header -->
      <div class="welcome-header">
        <h2>欢迎回来，{{ companyName }}</h2>
        <p>{{ today }} · 校园招聘工作台</p>
      </div>

      <!-- Stat Cards -->
      <a-row :gutter="16" class="stat-cards">
        <a-col :xs="12" :sm="12" :md="6">
          <a-card hoverable>
            <a-statistic title="发布岗位数" :value="stats.jobCount" class="stat-item">
              <template #prefix>
                <AppstoreOutlined style="color: #1890ff" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6">
          <a-card hoverable>
            <a-statistic title="收到投递数" :value="stats.applicationCount" class="stat-item">
              <template #prefix>
                <FileTextOutlined style="color: #52c41a" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6">
          <a-card hoverable>
            <a-statistic title="面试中" :value="stats.interviewCount" class="stat-item">
              <template #prefix>
                <TeamOutlined style="color: #faad14" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6">
          <a-card hoverable>
            <a-statistic title="已录用" :value="stats.hiredCount" class="stat-item">
              <template #prefix>
                <CheckCircleOutlined style="color: #13c2c2" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- Progress Overview -->
      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :md="12">
          <a-card title="招聘进度概览" class="chart-card">
            <div class="progress-list">
              <div class="progress-item">
                <span class="label">岗位发布</span>
                <a-progress :percent="progressPercents.job" status="active" stroke-color="#1890ff" />
              </div>
              <div class="progress-item">
                <span class="label">简历筛选</span>
                <a-progress :percent="progressPercents.application" status="active" stroke-color="#52c41a" />
              </div>
              <div class="progress-item">
                <span class="label">面试安排</span>
                <a-progress :percent="progressPercents.interview" status="active" stroke-color="#faad14" />
              </div>
              <div class="progress-item">
                <span class="label">Offer发放</span>
                <a-progress :percent="progressPercents.offer" status="active" stroke-color="#13c2c2" />
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card title="最近发布的岗位" class="chart-card">
            <a-table
              :columns="applicationColumns"
              :data-source="recentApplications"
              :pagination="false"
              size="small"
              row-key="id"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'status'">
                  <a-tag :color="applicationStatusMap[record.status]?.color">
                    {{ applicationStatusMap[record.status]?.text }}
                  </a-tag>
                </template>
                <template v-if="column.dataIndex === 'createTime'">
                  {{ formatDate(record.createTime) }}
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getCompanyDashboardData } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import {
  AppstoreOutlined,
  FileTextOutlined,
  TeamOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const loading = ref(false)

const companyName = computed(() => userStore.userInfo?.companyName || userStore.userInfo?.username || '企业用户')
const today = dayjs().format('YYYY年MM月DD日')

const stats = reactive({
  jobCount: 0,
  applicationCount: 0,
  interviewCount: 0,
  hiredCount: 0
})

const recentApplications = ref([])

const applicationStatusMap = {
  0: { text: '草稿', color: 'default' },
  1: { text: '待审核', color: 'orange' },
  2: { text: '已发布', color: 'green' },
  3: { text: '已关闭', color: 'gray' },
  4: { text: '已拒绝', color: 'red' }
}

const applicationColumns = [
  { title: '岗位名称', dataIndex: 'title', ellipsis: true },
  { title: '城市', dataIndex: 'city', width: 80 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '发布时间', dataIndex: 'createTime', width: 110 }
]

const progressPercents = computed(() => {
  const total = stats.applicationCount || 1
  return {
    job: Math.min(stats.jobCount * 10, 100),
    application: Math.min(Math.round((stats.applicationCount / total) * 100), 100),
    interview: Math.min(Math.round((stats.interviewCount / total) * 100), 100),
    offer: Math.min(Math.round((stats.hiredCount / total) * 100), 100)
  }
})

function formatDate(val) {
  return val ? dayjs(val).format('MM-DD HH:mm') : '-'
}

async function fetchDashboard() {
  loading.value = true
  try {
    const res = await getCompanyDashboardData()
    const data = res.data || {}
    // 后端返回企业专属数据
    stats.jobCount = data.totalJobs || 0
    stats.applicationCount = data.totalApplications || 0
    const statusStats = data.applicationStatusStats || {}
    stats.interviewCount = statusStats['面试中'] || 0
    stats.hiredCount = statusStats['已录用'] || 0
    // recentJobs 直接映射
    recentApplications.value = (data.recentJobs || []).slice(0, 5).map(j => ({
      id: j.id,
      title: j.title || '-',
      city: j.city || '-',
      status: j.status,
      createTime: j.createdAt
    }))
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboard)
</script>

<style scoped>
.dashboard-page {
  padding: 24px;
}
.welcome-header {
  margin-bottom: 24px;
}
.welcome-header h2 {
  margin-bottom: 4px;
  font-size: 24px;
  color: #1a1a1a;
}
.welcome-header p {
  color: #999;
  font-size: 14px;
}
.stat-cards .ant-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.stat-item {
  text-align: center;
}
.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  min-height: 320px;
}
.progress-list {
  padding: 8px 0;
}
.progress-item {
  margin-bottom: 20px;
}
.progress-item .label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #555;
}
</style>

<template>
  <div class="job-list-page">
    <div class="page-container">
      <!-- Filter Bar -->
      <a-card class="filter-card">
        <a-row :gutter="[16, 12]" align="middle">
          <a-col :xs="24" :sm="12" :md="6">
            <a-input-search
              v-model:value="filters.keyword"
              placeholder="搜索岗位名称"
              allow-clear
              @search="handleSearch"
            />
          </a-col>
          <a-col :xs="12" :sm="6" :md="4">
            <a-select
              v-model:value="filters.city"
              placeholder="城市"
              allow-clear
              style="width: 100%"
              :options="cityOptions"
            />
          </a-col>
          <a-col :xs="12" :sm="6" :md="4">
            <a-select
              v-model:value="filters.category"
              placeholder="岗位分类"
              allow-clear
              style="width: 100%"
              :options="categoryOptions"
            />
          </a-col>
          <a-col :xs="12" :sm="6" :md="4">
            <a-select
              v-model:value="filters.jobType"
              placeholder="工作类型"
              allow-clear
              style="width: 100%"
              :options="jobTypeOptions"
            />
          </a-col>
          <a-col :xs="12" :sm="6" :md="4">
            <a-select
              v-model:value="filters.education"
              placeholder="学历要求"
              allow-clear
              style="width: 100%"
              :options="educationOptions"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="2">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <SearchOutlined /> 搜索
              </a-button>
              <a-tooltip v-if="userStore.role === 'STUDENT' || userStore.role === 'student'"
                title="根据您的个人信息（专业/学历/期望城市/薪资）计算每个岗位的匹配度并排序">
                <a-button
                  :type="isMatchMode ? 'default' : 'dashed'"
                  :loading="matchLoading"
                  @click="toggleMatch"
                >
                  <ThunderboltOutlined />
                  {{ isMatchMode ? '退出匹配' : '智能匹配' }}
                </a-button>
              </a-tooltip>
            </a-space>
          </a-col>
        </a-row>
      </a-card>

      <!-- Match tip -->
      <a-alert
        v-if="isMatchMode"
        message="已按匹配度排序，匹配度越高说明该岗位越符合您的专业/学历/城市/薪资期望"
        type="info"
        show-icon
        closable
        style="margin-bottom: 16px"
      />

      <!-- Job List -->
      <a-spin :spinning="loading || matchLoading">
        <div class="job-list">
          <div
            v-for="job in jobList"
            :key="job.id"
            class="job-item"
            @click="router.push(`/jobs/${job.id}`)"
          >
            <div class="job-item-main">
              <div class="job-item-left">
                <div class="job-item-header">
                  <h3 class="job-item-title">{{ job.title }}</h3>
                  <span class="job-item-salary">
                    {{ job.salaryMin && job.salaryMax ? `${job.salaryMin}-${job.salaryMax}元` : '面议' }}
                  </span>
                  <!-- 匹配度徽章 -->
                  <a-tooltip v-if="isMatchMode && job.matchScore !== undefined"
                    :title="`匹配度 ${job.matchScore}%`">
                    <span :class="['match-badge', matchBadgeClass(job.matchScore)]">
                      {{ job.matchScore }}% 匹配
                    </span>
                  </a-tooltip>
                </div>
                <div class="job-item-company">
                  <BankOutlined style="margin-right: 4px" />
                  {{ job.companyName }}
                </div>
                <div class="job-item-tags">
                  <a-tag color="blue">{{ job.city || '不限' }}</a-tag>
                  <a-tag v-if="job.educationReq">{{ job.educationReq }}</a-tag>
                  <a-tag v-if="job.jobType" color="green">{{ job.jobType }}</a-tag>
                  <a-tag v-if="job.category" color="orange">{{ job.category }}</a-tag>
                </div>
                <div v-if="job.description" class="job-item-desc">
                  {{ job.description }}
                </div>
                <!-- 匹配度进度条 -->
                <div v-if="isMatchMode && job.matchScore !== undefined" class="match-progress-wrap">
                  <a-progress
                    :percent="job.matchScore"
                    :stroke-color="matchProgressColor(job.matchScore)"
                    size="small"
                    :show-info="false"
                  />
                </div>
              </div>
              <div class="job-item-right">
                <div class="job-item-date">
                  <span v-if="job.deadline">截止: {{ job.deadline }}</span>
                  <span v-if="job.createdAt" class="post-date">
                    发布: {{ job.createdAt?.replace('T', ' ').substring(0, 10) }}
                  </span>
                </div>
                <a-button type="primary" size="small" @click.stop="handleApply(job)">
                  投递
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <a-empty v-if="!loading && !matchLoading && jobList.length === 0" description="暂无符合条件的岗位" style="padding: 60px 0" />
      </a-spin>

      <!-- Pagination（匹配模式下不分页，全量返回） -->
      <div class="pagination-wrap" v-if="!isMatchMode && total > 0">
        <a-pagination
          v-model:current="pagination.page"
          :total="total"
          :page-size="pagination.size"
          show-size-changer
          :page-size-options="['10', '20', '30']"
          @change="onPageChange"
          @showSizeChange="onSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, BankOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import { getJobList, matchJobs } from '@/api/job'
import { apply } from '@/api/application'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const matchLoading = ref(false)
const isMatchMode = ref(false)
const jobList = ref([])
const total = ref(0)

const pagination = reactive({ page: 1, size: 10 })

const filters = reactive({
  keyword: route.query.keyword || '',
  city: undefined,
  category: undefined,
  jobType: undefined,
  education: undefined
})

const cityOptions = [
  { label: '北京', value: '北京' },
  { label: '上海', value: '上海' },
  { label: '广州', value: '广州' },
  { label: '深圳', value: '深圳' },
  { label: '杭州', value: '杭州' },
  { label: '成都', value: '成都' },
  { label: '南京', value: '南京' },
  { label: '武汉', value: '武汉' }
]

const categoryOptions = [
  { label: '技术开发', value: '技术开发' },
  { label: '产品运营', value: '产品运营' },
  { label: '设计', value: '设计' },
  { label: '市场销售', value: '市场销售' },
  { label: '行政人事', value: '行政人事' },
  { label: '财务金融', value: '财务金融' }
]

const jobTypeOptions = [
  { label: '全职', value: '全职' },
  { label: '兼职', value: '兼职' },
  { label: '实习', value: '实习' }
]

const educationOptions = [
  { label: '不限', value: '不限' },
  { label: '大专', value: '大专' },
  { label: '本科', value: '本科' },
  { label: '硕士', value: '硕士' },
  { label: '博士', value: '博士' }
]

function matchBadgeClass(score) {
  if (score >= 80) return 'match-badge-high'
  if (score >= 50) return 'match-badge-mid'
  return 'match-badge-low'
}

function matchProgressColor(score) {
  if (score >= 80) return '#52c41a'
  if (score >= 50) return '#1890ff'
  return '#faad14'
}

async function fetchJobs() {
  loading.value = true
  isMatchMode.value = false
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      status: 2,
      ...filters
    }
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') delete params[key]
    })
    const res = await getJobList(params)
    jobList.value = res.data?.records || res.data?.list || res.data || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function toggleMatch() {
  if (isMatchMode.value) {
    isMatchMode.value = false
    fetchJobs()
    return
  }
  matchLoading.value = true
  try {
    const res = await matchJobs()
    const list = res.data || []
    // 将分值归一化为百分比（满分 100）
    jobList.value = list.map(j => ({ ...j, matchScore: Math.min(j.matchScore || 0, 100) }))
    total.value = list.length
    isMatchMode.value = true
  } catch {
    message.error('匹配失败，请先完善个人信息')
  } finally {
    matchLoading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchJobs()
}

function onPageChange(page) {
  pagination.page = page
  fetchJobs()
}

function onSizeChange(_current, size) {
  pagination.page = 1
  pagination.size = size
  fetchJobs()
}

async function handleApply(job) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (userStore.role !== 'STUDENT' && userStore.role !== 'student') {
    message.warning('仅学生用户可投递简历')
    return
  }
  try {
    await apply({ jobId: job.id })
    message.success('投递成功')
  } catch {
    // handled by interceptor
  }
}

onMounted(fetchJobs)
</script>

<style scoped>
.job-list-page {
  padding: 24px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.filter-card {
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.job-item:hover {
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.12);
  border-color: #1890ff;
  transform: translateY(-2px);
}

.job-item-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.job-item-left {
  flex: 1;
  min-width: 0;
}

.job-item-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.job-item-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-item-salary {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
  white-space: nowrap;
}

.match-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
  white-space: nowrap;
}
.match-badge-high { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.match-badge-mid  { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.match-badge-low  { background: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }

.match-progress-wrap {
  margin-top: 6px;
  max-width: 300px;
}

.job-item-company {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.job-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.job-item-desc {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

.job-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.job-item-date {
  font-size: 12px;
  color: #bbb;
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
</style>

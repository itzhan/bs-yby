<template>
  <div class="job-detail-page">
    <div class="page-container">
      <!-- Breadcrumb -->
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item><router-link to="/">首页</router-link></a-breadcrumb-item>
        <a-breadcrumb-item><router-link to="/jobs">岗位列表</router-link></a-breadcrumb-item>
        <a-breadcrumb-item>岗位详情</a-breadcrumb-item>
      </a-breadcrumb>

      <a-spin :spinning="loading">
        <a-row :gutter="24" v-if="job">
          <!-- Left: Job Detail -->
          <a-col :xs="24" :md="16">
            <a-card class="detail-card">
              <!-- Header -->
              <div class="detail-header">
                <div class="detail-header-top">
                  <h1 class="detail-title">{{ job.title }}</h1>
                  <span class="detail-salary">{{ job.salary || '面议' }}</span>
                </div>
                <div class="detail-meta">
                  <a-tag color="blue">{{ job.city || '不限' }}</a-tag>
                  <a-tag v-if="job.education">{{ job.education }}</a-tag>
                  <a-tag v-if="job.jobType" color="green">{{ job.jobType }}</a-tag>
                  <a-tag v-if="job.category" color="orange">{{ job.category }}</a-tag>
                </div>
                <div class="detail-sub-info">
                  <span v-if="job.createTime">
                    <CalendarOutlined /> 发布于 {{ job.createTime }}
                  </span>
                  <a-divider type="vertical" />
                  <span v-if="job.deadline" class="deadline-text">
                    <ClockCircleOutlined /> 截止日期: {{ job.deadline }}
                  </span>
                </div>
              </div>

              <a-divider />

              <!-- Job Description -->
              <div class="detail-section">
                <h3>岗位描述</h3>
                <div class="detail-content" v-html="formatContent(job.description)"></div>
              </div>

              <!-- Job Requirements -->
              <div class="detail-section" v-if="job.requirements">
                <h3>任职要求</h3>
                <div class="detail-content" v-html="formatContent(job.requirements)"></div>
              </div>
            </a-card>
          </a-col>

          <!-- Right: Company Info & Apply -->
          <a-col :xs="24" :md="8">
            <!-- Company Card -->
            <a-card class="company-card" v-if="job.companyId || job.companyName">
              <div class="company-header" @click="goCompany">
                <div class="company-logo">
                  <img v-if="job.companyLogo" :src="job.companyLogo" :alt="job.companyName" />
                  <a-avatar v-else :size="56" style="background-color: #1890ff; font-size: 22px">
                    {{ job.companyName?.charAt(0) }}
                  </a-avatar>
                </div>
                <div class="company-info">
                  <h3 class="company-name">{{ job.companyName }}</h3>
                  <div class="company-meta" v-if="job.companyIndustry || job.companyScale">
                    <span v-if="job.companyIndustry">{{ job.companyIndustry }}</span>
                    <a-divider type="vertical" v-if="job.companyIndustry && job.companyScale" />
                    <span v-if="job.companyScale">{{ job.companyScale }}</span>
                  </div>
                  <div class="company-city" v-if="job.companyCity">
                    <EnvironmentOutlined /> {{ job.companyCity }}
                  </div>
                </div>
              </div>
            </a-card>

            <!-- Apply Card -->
            <a-card class="apply-card">
              <a-button
                type="primary"
                size="large"
                block
                :loading="applyLoading"
                @click="handleApply"
              >
                <SendOutlined /> 立即投递
              </a-button>
              <p class="apply-tip">投递后企业将查看你的简历</p>
            </a-card>
          </a-col>
        </a-row>
        <a-empty v-if="!loading && !job" description="岗位不存在或已下架" />
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  CalendarOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  SendOutlined
} from '@ant-design/icons-vue'
import { getJobDetail } from '@/api/job'
import { apply } from '@/api/application'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const applyLoading = ref(false)
const job = ref(null)

function formatContent(text) {
  if (!text) return ''
  return text.replace(/\n/g, '<br/>')
}

function goCompany() {
  if (job.value?.companyId) {
    router.push(`/companies/${job.value.companyId}`)
  }
}

async function handleApply() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (userStore.role !== 'STUDENT' && userStore.role !== 'student') {
    message.warning('仅学生用户可投递简历')
    return
  }
  applyLoading.value = true
  try {
    await apply({ jobId: job.value.id })
    message.success('投递成功！')
  } catch {
    // handled by interceptor
  } finally {
    applyLoading.value = false
  }
}

async function fetchDetail() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await getJobDetail(id)
    job.value = res.data
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.job-detail-page {
  padding: 24px 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.detail-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.detail-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.detail-salary {
  font-size: 22px;
  color: #ff4d4f;
  font-weight: 700;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.detail-sub-info {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.deadline-text {
  color: #faad14;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid #1890ff;
}

.detail-content {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
}

/* Company Card */
.company-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.company-card:hover {
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.12);
}

.company-header {
  display: flex;
  gap: 16px;
  align-items: center;
}

.company-logo img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: contain;
}

.company-info {
  flex: 1;
  min-width: 0;
}

.company-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px;
}

.company-meta {
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}

.company-city {
  font-size: 13px;
  color: #999;
}

/* Apply Card */
.apply-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.apply-tip {
  font-size: 12px;
  color: #bbb;
  margin: 12px 0 0;
}
</style>

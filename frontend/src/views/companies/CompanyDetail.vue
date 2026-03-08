<template>
  <div class="company-detail-page">
    <div class="page-container">
      <!-- Breadcrumb -->
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item><router-link to="/">首页</router-link></a-breadcrumb-item>
        <a-breadcrumb-item><router-link to="/companies">企业列表</router-link></a-breadcrumb-item>
        <a-breadcrumb-item>企业详情</a-breadcrumb-item>
      </a-breadcrumb>

      <a-spin :spinning="loading">
        <template v-if="company">
          <!-- Company Header -->
          <a-card class="header-card">
            <div class="company-header">
              <div class="company-logo">
                <img v-if="company.logo" :src="company.logo" :alt="company.name" />
                <a-avatar v-else :size="80" style="background-color: #1890ff; font-size: 32px">
                  {{ company.name?.charAt(0) }}
                </a-avatar>
              </div>
              <div class="company-info">
                <h1 class="company-name">{{ company.name }}</h1>
                <div class="company-tags">
                  <a-tag v-if="company.industry" color="blue">{{ company.industry }}</a-tag>
                  <a-tag v-if="company.scale">{{ company.scale }}</a-tag>
                  <a-tag v-if="company.city" color="cyan">
                    <EnvironmentOutlined /> {{ company.city }}
                  </a-tag>
                </div>
                <div class="company-website" v-if="company.website">
                  <GlobalOutlined />
                  <a :href="company.website" target="_blank" rel="noopener">{{ company.website }}</a>
                </div>
              </div>
            </div>
          </a-card>

          <!-- Company Description -->
          <a-card class="section-card" v-if="company.description">
            <h2 class="section-title">公司简介</h2>
            <div class="section-content" v-html="formatContent(company.description)"></div>
          </a-card>

          <!-- Active Jobs -->
          <a-card class="section-card">
            <h2 class="section-title">
              在招岗位
              <a-tag color="blue" style="margin-left: 8px">{{ companyJobs.length }}</a-tag>
            </h2>
            <div class="job-list" v-if="companyJobs.length > 0">
              <div
                v-for="job in companyJobs"
                :key="job.id"
                class="job-item"
                @click="router.push(`/jobs/${job.id}`)"
              >
                <div class="job-item-left">
                  <h3 class="job-title">{{ job.title }}</h3>
                  <div class="job-tags">
                    <a-tag color="blue">{{ job.city || '不限' }}</a-tag>
                    <a-tag v-if="job.education">{{ job.education }}</a-tag>
                    <a-tag v-if="job.jobType" color="green">{{ job.jobType }}</a-tag>
                  </div>
                </div>
                <div class="job-item-right">
                  <span class="job-salary">{{ job.salary || '面议' }}</span>
                </div>
              </div>
            </div>
            <a-empty v-else description="暂无在招岗位" />
          </a-card>
        </template>
        <a-empty v-if="!loading && !company" description="企业不存在" />
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { EnvironmentOutlined, GlobalOutlined } from '@ant-design/icons-vue'
import { getCompanyDetail } from '@/api/company'
import { getJobList } from '@/api/job'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const company = ref(null)
const companyJobs = ref([])

function formatContent(text) {
  if (!text) return ''
  return text.replace(/\n/g, '<br/>')
}

async function fetchData() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const [companyRes, jobsRes] = await Promise.all([
      getCompanyDetail(id),
      getJobList({ companyId: id, status: 2, size: 50 })
    ])
    company.value = companyRes.data
    companyJobs.value = jobsRes.data?.records || jobsRes.data?.list || jobsRes.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.company-detail-page {
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

.header-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.company-header {
  display: flex;
  gap: 24px;
  align-items: center;
}

.company-logo img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: contain;
}

.company-info {
  flex: 1;
}

.company-name {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.company-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.company-website {
  font-size: 14px;
  color: #1890ff;
  display: flex;
  align-items: center;
  gap: 6px;
}

.company-website a {
  color: #1890ff;
}

.section-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 3px solid #1890ff;
  display: flex;
  align-items: center;
}

.section-content {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
}

/* Job List */
.job-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.job-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}

.job-item:hover {
  background: #f0f7ff;
  border-color: #1890ff;
}

.job-item-left {
  flex: 1;
  min-width: 0;
}

.job-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.job-salary {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
  white-space: nowrap;
}
</style>

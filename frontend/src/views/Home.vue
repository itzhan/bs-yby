<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">校园招聘平台</h1>
        <p class="hero-subtitle">连接优秀人才与优质企业，开启你的职业之旅</p>
        <div class="hero-search">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索岗位名称、公司、关键词..."
            size="large"
            enter-button="搜索"
            class="search-input"
            @search="onSearch"
          />
        </div>
        <div class="hero-tags">
          <span class="tag-label">热门搜索：</span>
          <a-tag v-for="tag in hotTags" :key="tag" color="blue" class="hot-tag" @click="onSearch(tag)">
            {{ tag }}
          </a-tag>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section">
      <a-row :gutter="24" justify="center">
        <a-col :xs="12" :sm="6" v-for="item in statsItems" :key="item.label">
          <div class="stat-card">
            <div class="stat-number">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </a-col>
      </a-row>
    </section>

    <!-- Hot Jobs Section -->
    <section class="section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">
            <FireOutlined style="color: #ff4d4f; margin-right: 8px" />
            热门岗位
          </h2>
          <router-link to="/jobs" class="section-more">查看更多 <RightOutlined /></router-link>
        </div>
        <a-spin :spinning="loading">
          <a-row :gutter="[16, 16]">
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="job in hotJobs" :key="job.id">
              <a-card hoverable class="job-card" @click="router.push(`/jobs/${job.id}`)">
                <div class="job-card-header">
                  <h3 class="job-title">{{ job.title }}</h3>
                  <span class="job-salary">{{ formatSalary(job.salaryMin, job.salaryMax) }}</span>
                </div>
                <div class="job-company">{{ job.companyName || '企业' }}</div>
                <div class="job-meta">
                  <a-tag color="blue">{{ job.city || '不限' }}</a-tag>
                  <a-tag v-if="job.educationReq">{{ job.educationReq }}</a-tag>
                  <a-tag v-if="job.jobType" color="green">{{ job.jobType }}</a-tag>
                </div>
              </a-card>
            </a-col>
          </a-row>
          <a-empty v-if="!loading && hotJobs.length === 0" description="暂无热门岗位" />
        </a-spin>
      </div>
    </section>

    <!-- Featured Companies Section -->
    <section class="section section-gray">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">
            <BankOutlined style="color: #1890ff; margin-right: 8px" />
            优质企业
          </h2>
          <router-link to="/companies" class="section-more">查看更多 <RightOutlined /></router-link>
        </div>
        <a-spin :spinning="loading">
          <a-row :gutter="[16, 16]">
            <a-col :xs="12" :sm="8" :md="6" :lg="4" v-for="company in featuredCompanies" :key="company.id">
              <a-card hoverable class="company-card" @click="router.push(`/companies/${company.id}`)">
                <div class="company-logo">
                  <img v-if="company.logo" :src="company.logo" :alt="company.companyName" />
                  <a-avatar v-else :size="64" style="background-color: #1890ff; font-size: 24px">
                    {{ company.companyName?.charAt(0) }}
                  </a-avatar>
                </div>
                <div class="company-name">{{ company.companyName }}</div>
                <div class="company-industry">{{ company.industry || '综合' }}</div>
              </a-card>
            </a-col>
          </a-row>
          <a-empty v-if="!loading && featuredCompanies.length === 0" description="暂无推荐企业" />
        </a-spin>
      </div>
    </section>

    <!-- Latest Announcements Section -->
    <section class="section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">
            <NotificationOutlined style="color: #faad14; margin-right: 8px" />
            最新公告
          </h2>
          <router-link to="/announcements" class="section-more">查看更多 <RightOutlined /></router-link>
        </div>
        <a-spin :spinning="loading">
          <a-list :data-source="latestAnnouncements" :locale="{ emptyText: '暂无公告' }">
            <template #renderItem="{ item }">
              <a-list-item class="announcement-item" @click="router.push(`/announcements/${item.id}`)">
                <a-list-item-meta>
                  <template #title>
                    <span class="announcement-title">{{ item.title }}</span>
                  </template>
                  <template #description>
                    <a-tag v-if="item.type" color="processing">{{ typeMap[item.type] || item.type }}</a-tag>
                    <span class="announcement-date">{{ formatDate(item.createdAt) }}</span>
                  </template>
                </a-list-item-meta>
                <template #extra><RightOutlined style="color: #bbb" /></template>
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { FireOutlined, BankOutlined, NotificationOutlined, RightOutlined } from '@ant-design/icons-vue'
import { getDashboardData } from '@/api/dashboard'
import { getJobList } from '@/api/job'
import { getCompanyList } from '@/api/company'
import { getAnnouncementList } from '@/api/announcement'

const router = useRouter()
const keyword = ref('')
const loading = ref(false)
const hotJobs = ref([])
const featuredCompanies = ref([])
const latestAnnouncements = ref([])
const hotTags = ['Java开发', '前端工程师', '产品经理', '数据分析', '市场营销']
const typeMap = { system: '系统通知', recruitment: '招聘动态', activity: '活动资讯' }

const statsItems = reactive([
  { label: '注册学生', value: 0 },
  { label: '入驻企业', value: 0 },
  { label: '在招岗位', value: 0 },
  { label: '投递次数', value: 0 }
])

function formatSalary (min, max) {
  if (!min && !max) return '面议'
  if (min && max) return `${min}-${max}元`
  return `${min || max}元`
}

function formatDate (str) {
  if (!str) return ''
  return str.replace('T', ' ').substring(0, 16)
}

function onSearch (val) {
  const kw = typeof val === 'string' ? val : keyword.value
  router.push(kw ? { path: '/jobs', query: { keyword: kw } } : '/jobs')
}

async function fetchData () {
  loading.value = true
  try {
    // 并行请求四个接口
    const [dashRes, jobRes, companyRes, announcementRes] = await Promise.all([
      getDashboardData(),
      getJobList({ page: 1, size: 8, status: 2 }),
      getCompanyList({ page: 1, size: 10, auditStatus: 1 }),
      getAnnouncementList({ page: 1, size: 5, status: 1 })
    ])

    // Dashboard 统计数据
    const dash = dashRes.data || {}
    statsItems[0].value = dash.totalStudents || 0
    statsItems[1].value = dash.totalCompanies || 0
    statsItems[2].value = dash.totalJobs || 0
    statsItems[3].value = dash.totalApplications || 0

    // 热门岗位
    hotJobs.value = (jobRes.data?.records || []).slice(0, 8)

    // 优质企业
    featuredCompanies.value = (companyRes.data?.records || []).slice(0, 10)

    // 最新公告
    latestAnnouncements.value = (announcementRes.data?.records || []).slice(0, 5)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.hero-section { background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); padding: 80px 24px 60px; text-align: center; color: #fff; }
.hero-content { max-width: 700px; margin: 0 auto; }
.hero-title { font-size: 42px; font-weight: 700; margin-bottom: 12px; color: #fff; letter-spacing: 2px; }
.hero-subtitle { font-size: 18px; color: rgba(255,255,255,.85); margin-bottom: 32px; }
.hero-search { max-width: 560px; margin: 0 auto 20px; }
.hero-search :deep(.ant-input) { height: 50px; font-size: 16px; border-radius: 8px 0 0 8px; }
.hero-search :deep(.ant-input-search-button) { height: 50px; font-size: 16px; width: 100px; }
.hero-tags { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 6px; }
.tag-label { font-size: 14px; color: rgba(255,255,255,.75); }
.hot-tag { cursor: pointer; border: none; }
.stats-section { max-width: 1200px; margin: -40px auto 0; padding: 0 24px; position: relative; z-index: 10; }
.stat-card { background: #fff; border-radius: 8px; padding: 24px 16px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.stat-number { font-size: 32px; font-weight: 700; color: #1890ff; line-height: 1.2; }
.stat-label { font-size: 14px; color: #666; margin-top: 4px; }
.section { padding: 48px 24px; }
.section-gray { background: #f9fbfd; }
.section-inner { max-width: 1200px; margin: 0 auto; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.section-title { font-size: 22px; font-weight: 600; color: #1a1a1a; margin: 0; }
.section-more { font-size: 14px; color: #1890ff; }
.job-card { border-radius: 8px; height: 100%; transition: all .3s; }
.job-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(24,144,255,.15); }
.job-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.job-title { font-size: 16px; font-weight: 600; color: #1a1a1a; margin: 0; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 8px; }
.job-salary { color: #ff4d4f; font-weight: 600; font-size: 14px; white-space: nowrap; }
.job-company { font-size: 13px; color: #666; margin-bottom: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.job-meta { display: flex; flex-wrap: wrap; gap: 4px; }
.company-card { border-radius: 8px; text-align: center; transition: all .3s; }
.company-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(24,144,255,.12); }
.company-logo { display: flex; justify-content: center; margin-bottom: 12px; }
.company-logo img { width: 64px; height: 64px; border-radius: 8px; object-fit: contain; }
.company-name { font-size: 14px; font-weight: 600; color: #1a1a1a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.company-industry { font-size: 12px; color: #999; margin-top: 4px; }
.announcement-item { cursor: pointer; transition: background .2s; padding: 12px 16px; border-radius: 8px; }
.announcement-item:hover { background: #f0f7ff; }
.announcement-title { font-size: 15px; color: #1a1a1a; }
.announcement-date { font-size: 13px; color: #999; margin-left: 8px; }
</style>

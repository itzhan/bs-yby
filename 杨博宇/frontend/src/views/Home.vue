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

    <!-- 猜你喜欢（仅登录学生可见） -->
    <section class="section" v-if="userStore.isLoggedIn && (userStore.role === 'STUDENT' || userStore.role === 'student') && recommendedJobs.length > 0">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">
            <StarOutlined style="color: #722ed1; margin-right: 8px" />
            猜你喜欢
          </h2>
          <router-link to="/jobs" class="section-more">查看更多 <RightOutlined /></router-link>
        </div>
        <a-spin :spinning="recommendLoading">
          <a-row :gutter="[16, 16]">
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="job in recommendedJobs" :key="job.id">
              <a-card hoverable class="job-card recommend-card" @click="router.push(`/jobs/${job.id}`)">
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
        </a-spin>
      </div>
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
import { FireOutlined, BankOutlined, NotificationOutlined, RightOutlined, StarOutlined } from '@ant-design/icons-vue'
import { getDashboardData } from '@/api/dashboard'
import { getJobList, getRecommendedJobs } from '@/api/job'
import { getCompanyList } from '@/api/company'
import { getAnnouncementList } from '@/api/announcement'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')
const loading = ref(false)
const recommendLoading = ref(false)
const hotJobs = ref([])
const recommendedJobs = ref([])
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

async function fetchRecommended () {
  if (!userStore.isLoggedIn) return
  if (userStore.role !== 'STUDENT' && userStore.role !== 'student') return
  recommendLoading.value = true
  try {
    const res = await getRecommendedJobs(8)
    recommendedJobs.value = res.data || []
  } catch {
    // silent
  } finally {
    recommendLoading.value = false
  }
}

async function fetchData () {
  loading.value = true
  try {
    const [dashRes, jobRes, companyRes, announcementRes] = await Promise.all([
      getDashboardData(),
      getJobList({ page: 1, size: 8, status: 2 }),
      getCompanyList({ page: 1, size: 10, auditStatus: 1 }),
      getAnnouncementList({ page: 1, size: 5, status: 1 })
    ])

    const dash = dashRes.data || {}
    statsItems[0].value = dash.totalStudents || 0
    statsItems[1].value = dash.totalCompanies || 0
    statsItems[2].value = dash.totalJobs || 0
    statsItems[3].value = dash.totalApplications || 0

    hotJobs.value = (jobRes.data?.records || []).slice(0, 8)
    featuredCompanies.value = (companyRes.data?.records || []).slice(0, 10)
    latestAnnouncements.value = (announcementRes.data?.records || []).slice(0, 5)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchData()
  fetchRecommended()
})
</script>

<style scoped>
:deep(*) {
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.hero-section {
  background: linear-gradient(135deg, #f8fbff 0%, #edf5ff 100%);
  padding: 90px 24px 70px;
  text-align: center;
  color: #2d3748;
  position: relative;
  overflow: hidden;
}
.hero-section::before {
  content: "";
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: url("https://picsum.photos/id/180/2000/1000") center/cover no-repeat;
  opacity: 0.05;
  pointer-events: none;
}

.hero-content { max-width: 720px; margin: 0 auto; position: relative; z-index: 1; }
.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
  color: #1a202c;
  letter-spacing: -0.5px;
  line-height: 1.2;
}
.hero-subtitle {
  font-size: 20px;
  color: #4a5568;
  margin-bottom: 40px;
  line-height: 1.5;
}

.hero-search { max-width: 600px; margin: 0 auto 24px; }
.hero-search :deep(.ant-input) {
  height: 56px;
  font-size: 16px;
  border-radius: 12px 0 0 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.hero-search :deep(.ant-input-search-button) {
  height: 56px;
  font-size: 16px;
  width: 110px;
  border-radius: 0 12px 12px 0;
  background-color: #3182ce;
  border: none;
  font-weight: 600;
}
.hero-search :deep(.ant-input-search-button):hover {
  background-color: #2b6cb0;
}

.hero-tags { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 8px; }
.tag-label { font-size: 14px; color: #718096; font-weight: 500; }
.hot-tag {
  cursor: pointer;
  border: none;
  background-color: #e8f4f8;
  color: #2d3748;
  font-weight: 500;
}

.stats-section { max-width: 1200px; margin: -50px auto 0; padding: 0 24px; position: relative; z-index: 10; }
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 20px;
  text-align: center;
  box-shadow: 0 6px 16px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
  border: 1px solid #f0f4f8;
}
.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.06);
}
.stat-number {
  font-size: 40px;
  font-weight: 800;
  color: #3182ce;
  line-height: 1;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 15px;
  color: #4a5568;
  font-weight: 500;
}

.section { padding: 60px 24px; }
.section-gray { background: #fafbfc; }
.section-inner { max-width: 1200px; margin: 0 auto; }

.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 32px; }
.section-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
  display: flex;
  align-items: center;
}
.section-more {
  font-size: 14px;
  color: #3182ce;
  font-weight: 500;
  display: flex;
  align-items: center;
}
.section-more:hover { color: #2b6cb0; }

.job-card {
  border-radius: 12px;
  height: 100%;
  transition: all .3s;
  border: 1px solid #f0f4f8;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.job-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
}
.recommend-card {
  border: 1px solid #f0d6ff;
}
.recommend-card:hover {
  border-color: #722ed1;
  box-shadow: 0 10px 20px rgba(114,46,209,0.1);
}
.job-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.job-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}
.job-salary {
  color: #e53e3e;
  font-weight: 700;
  font-size: 15px;
  white-space: nowrap;
}
.job-company {
  font-size: 14px;
  color: #718096;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.company-card {
  border-radius: 12px;
  text-align: center;
  transition: all .3s;
  border: 1px solid #f0f4f8;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.company-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
}
.company-logo { display: flex; justify-content: center; margin-bottom: 16px; }
.company-logo img { width: 72px; height: 72px; border-radius: 12px; object-fit: contain; }
.company-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.company-industry {
  font-size: 13px;
  color: #718096;
  margin-top: 6px;
}

.announcement-item {
  cursor: pointer;
  transition: background .2s;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #f0f4f8;
  margin-bottom: 8px;
}
.announcement-item:hover { background: #f8fbff; }
.announcement-title {
  font-size: 16px;
  color: #1a202c;
  font-weight: 500;
}
.announcement-date {
  font-size: 13px;
  color: #718096;
  margin-left: 8px;
}
</style>

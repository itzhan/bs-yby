<template>
  <div class="company-list-page">
    <div class="page-container">
      <!-- Search Bar -->
      <a-card class="filter-card">
        <a-row :gutter="16" align="middle">
          <a-col :xs="18" :sm="20">
            <a-input-search
              v-model:value="keyword"
              placeholder="搜索企业名称"
              allow-clear
              size="large"
              @search="handleSearch"
            />
          </a-col>
          <a-col :xs="6" :sm="4">
            <a-button type="primary" size="large" block @click="handleSearch">
              <SearchOutlined /> 搜索
            </a-button>
          </a-col>
        </a-row>
      </a-card>

      <!-- Company Grid -->
      <a-spin :spinning="loading">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" v-for="company in companyList" :key="company.id">
            <a-card hoverable class="company-card" @click="router.push(`/companies/${company.id}`)">
              <div class="company-card-top">
                <div class="company-logo">
                  <img v-if="company.logo" :src="company.logo" :alt="company.companyName" />
                  <a-avatar v-else :size="56" style="background-color: #1890ff; font-size: 22px">
                    {{ company.companyName?.charAt(0) }}
                  </a-avatar>
                </div>
                <div class="company-info">
                  <h3 class="company-name">{{ company.companyName }}</h3>
                  <div class="company-tags">
                    <a-tag v-if="company.industry" color="blue">{{ company.industry }}</a-tag>
                    <a-tag v-if="company.scale">{{ company.scale }}</a-tag>
                  </div>
                </div>
              </div>
              <div class="company-city" v-if="company.city">
                <EnvironmentOutlined /> {{ company.city }}
              </div>
              <p class="company-desc">{{ company.description || '暂无简介' }}</p>
            </a-card>
          </a-col>
        </a-row>
        <a-empty v-if="!loading && companyList.length === 0" description="暂无企业信息" style="padding: 60px 0" />
      </a-spin>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination
          v-model:current="pagination.page"
          :total="total"
          :page-size="pagination.size"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { SearchOutlined, EnvironmentOutlined } from '@ant-design/icons-vue'
import { getCompanyList } from '@/api/company'

const router = useRouter()

const loading = ref(false)
const keyword = ref('')
const companyList = ref([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 9 })

async function fetchCompanies() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      auditStatus: 1
    }
    if (keyword.value) params.keyword = keyword.value
    const res = await getCompanyList(params)
    companyList.value = res.data?.records || res.data?.list || res.data || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchCompanies()
}

function onPageChange(page) {
  pagination.page = page
  fetchCompanies()
}

function onSizeChange(_current, size) {
  pagination.page = 1
  pagination.size = size
  fetchCompanies()
}

onMounted(fetchCompanies)
</script>

<style scoped>
.company-list-page {
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

.company-card {
  border-radius: 8px;
  height: 100%;
  transition: all 0.3s;
}

.company-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.12);
}

.company-card-top {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 12px;
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
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.company-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.company-city {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.company-desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
  margin: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
</style>

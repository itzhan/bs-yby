<template>
  <div class="offer-list-page">
    <a-card title="Offer列表" :bordered="false" class="main-card">
      <a-spin :spinning="loading">
        <a-row :gutter="[16, 16]">
          <a-col v-for="offer in offerList" :key="offer.id" :xs="24" :sm="12" :md="12" :lg="8">
            <a-card class="offer-card">
              <template #title>
                <div class="offer-title">
                  <span>{{ offer.jobTitle }}</span>
                  <a-tag :color="statusMap[offer.status]?.color" size="small">
                    {{ statusMap[offer.status]?.text }}
                  </a-tag>
                </div>
              </template>
              <div class="offer-info">
                <div class="info-item">
                  <span class="label">公司：</span>
                  <span class="value">{{ offer.companyName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">薪资：</span>
                  <span class="value salary">{{ formatSalary(offer.salary) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">入职日期：</span>
                  <span class="value">{{ formatDate(offer.startDate) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">回复截止：</span>
                  <span class="value" :class="{ 'deadline-warning': isDeadlineNear(offer.deadline) }">
                    {{ formatDate(offer.deadline) }}
                  </span>
                </div>
                <div class="info-item" v-if="offer.description">
                  <span class="label">Offer说明：</span>
                  <span class="value description">{{ offer.description }}</span>
                </div>
              </div>
              <div class="offer-actions" v-if="offer.status === 0">
                <a-button type="primary" @click="handleAccept(offer)">接受</a-button>
                <a-button danger @click="handleReject(offer)">拒绝</a-button>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-empty v-if="!loading && offerList.length === 0" description="暂无Offer" />

        <div class="pagination-wrapper" v-if="offerList.length > 0">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getMyOffers, respondOffer } from '@/api/offer'
import dayjs from 'dayjs'

const loading = ref(false)
const offerList = ref([])
const pagination = reactive({ current: 1, pageSize: 9, total: 0 })

const statusMap = {
  0: { text: '待回复', color: 'orange' },
  1: { text: '已接受', color: 'green' },
  2: { text: '已拒绝', color: 'red' },
  3: { text: '已过期', color: 'default' }
}

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD') : '-'
}

function formatSalary(salary) {
  if (!salary) return '-'
  return typeof salary === 'number' ? `${salary}元/月` : salary
}

function isDeadlineNear(deadline) {
  if (!deadline) return false
  const days = dayjs(deadline).diff(dayjs(), 'day')
  return days >= 0 && days <= 3
}

async function fetchOffers() {
  loading.value = true
  try {
    const res = await getMyOffers({
      page: pagination.current,
      size: pagination.pageSize
    })
    const data = res.data || {}
    offerList.value = data.records || data.list || data.content || []
    pagination.total = data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handlePageChange() {
  fetchOffers()
}

function handleAccept(offer) {
  Modal.confirm({
    title: '确认接受',
    content: '确定要接受该Offer吗？',
    onOk: async () => {
      try {
        await respondOffer(offer.id, { status: 1 })
        message.success('已接受Offer')
        fetchOffers()
      } catch {
        // handled by interceptor
      }
    }
  })
}

function handleReject(offer) {
  Modal.confirm({
    title: '确认拒绝',
    content: '确定要拒绝该Offer吗？',
    okType: 'danger',
    onOk: async () => {
      try {
        await respondOffer(offer.id, { status: 2 })
        message.success('已拒绝Offer')
        fetchOffers()
      } catch {
        // handled by interceptor
      }
    }
  })
}

onMounted(fetchOffers)
</script>

<style scoped>
.offer-list-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.offer-card {
  height: 100%;
  transition: all 0.3s;
  border: 1px solid #e8e8e8;
}
.offer-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.offer-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.offer-info {
  margin-top: 16px;
}
.info-item {
  margin-bottom: 8px;
  display: flex;
  align-items: flex-start;
}
.label {
  color: #666;
  min-width: 90px;
  flex-shrink: 0;
}
.value {
  color: #333;
  flex: 1;
}
.value.salary {
  color: #1890ff;
  font-weight: 600;
  font-size: 16px;
}
.value.deadline-warning {
  color: #ff4d4f;
  font-weight: 500;
}
.value.description {
  white-space: pre-wrap;
  word-break: break-word;
}
.offer-actions {
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

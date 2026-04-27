<template>
  <div class="jobfair-list-page">
    <div class="page-container">
      <div class="page-header">
        <h1 class="page-title">校园宣讲会</h1>
        <p class="page-desc">了解最新宣讲会信息，提前预约心仪企业</p>
      </div>

      <a-spin :spinning="loading">
        <div class="jobfair-list">
          <a-card v-for="item in jobfairList" :key="item.id" class="jobfair-card">
            <div class="jobfair-content">
              <div class="jobfair-left">
                <div class="jobfair-date-badge">
                  <div class="date-day">{{ getDay(item.startTime) }}</div>
                  <div class="date-month">{{ getMonth(item.startTime) }}</div>
                </div>
              </div>
              <div class="jobfair-main">
                <h3 class="jobfair-title">{{ item.title }}</h3>
                <div class="jobfair-meta">
                  <span class="meta-item"><BankOutlined /> {{ item.companyName || '企业' }}</span>
                  <span class="meta-item"><EnvironmentOutlined /> {{ item.location || '待定' }}</span>
                  <span class="meta-item">
                    <ClockCircleOutlined />
                    {{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}
                  </span>
                </div>
                <div class="jobfair-capacity" v-if="item.maxCapacity">
                  <span>容量：{{ item.currentCount || 0 }} / {{ item.maxCapacity }}</span>
                  <a-progress
                    :percent="Math.round(((item.currentCount || 0) / item.maxCapacity) * 100)"
                    :stroke-color="getProgressColor(item)"
                    :show-info="false"
                    size="small"
                    style="width: 120px; margin-left: 12px"
                  />
                </div>
              </div>
              <div class="jobfair-right">
                <a-tag :color="getStatusColor(item.status)" class="status-tag">
                  {{ getStatusText(item.status) }}
                </a-tag>
                <!-- 已预约 -->
                <template v-if="item.booked">
                  <a-tag color="success" style="font-size: 13px; margin: 0">✓ 已预约</a-tag>
                  <a-button size="small" danger :loading="item._booking" @click="handleCancel(item)">取消预约</a-button>
                </template>
                <!-- 可预约 -->
                <template v-else-if="item.status === 1 && (!item.maxCapacity || (item.currentCount || 0) < item.maxCapacity)">
                  <a-button type="primary" :loading="item._booking" @click="handleBook(item)">预约</a-button>
                </template>
                <!-- 不可预约 -->
                <template v-else>
                  <a-button disabled>{{ item.status === 3 ? '已结束' : item.status === 4 ? '已取消' : '已满' }}</a-button>
                </template>
              </div>
            </div>
          </a-card>
        </div>
        <a-empty v-if="!loading && jobfairList.length === 0" description="暂无宣讲会" style="padding: 60px 0" />
      </a-spin>

      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination v-model:current="pagination.page" :total="total" :page-size="pagination.size" @change="onPageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { BankOutlined, EnvironmentOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import { getJobFairList, bookJobFair, cancelBooking, getMyBookedFairIds } from '@/api/jobfair'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const jobfairList = ref([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })

/** 格式化时间：2026-03-10T14:00:00 -> 2026-03-10 14:00 */
function formatTime (str) {
  if (!str) return '--'
  return str.replace('T', ' ').substring(0, 16)
}

function getDay (dateStr) {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  return isNaN(d.getTime()) ? '--' : d.getDate()
}

function getMonth (dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return isNaN(d.getTime()) ? '' : `${d.getMonth() + 1}月`
}

function getStatusColor (status) {
  return { 0: 'default', 1: 'processing', 2: 'error', 3: 'success', 4: 'default' }[status] || 'default'
}

function getStatusText (status) {
  return { 0: '待审核', 1: '报名中', 2: '已拒绝', 3: '已结束', 4: '已取消' }[status] || '未知'
}

function getProgressColor (item) {
  const pct = ((item.currentCount || 0) / (item.maxCapacity || 1)) * 100
  if (pct >= 90) return '#ff4d4f'
  if (pct >= 60) return '#faad14'
  return '#1890ff'
}

async function handleBook (item) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/jobfairs' } })
    return
  }
  if (userStore.role !== 'STUDENT') {
    message.warning('仅学生用户可预约宣讲会')
    return
  }
  item._booking = true
  try {
    await bookJobFair(item.id)
    message.success('预约成功！')
    item.booked = true
    if (item.currentCount !== undefined) item.currentCount++
  } catch {
    // 错误已由拦截器处理，如果是"已预约"错误则标记
    item.booked = true
  } finally {
    item._booking = false
  }
}

async function handleCancel (item) {
  item._booking = true
  try {
    await cancelBooking(item.id)
    message.success('已取消预约')
    item.booked = false
    if (item.currentCount > 0) item.currentCount--
  } catch {
    // handled
  } finally {
    item._booking = false
  }
}

async function fetchJobFairs () {
  loading.value = true
  try {
    const res = await getJobFairList({ page: pagination.page, size: pagination.size, status: 1 })
    const list = (res.data?.records || []).map(item => ({ ...item, _booking: false, booked: false }))

    // 如果是已登录学生，查询已预约的宣讲会ID列表
    if (userStore.isLoggedIn && userStore.role === 'STUDENT') {
      try {
        const bookRes = await getMyBookedFairIds()
        const bookedIds = new Set(bookRes.data || [])
        list.forEach(item => {
          if (bookedIds.has(item.id)) item.booked = true
        })
      } catch {
        // 查询失败不影响列表展示
      }
    }

    jobfairList.value = list
    total.value = res.data?.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function onPageChange (page) {
  pagination.page = page
  fetchJobFairs()
}

onMounted(fetchJobFairs)
</script>

<style scoped>
.jobfair-list-page { padding: 24px 0; }
.page-container { max-width: 1200px; margin: 0 auto; padding: 0 24px; }
.page-header { text-align: center; margin-bottom: 32px; }
.page-title { font-size: 28px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px; }
.page-desc { font-size: 15px; color: #999; margin: 0; }
.jobfair-list { display: flex; flex-direction: column; gap: 16px; }
.jobfair-card { border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.06); transition: all .3s; }
.jobfair-card:hover { box-shadow: 0 4px 16px rgba(24,144,255,.12); transform: translateY(-2px); }
.jobfair-content { display: flex; gap: 20px; align-items: center; }
.jobfair-left { flex-shrink: 0; }
.jobfair-date-badge { width: 64px; height: 64px; background: linear-gradient(135deg, #1890ff, #096dd9); border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; }
.date-day { font-size: 24px; font-weight: 700; line-height: 1.1; }
.date-month { font-size: 12px; opacity: .85; }
.jobfair-main { flex: 1; min-width: 0; }
.jobfair-title { font-size: 17px; font-weight: 600; color: #1a1a1a; margin: 0 0 8px; }
.jobfair-meta { display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 6px; }
.meta-item { font-size: 13px; color: #666; display: flex; align-items: center; gap: 4px; }
.jobfair-capacity { font-size: 13px; color: #999; display: flex; align-items: center; margin-top: 4px; }
.jobfair-right { display: flex; flex-direction: column; align-items: center; gap: 10px; flex-shrink: 0; }
.status-tag { font-size: 13px; }
.pagination-wrap { display: flex; justify-content: center; padding: 32px 0; }
@media (max-width: 576px) { .jobfair-content { flex-direction: column; align-items: flex-start; } .jobfair-right { flex-direction: row; width: 100%; justify-content: space-between; } }
</style>

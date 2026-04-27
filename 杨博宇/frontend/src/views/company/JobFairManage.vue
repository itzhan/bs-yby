<template>
  <div class="jobfair-manage-page">
    <a-card title="宣讲会管理" :bordered="false" class="main-card">
      <template #extra>
        <a-button type="primary" @click="openCreateModal">
          <PlusOutlined /> 发布宣讲会
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="jobFairList"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'time'">
              <div>{{ formatDateTime(record.startTime) }}</div>
              <div style="color: #999; font-size: 12px">至 {{ formatDateTime(record.endTime) }}</div>
            </template>
            <template v-if="column.dataIndex === 'capacity'">
              {{ record.currentCount || 0 }} / {{ record.maxCapacity || '-' }}
            </template>
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="getStatusInfo(record).color">
                {{ getStatusInfo(record).text }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- Create Modal -->
    <a-modal
      v-model:open="modalVisible"
      title="发布宣讲会"
      :confirm-loading="modalLoading"
      width="640px"
      @ok="handleSubmit"
      @cancel="resetModal"
    >
      <a-form :model="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="宣讲会标题">
          <a-input v-model:value="form.title" placeholder="请输入宣讲会标题" />
        </a-form-item>
        <a-form-item label="宣讲会简介">
          <a-textarea v-model:value="form.description" :rows="4" placeholder="请输入宣讲会简介" />
        </a-form-item>
        <a-form-item label="举办地点">
          <a-input v-model:value="form.location" placeholder="请输入举办地点" />
        </a-form-item>
        <a-form-item label="开始时间">
          <a-date-picker
            v-model:value="form.startTime"
            show-time
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="结束时间">
          <a-date-picker
            v-model:value="form.endTime"
            show-time
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="最大容量">
          <a-input-number v-model:value="form.maxCapacity" :min="1" placeholder="最大参与人数" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getMyJobFairs, createJobFair } from '@/api/jobfair'
import dayjs from 'dayjs'

const loading = ref(false)
const jobFairList = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: '宣讲会标题', dataIndex: 'title', ellipsis: true },
  { title: '地点', dataIndex: 'location', ellipsis: true, width: 160 },
  { title: '时间', dataIndex: 'time', width: 180 },
  { title: '报名 / 容量', dataIndex: 'capacity', width: 120 },
  { title: '状态', dataIndex: 'status', width: 100 }
]

// Modal state
const modalVisible = ref(false)
const modalLoading = ref(false)
const form = reactive({
  title: '',
  description: '',
  location: '',
  startTime: null,
  endTime: null,
  maxCapacity: null
})

function formatDateTime(val) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
}

function getStatusInfo(record) {
  const now = dayjs()
  const start = record.startTime ? dayjs(record.startTime) : null
  const end = record.endTime ? dayjs(record.endTime) : null
  if (record.status === 0 || record.status === '待审核') return { text: '待审核', color: 'orange' }
  if (end && now.isAfter(end)) return { text: '已结束', color: 'default' }
  if (start && now.isAfter(start)) return { text: '进行中', color: 'green' }
  return { text: '未开始', color: 'blue' }
}

function resetModal() {
  Object.assign(form, {
    title: '',
    description: '',
    location: '',
    startTime: null,
    endTime: null,
    maxCapacity: null
  })
  modalVisible.value = false
}

function openCreateModal() {
  resetModal()
  modalVisible.value = true
}

async function fetchJobFairs() {
  loading.value = true
  try {
    const res = await getMyJobFairs({
      page: pagination.current,
      size: pagination.pageSize
    })
    const data = res.data || {}
    jobFairList.value = data.records || data.list || data.content || []
    pagination.total = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchJobFairs()
}

async function handleSubmit() {
  if (!form.title) {
    message.warning('请输入宣讲会标题')
    return
  }
  if (!form.startTime || !form.endTime) {
    message.warning('请选择开始和结束时间')
    return
  }
  modalLoading.value = true
  try {
    await createJobFair({
      title: form.title,
      description: form.description,
      location: form.location,
      startTime: dayjs(form.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(form.endTime).format('YYYY-MM-DD HH:mm:ss'),
      maxCapacity: form.maxCapacity
    })
    message.success('宣讲会发布成功')
    resetModal()
    fetchJobFairs()
  } catch {
    // handled
  } finally {
    modalLoading.value = false
  }
}

onMounted(fetchJobFairs)
</script>

<style scoped>
.jobfair-manage-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

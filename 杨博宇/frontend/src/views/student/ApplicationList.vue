<template>
  <div class="application-list-page">
    <a-card title="投递记录" :bordered="false" class="main-card">
      <template #extra>
        <a-select
          v-model:value="statusFilter"
          placeholder="筛选状态"
          allow-clear
          style="width: 140px"
          @change="fetchApplications"
        >
          <a-select-option :value="0">待查看</a-select-option>
          <a-select-option :value="1">已查看</a-select-option>
          <a-select-option :value="2">面试中</a-select-option>
          <a-select-option :value="3">已录用</a-select-option>
          <a-select-option :value="4">已拒绝</a-select-option>
          <a-select-option :value="5">已撤回</a-select-option>
        </a-select>
      </template>

      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="applicationList"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="statusMap[record.status]?.color">
                {{ statusMap[record.status]?.text }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'createdAt'">
              {{ formatDate(record.createdAt) }}
            </template>
            <template v-if="column.dataIndex === 'action'">
              <a-button
                v-if="record.status === 0"
                type="link"
                danger
                size="small"
                @click="handleWithdraw(record)"
              >
                撤回
              </a-button>
              <span v-else style="color: #999">-</span>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getMyApplications, withdraw } from '@/api/application'
import dayjs from 'dayjs'

const loading = ref(false)
const statusFilter = ref(undefined)
const applicationList = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const statusMap = {
  0: { text: '待查看', color: 'blue' },
  1: { text: '已查看', color: 'cyan' },
  2: { text: '面试中', color: 'orange' },
  3: { text: '已录用', color: 'green' },
  4: { text: '已拒绝', color: 'red' },
  5: { text: '已撤回', color: 'default' }
}

const columns = [
  { title: '岗位名称', dataIndex: 'jobTitle', ellipsis: true },
  { title: '公司名称', dataIndex: 'companyName', ellipsis: true },
  { title: '投递时间', dataIndex: 'createdAt', width: 180 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', width: 100 }
]

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchApplications() {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    }
    if (statusFilter.value !== undefined && statusFilter.value !== null) {
      params.status = statusFilter.value
    }
    const res = await getMyApplications(params)
    const data = res.data || {}
    applicationList.value = data.records || data.list || data.content || []
    pagination.total = data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchApplications()
}

function handleWithdraw(record) {
  Modal.confirm({
    title: '确认撤回',
    content: '确定要撤回该投递吗？撤回后无法恢复。',
    okType: 'danger',
    onOk: async () => {
      try {
        await withdraw(record.id)
        message.success('撤回成功')
        fetchApplications()
      } catch {
        // handled by interceptor
      }
    }
  })
}

onMounted(fetchApplications)
</script>

<style scoped>
.application-list-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

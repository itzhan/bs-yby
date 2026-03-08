<template>
  <div class="interview-manage-page">
    <a-card title="面试管理" :bordered="false" class="main-card">
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="interviewList"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'interviewTime'">
              {{ formatDate(record.interviewTime) }}
            </template>
            <template v-if="column.dataIndex === 'interviewType'">
              <a-tag :color="record.interviewType === '线上' ? 'blue' : 'green'">
                {{ record.interviewType || '-' }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="statusMap[record.status]?.color">
                {{ statusMap[record.status]?.text }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'action'">
              <a-space>
                <a-button
                  v-if="record.status === 1"
                  type="link"
                  size="small"
                  @click="markCompleted(record)"
                >标记已完成</a-button>
                <span v-else style="color: #999; font-size: 12px">—</span>
              </a-space>
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
import { getCompanyInterviews, updateInterviewStatus } from '@/api/interview'
import dayjs from 'dayjs'

const loading = ref(false)
const interviewList = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const statusMap = {
  0: { text: '待确认', color: 'blue' },
  1: { text: '已接受', color: 'green' },
  2: { text: '已拒绝', color: 'red' },
  3: { text: '已完成', color: 'default' }
}

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', width: 100 },
  { title: '应聘岗位', dataIndex: 'jobTitle', ellipsis: true },
  { title: '面试时间', dataIndex: 'interviewTime', width: 160 },
  { title: '面试地点', dataIndex: 'location', ellipsis: true },
  { title: '面试方式', dataIndex: 'interviewType', width: 100 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '操作', dataIndex: 'action', width: 120, fixed: 'right' }
]

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchInterviews() {
  loading.value = true
  try {
    const res = await getCompanyInterviews({
      page: pagination.current,
      size: pagination.pageSize
    })
    const data = res.data || {}
    interviewList.value = data.records || data.list || data.content || []
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
  fetchInterviews()
}

function markCompleted(record) {
  Modal.confirm({
    title: '确认完成',
    content: '确认将该面试标记为已完成？',
    onOk: async () => {
      try {
        await updateInterviewStatus(record.id, { status: 3 })
        message.success('面试已标记为完成')
        fetchInterviews()
      } catch {
        // handled
      }
    }
  })
}

onMounted(fetchInterviews)
</script>

<style scoped>
.interview-manage-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

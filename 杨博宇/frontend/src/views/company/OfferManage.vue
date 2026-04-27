<template>
  <div class="offer-manage-page">
    <a-card title="Offer管理" :bordered="false" class="main-card">
      <template #extra>
        <a-button type="primary" @click="openCreateModal">
          <PlusOutlined /> 发放Offer
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="offerList"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'startDate'">
              {{ formatDate(record.startDate) }}
            </template>
            <template v-if="column.dataIndex === 'deadline'">
              {{ formatDate(record.deadline) }}
            </template>
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="offerStatusMap[record.status]?.color">
                {{ offerStatusMap[record.status]?.text }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- Create Offer Modal -->
    <a-modal
      v-model:open="modalVisible"
      title="发放Offer"
      :confirm-loading="modalLoading"
      width="600px"
      @ok="handleSubmit"
      @cancel="resetModal"
    >
      <a-form :model="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="选择投递">
          <a-select
            v-model:value="form.applicationId"
            placeholder="请选择面试中的投递记录"
            show-search
            :filter-option="filterOption"
            style="width: 100%"
          >
            <a-select-option
              v-for="app in interviewApplications"
              :key="app.id"
              :value="app.id"
            >
              {{ app.studentName }} - {{ app.jobTitle }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="薪资 (元/月)">
          <a-input-number v-model:value="form.salary" :min="0" placeholder="请输入月薪" style="width: 100%" />
        </a-form-item>
        <a-form-item label="入职日期">
          <a-date-picker v-model:value="form.startDate" style="width: 100%" placeholder="请选择入职日期" />
        </a-form-item>
        <a-form-item label="Offer截止">
          <a-date-picker v-model:value="form.deadline" style="width: 100%" placeholder="请选择回复截止日期" />
        </a-form-item>
        <a-form-item label="补充说明">
          <a-textarea v-model:value="form.description" :rows="3" placeholder="Offer补充说明（选填）" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getCompanyOffers, createOffer } from '@/api/offer'
import { getJobApplications } from '@/api/application'
import { getJobList } from '@/api/job'
import { getCurrentCompany } from '@/api/company'
import dayjs from 'dayjs'

const loading = ref(false)
const companyId = ref(null)
const offerList = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const interviewApplications = ref([])

const offerStatusMap = {
  0: { text: '待回复', color: 'blue' },
  1: { text: '已接受', color: 'green' },
  2: { text: '已拒绝', color: 'red' },
  3: { text: '已过期', color: 'default' }
}

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', width: 100 },
  { title: '应聘岗位', dataIndex: 'jobTitle', ellipsis: true },
  { title: '薪资 (元/月)', dataIndex: 'salary', width: 120 },
  { title: '入职日期', dataIndex: 'startDate', width: 120 },
  { title: 'Offer截止', dataIndex: 'deadline', width: 120 },
  { title: '状态', dataIndex: 'status', width: 90 }
]

// Modal state
const modalVisible = ref(false)
const modalLoading = ref(false)
const form = reactive({
  applicationId: undefined,
  salary: null,
  startDate: null,
  deadline: null,
  description: ''
})

function filterOption(input, option) {
  return option.children?.[0]?.children?.toLowerCase().includes(input.toLowerCase())
}

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD') : '-'
}

function resetModal() {
  Object.assign(form, {
    applicationId: undefined,
    salary: null,
    startDate: null,
    deadline: null,
    description: ''
  })
  modalVisible.value = false
}

async function openCreateModal() {
  resetModal()
  await fetchInterviewApplications()
  modalVisible.value = true
}

async function fetchOffers() {
  loading.value = true
  try {
    const res = await getCompanyOffers({
      page: pagination.current,
      size: pagination.pageSize
    })
    const data = res.data || {}
    offerList.value = data.records || data.list || data.content || []
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
  fetchOffers()
}

async function fetchInterviewApplications() {
  try {
    // Fetch all company jobs, then get applications in interview status (status=2)
    const jobRes = await getJobList({ companyId: companyId.value, size: 100 })
    const jobs = jobRes.data?.records || jobRes.data?.list || jobRes.data?.content || []
    const allApps = []
    for (const job of jobs) {
      try {
        const appRes = await getJobApplications(job.id, { status: 2, size: 100 })
        const apps = appRes.data?.records || appRes.data?.list || appRes.data?.content || []
        apps.forEach((app) => {
          allApps.push({
            ...app,
            jobTitle: job.title
          })
        })
      } catch {
        // skip failed jobs
      }
    }
    interviewApplications.value = allApps
  } catch {
    // handled
  }
}

async function handleSubmit() {
  if (!form.applicationId) {
    message.warning('请选择投递记录')
    return
  }
  modalLoading.value = true
  try {
    await createOffer({
      applicationId: form.applicationId,
      salary: form.salary,
      startDate: form.startDate ? dayjs(form.startDate).format('YYYY-MM-DD') : null,
      deadline: form.deadline ? dayjs(form.deadline).format('YYYY-MM-DD') : null,
      description: form.description
    })
    message.success('Offer发放成功')
    resetModal()
    fetchOffers()
  } catch {
    // handled
  } finally {
    modalLoading.value = false
  }
}

async function init() {
  try {
    const res = await getCurrentCompany()
    companyId.value = res.data?.id
  } catch {
    // handled
  }
  fetchOffers()
}

onMounted(init)
</script>

<style scoped>
.offer-manage-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

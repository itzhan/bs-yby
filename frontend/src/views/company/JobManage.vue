<template>
  <div class="job-manage-page">
    <a-card title="岗位管理" :bordered="false" class="main-card">
      <template #extra>
        <a-button type="primary" @click="openCreateModal">
          <PlusOutlined /> 发布新岗位
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="jobList"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'salary'">
              {{ record.salaryMin }}K - {{ record.salaryMax }}K
            </template>
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="statusMap[record.status]?.color">
                {{ statusMap[record.status]?.text }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'deadline'">
              {{ formatDate(record.deadline) }}
            </template>
            <template v-if="column.dataIndex === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
                <a-button
                  v-if="record.status !== 3"
                  type="link"
                  size="small"
                  @click="handleClose(record)"
                >关闭</a-button>
                <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- Create / Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑岗位' : '发布新岗位'"
      :confirm-loading="modalLoading"
      width="720px"
      @ok="handleModalOk"
      @cancel="resetModal"
    >
      <a-form :model="modalForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="岗位名称" :rules="[{ required: true, message: '请输入岗位名称' }]">
          <a-input v-model:value="modalForm.title" placeholder="请输入岗位名称" />
        </a-form-item>
        <a-form-item label="岗位类别">
          <a-select v-model:value="modalForm.category" placeholder="请选择类别">
            <a-select-option v-for="c in categoryOptions" :key="c" :value="c">{{ c }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="工作类型">
          <a-select v-model:value="modalForm.jobType" placeholder="请选择工作类型">
            <a-select-option value="全职">全职</a-select-option>
            <a-select-option value="实习">实习</a-select-option>
            <a-select-option value="兼职">兼职</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="薪资范围 (K)">
          <a-row :gutter="8">
            <a-col :span="11">
              <a-input-number v-model:value="modalForm.salaryMin" :min="0" placeholder="最低" style="width: 100%" />
            </a-col>
            <a-col :span="2" style="text-align: center; line-height: 32px">-</a-col>
            <a-col :span="11">
              <a-input-number v-model:value="modalForm.salaryMax" :min="0" placeholder="最高" style="width: 100%" />
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item label="工作城市">
          <a-input v-model:value="modalForm.city" placeholder="请输入工作城市" />
        </a-form-item>
        <a-form-item label="工作地址">
          <a-input v-model:value="modalForm.address" placeholder="请输入工作地址" />
        </a-form-item>
        <a-form-item label="学历要求">
          <a-select v-model:value="modalForm.educationReq" placeholder="请选择学历要求">
            <a-select-option value="不限">不限</a-select-option>
            <a-select-option value="大专">大专</a-select-option>
            <a-select-option value="本科">本科</a-select-option>
            <a-select-option value="硕士">硕士</a-select-option>
            <a-select-option value="博士">博士</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="专业要求">
          <a-input v-model:value="modalForm.majorReq" placeholder="请输入专业要求（选填）" />
        </a-form-item>
        <a-form-item label="招聘人数">
          <a-input-number v-model:value="modalForm.headcount" :min="1" placeholder="人数" style="width: 100%" />
        </a-form-item>
        <a-form-item label="截止日期">
          <a-date-picker v-model:value="modalForm.deadline" style="width: 100%" placeholder="请选择截止日期" />
        </a-form-item>
        <a-form-item label="岗位描述">
          <a-textarea v-model:value="modalForm.description" :rows="4" placeholder="请输入岗位描述" />
        </a-form-item>
        <a-form-item label="岗位要求">
          <a-textarea v-model:value="modalForm.requirements" :rows="4" placeholder="请输入岗位要求" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getJobList, createJob, updateJob, closeJob, deleteJob } from '@/api/job'
import { getCurrentCompany } from '@/api/company'
import dayjs from 'dayjs'

const loading = ref(false)
const companyId = ref(null)
const jobList = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const categoryOptions = ['技术', '产品', '运营', '市场', '人事', '财务', '其他']

const statusMap = {
  0: { text: '草稿', color: 'default' },
  1: { text: '待审核', color: 'orange' },
  2: { text: '已发布', color: 'green' },
  3: { text: '已关闭', color: 'gray' },
  4: { text: '已拒绝', color: 'red' }
}

const columns = [
  { title: '岗位名称', dataIndex: 'title', ellipsis: true },
  { title: '类别', dataIndex: 'category', width: 80 },
  { title: '城市', dataIndex: 'city', width: 80 },
  { title: '薪资 (K)', dataIndex: 'salary', width: 120 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '截止日期', dataIndex: 'deadline', width: 110 },
  { title: '操作', dataIndex: 'action', width: 180, fixed: 'right' }
]

// Modal state
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const defaultForm = () => ({
  title: '',
  category: undefined,
  jobType: undefined,
  salaryMin: null,
  salaryMax: null,
  city: '',
  address: '',
  educationReq: undefined,
  majorReq: '',
  headcount: null,
  deadline: null,
  description: '',
  requirements: ''
})

const modalForm = reactive(defaultForm())

function resetModal() {
  Object.assign(modalForm, defaultForm())
  isEdit.value = false
  editId.value = null
  modalVisible.value = false
}

function openCreateModal() {
  resetModal()
  modalVisible.value = true
}

function openEditModal(record) {
  isEdit.value = true
  editId.value = record.id
  Object.assign(modalForm, {
    title: record.title,
    category: record.category,
    jobType: record.jobType,
    salaryMin: record.salaryMin,
    salaryMax: record.salaryMax,
    city: record.city,
    address: record.address,
    educationReq: record.educationReq,
    majorReq: record.majorReq,
    headcount: record.headcount,
    deadline: record.deadline ? dayjs(record.deadline) : null,
    description: record.description,
    requirements: record.requirements
  })
  modalVisible.value = true
}

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD') : '-'
}

async function fetchJobs() {
  loading.value = true
  try {
    const res = await getJobList({
      companyId: companyId.value,
      page: pagination.current,
      size: pagination.pageSize
    })
    const data = res.data || {}
    jobList.value = data.records || data.list || data.content || []
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
  fetchJobs()
}

async function handleModalOk() {
  if (!modalForm.title) {
    message.warning('请输入岗位名称')
    return
  }
  modalLoading.value = true
  try {
    const payload = {
      ...modalForm,
      deadline: modalForm.deadline ? dayjs(modalForm.deadline).format('YYYY-MM-DD') : null
    }
    if (isEdit.value) {
      await updateJob(editId.value, payload)
      message.success('岗位更新成功')
    } else {
      await createJob(payload)
      message.success('岗位发布成功')
    }
    resetModal()
    fetchJobs()
  } catch {
    // handled
  } finally {
    modalLoading.value = false
  }
}

function handleClose(record) {
  Modal.confirm({
    title: '确认关闭',
    content: `确定要关闭岗位「${record.title}」吗？关闭后将不再接收投递。`,
    onOk: async () => {
      try {
        await closeJob(record.id)
        message.success('岗位已关闭')
        fetchJobs()
      } catch {
        // handled
      }
    }
  })
}

function handleDelete(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除岗位「${record.title}」吗？此操作不可恢复。`,
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteJob(record.id)
        message.success('岗位已删除')
        fetchJobs()
      } catch {
        // handled
      }
    }
  })
}

async function init() {
  try {
    const res = await getCurrentCompany()
    companyId.value = res.data?.id
  } catch {
    // handled
  }
  fetchJobs()
}

onMounted(init)
</script>

<style scoped>
.job-manage-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

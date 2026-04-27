<template>
  <div class="application-manage-page">
    <a-card title="收到的投递" :bordered="false" class="main-card">
      <!-- Job selector -->
      <div class="filter-bar">
        <a-select
          v-model:value="selectedJobId"
          placeholder="请选择岗位查看投递"
          style="width: 320px"
          show-search
          :filter-option="filterOption"
          @change="handleJobChange"
        >
          <a-select-option v-for="job in jobOptions" :key="job.id" :value="job.id">
            {{ job.title }}
          </a-select-option>
        </a-select>
        <a-select
          v-model:value="statusFilter"
          placeholder="筛选状态"
          allow-clear
          style="width: 140px; margin-left: 12px"
          @change="fetchApplications"
        >
          <a-select-option :value="0">待查看</a-select-option>
          <a-select-option :value="1">已查看</a-select-option>
          <a-select-option :value="2">面试中</a-select-option>
          <a-select-option :value="3">已录用</a-select-option>
          <a-select-option :value="4">已拒绝</a-select-option>
        </a-select>
      </div>

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
            <template v-if="column.dataIndex === 'createTime'">
              {{ formatDate(record.createTime) }}
            </template>
            <template v-if="column.dataIndex === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="viewResume(record)">查看简历</a-button>
                <a-button
                  v-if="record.status === 0"
                  type="link"
                  size="small"
                  @click="markViewed(record)"
                >标记已查看</a-button>
                <a-button
                  v-if="record.status <= 1"
                  type="link"
                  size="small"
                  style="color: #722ed1"
                  @click="openInterviewModal(record)"
                >安排面试</a-button>
                <a-button
                  v-if="record.status === 2"
                  type="link"
                  size="small"
                  style="color: #52c41a"
                  @click="markHired(record)"
                >录用</a-button>
                <a-button
                  v-if="record.status < 4 && record.status !== 3"
                  type="link"
                  size="small"
                  danger
                  @click="markRejected(record)"
                >拒绝</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- Resume Preview Modal -->
    <a-modal
      v-model:open="resumeModalVisible"
      title="简历详情"
      :footer="null"
      width="640px"
    >
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="姓名">{{ resumeDetail.studentName }}</a-descriptions-item>
        <a-descriptions-item label="简历标题">{{ resumeDetail.resumeTitle }}</a-descriptions-item>
        <a-descriptions-item label="学校">{{ resumeDetail.school }}</a-descriptions-item>
        <a-descriptions-item label="专业">{{ resumeDetail.major }}</a-descriptions-item>
        <a-descriptions-item label="学历">{{ resumeDetail.education }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ resumeDetail.phone }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ resumeDetail.email }}</a-descriptions-item>
        <a-descriptions-item label="自我介绍">
          <div style="white-space: pre-wrap">{{ resumeDetail.introduction }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="技能特长">
          <div style="white-space: pre-wrap">{{ resumeDetail.skills }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="项目经历">
          <div v-if="parsedExperience.length > 0" class="experience-list">
            <div v-for="(proj, idx) in parsedExperience" :key="idx" class="experience-item">
              <div class="exp-header">
                <strong>{{ proj.name || '未命名项目' }}</strong>
                <span v-if="proj.role" class="exp-role">{{ proj.role }}</span>
              </div>
              <div v-if="proj.startDate || proj.endDate" class="exp-time">
                {{ proj.startDate || '?' }} ~ {{ proj.endDate || '至今' }}
              </div>
              <div v-if="proj.description" class="exp-desc">{{ proj.description }}</div>
            </div>
          </div>
          <div v-else style="white-space: pre-wrap">{{ resumeDetail.experience }}</div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Interview Arrangement Modal -->
    <a-modal
      v-model:open="interviewModalVisible"
      title="安排面试"
      :confirm-loading="interviewLoading"
      @ok="submitInterview"
    >
      <a-form :model="interviewForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="面试时间">
          <a-date-picker
            v-model:value="interviewForm.interviewTime"
            show-time
            placeholder="请选择面试时间"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="面试地点">
          <a-input v-model:value="interviewForm.location" placeholder="请输入面试地点或在线会议链接" />
        </a-form-item>
        <a-form-item label="面试方式">
          <a-select v-model:value="interviewForm.interviewType" placeholder="请选择面试方式">
            <a-select-option value="线上">线上</a-select-option>
            <a-select-option value="线下">线下</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="面试说明">
          <a-textarea v-model:value="interviewForm.description" :rows="3" placeholder="面试注意事项（选填）" />
        </a-form-item>
        <a-form-item label="联系方式">
          <a-input v-model:value="interviewForm.contact" placeholder="企业联系方式" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getJobList } from '@/api/job'
import { getJobApplications, updateApplicationStatus as updateAppStatusApi } from '@/api/application'
import { createInterview } from '@/api/interview'
import { getResumeById } from '@/api/resume'
import { getCurrentCompany } from '@/api/company'
import dayjs from 'dayjs'

const loading = ref(false)
const companyId = ref(null)
const selectedJobId = ref(undefined)
const statusFilter = ref(undefined)
const jobOptions = ref([])
const applicationList = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const statusMap = {
  0: { text: '待查看', color: 'blue' },
  1: { text: '已查看', color: 'orange' },
  2: { text: '面试中', color: 'purple' },
  3: { text: '已录用', color: 'green' },
  4: { text: '已拒绝', color: 'red' }
}

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', width: 100 },
  { title: '简历标题', dataIndex: 'resumeTitle', ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '投递时间', dataIndex: 'createTime', width: 160 },
  { title: '操作', dataIndex: 'action', width: 300, fixed: 'right' }
]

// Resume modal
const resumeModalVisible = ref(false)
const resumeDetail = reactive({
  studentName: '',
  resumeTitle: '',
  school: '',
  major: '',
  education: '',
  phone: '',
  email: '',
  introduction: '',
  skills: '',
  experience: ''
})

// 将 experience 字段解析为结构化数组（兼容 JSON 字符串和纯文本）
const parsedExperience = computed(() => {
  const raw = resumeDetail.experience
  if (!raw || raw === '-') return []
  try {
    const arr = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

// Interview modal
const interviewModalVisible = ref(false)
const interviewLoading = ref(false)
const currentApplication = ref(null)
const interviewForm = reactive({
  interviewTime: null,
  location: '',
  interviewType: undefined,
  description: '',
  contact: ''
})

function filterOption(input, option) {
  return option.children?.[0]?.children?.toLowerCase().includes(input.toLowerCase())
}

function formatDate(val) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchJobs() {
  try {
    const res = await getJobList({ companyId: companyId.value, size: 100 })
    const data = res.data || {}
    jobOptions.value = data.records || data.list || data.content || []
    if (jobOptions.value.length > 0 && !selectedJobId.value) {
      selectedJobId.value = jobOptions.value[0].id
      fetchApplications()
    }
  } catch {
    // handled
  }
}

function handleJobChange() {
  pagination.current = 1
  fetchApplications()
}

async function fetchApplications() {
  if (!selectedJobId.value) return
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    }
    if (statusFilter.value !== undefined && statusFilter.value !== null) {
      params.status = statusFilter.value
    }
    const res = await getJobApplications(selectedJobId.value, params)
    const data = res.data || {}
    applicationList.value = data.records || data.list || data.content || []
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
  fetchApplications()
}

async function viewResume(record) {
  try {
    const res = await getResumeById(record.resumeId)
    const data = res.data || {}
    Object.assign(resumeDetail, {
      studentName: record.studentName || data.studentName || '-',
      resumeTitle: data.title || data.resumeTitle || '-',
      school: data.school || '-',
      major: data.major || '-',
      education: data.education || '-',
      phone: data.phone || '-',
      email: data.email || '-',
      introduction: data.introduction || data.selfEvaluation || '-',
      skills: data.skills || '-',
      experience: data.experience || data.projectExperience || '-'
    })
    resumeModalVisible.value = true
  } catch {
    message.error('获取简历详情失败')
  }
}

async function handleUpdateStatus(record, status) {
  try {
    await updateAppStatusApi(record.id, { status })
    message.success('状态更新成功')
    fetchApplications()
  } catch {
    // handled
  }
}

function markViewed(record) {
  handleUpdateStatus(record, 1)
}

function markHired(record) {
  Modal.confirm({
    title: '确认录用',
    content: `确定录用该学生吗？`,
    onOk: () => handleUpdateStatus(record, 3)
  })
}

function markRejected(record) {
  Modal.confirm({
    title: '确认拒绝',
    content: '确定拒绝该投递吗？',
    okType: 'danger',
    onOk: () => handleUpdateStatus(record, 4)
  })
}

function openInterviewModal(record) {
  currentApplication.value = record
  Object.assign(interviewForm, {
    interviewTime: null,
    location: '',
    interviewType: undefined,
    description: '',
    contact: ''
  })
  interviewModalVisible.value = true
}

async function submitInterview() {
  if (!interviewForm.interviewTime) {
    message.warning('请选择面试时间')
    return
  }
  if (!interviewForm.location) {
    message.warning('请输入面试地点')
    return
  }
  interviewLoading.value = true
  try {
    await createInterview({
      applicationId: currentApplication.value.id,
      jobId: selectedJobId.value,
      studentId: currentApplication.value.studentId,
      interviewTime: dayjs(interviewForm.interviewTime).format('YYYY-MM-DD HH:mm:ss'),
      location: interviewForm.location,
      interviewType: interviewForm.interviewType,
      description: interviewForm.description,
      contact: interviewForm.contact
    })
    // Also update application status to 面试中
    await updateAppStatusApi(currentApplication.value.id, { status: 2 })
    message.success('面试安排成功')
    interviewModalVisible.value = false
    fetchApplications()
  } catch {
    // handled
  } finally {
    interviewLoading.value = false
  }
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
.application-manage-page {
  padding: 24px;
}
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.filter-bar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
/* 项目经历卡片样式 */
.experience-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.experience-item {
  padding: 10px 12px;
  background: #fafafa;
  border-left: 3px solid #1890ff;
  border-radius: 4px;
}
.exp-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.exp-role {
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  padding: 0 6px;
  border-radius: 3px;
}
.exp-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.exp-desc {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
}
</style>

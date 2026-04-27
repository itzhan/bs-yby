<template>
  <div class="resume-list-page">
    <a-card title="简历管理" :bordered="false" class="main-card">
      <template #extra>
        <a-button type="primary" @click="handleCreate">
          <template #icon><PlusOutlined /></template>
          新增简历
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <a-row :gutter="[16, 16]">
          <a-col v-for="resume in resumeList" :key="resume.id" :xs="24" :sm="12" :md="8">
            <a-card class="resume-card" :class="{ 'default-resume': resume.isDefault }">
              <template #title>
                <div class="resume-title">
                  <span>{{ resume.title }}</span>
                  <a-badge v-if="resume.isDefault" status="success" text="默认" />
                </div>
              </template>
              <template #extra>
                <a-tag :color="resume.status === 1 ? 'green' : 'default'">
                  {{ resume.status === 1 ? '公开' : '私密' }}
                </a-tag>
              </template>
              <p class="resume-info" v-if="resume.skills">
                <strong>技能：</strong>{{ resume.skills }}
              </p>
              <div class="resume-actions">
                <a-button type="link" @click="handleEdit(resume)">编辑</a-button>
                <a-button type="link" danger @click="handleDelete(resume)">删除</a-button>
              </div>
            </a-card>
          </a-col>
        </a-row>
        <a-empty v-if="!loading && resumeList.length === 0" description="暂无简历，点击新增创建" />
      </a-spin>
    </a-card>

    <!-- Resume Form Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="submitting"
      @ok="handleSubmit"
      width="800px"
      :body-style="{ maxHeight: '65vh', overflowY: 'auto' }"
    >
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="简历标题" required>
          <a-input v-model:value="form.title" placeholder="如：张明-Java开发简历" />
        </a-form-item>

        <!-- 教育经历（结构化） -->
        <a-divider orientation="left">教育经历</a-divider>
        <div v-for="(edu, i) in form.educationList" :key="'edu' + i" class="exp-block">
          <a-row :gutter="12">
            <a-col :span="8"><a-input v-model:value="edu.school" placeholder="学校" /></a-col>
            <a-col :span="6"><a-input v-model:value="edu.major" placeholder="专业" /></a-col>
            <a-col :span="4"><a-input v-model:value="edu.degree" placeholder="学历" /></a-col>
            <a-col :span="4">
              <a-input v-model:value="edu.startDate" placeholder="开始 如2022-09" />
            </a-col>
            <a-col :span="2">
              <a-button type="text" danger @click="form.educationList.splice(i, 1)">
                <DeleteOutlined />
              </a-button>
            </a-col>
          </a-row>
          <a-row :gutter="12" style="margin-top: 6px">
            <a-col :span="4"><a-input v-model:value="edu.endDate" placeholder="结束 如2026-06" /></a-col>
            <a-col :span="20"><a-input v-model:value="edu.description" placeholder="描述（GPA、荣誉等）" /></a-col>
          </a-row>
        </div>
        <a-button type="dashed" block @click="form.educationList.push({school:'',major:'',degree:'',startDate:'',endDate:'',description:''})">
          <PlusOutlined /> 添加教育经历
        </a-button>

        <!-- 工作/实习经历 -->
        <a-divider orientation="left">工作/实习经历</a-divider>
        <div v-for="(work, i) in form.workList" :key="'work' + i" class="exp-block">
          <a-row :gutter="12">
            <a-col :span="7"><a-input v-model:value="work.company" placeholder="公司名称" /></a-col>
            <a-col :span="6"><a-input v-model:value="work.position" placeholder="职位" /></a-col>
            <a-col :span="4"><a-input v-model:value="work.startDate" placeholder="开始时间" /></a-col>
            <a-col :span="4"><a-input v-model:value="work.endDate" placeholder="结束时间" /></a-col>
            <a-col :span="2">
              <a-button type="text" danger @click="form.workList.splice(i, 1)"><DeleteOutlined /></a-button>
            </a-col>
          </a-row>
          <a-input v-model:value="work.description" placeholder="工作描述" style="margin-top: 6px" />
        </div>
        <a-button type="dashed" block @click="form.workList.push({company:'',position:'',startDate:'',endDate:'',description:''})">
          <PlusOutlined /> 添加工作经历
        </a-button>

        <!-- 项目经历 -->
        <a-divider orientation="left">项目经历</a-divider>
        <div v-for="(proj, i) in form.projectList" :key="'proj' + i" class="exp-block">
          <a-row :gutter="12">
            <a-col :span="7"><a-input v-model:value="proj.name" placeholder="项目名称" /></a-col>
            <a-col :span="6"><a-input v-model:value="proj.role" placeholder="角色" /></a-col>
            <a-col :span="4"><a-input v-model:value="proj.startDate" placeholder="开始时间" /></a-col>
            <a-col :span="4"><a-input v-model:value="proj.endDate" placeholder="结束时间" /></a-col>
            <a-col :span="2">
              <a-button type="text" danger @click="form.projectList.splice(i, 1)"><DeleteOutlined /></a-button>
            </a-col>
          </a-row>
          <a-input v-model:value="proj.description" placeholder="项目描述" style="margin-top: 6px" />
        </div>
        <a-button type="dashed" block @click="form.projectList.push({name:'',role:'',startDate:'',endDate:'',description:''})">
          <PlusOutlined /> 添加项目经历
        </a-button>

        <a-divider orientation="left">其他信息</a-divider>
        <a-form-item label="技能特长">
          <a-textarea v-model:value="form.skills" placeholder="如：Java, Spring Boot, Vue.js, MySQL" :rows="2" />
        </a-form-item>
        <a-form-item label="获奖情况">
          <a-textarea v-model:value="form.awards" placeholder="如：校级一等奖学金" :rows="2" />
        </a-form-item>
        <a-form-item label="自我评价">
          <a-textarea v-model:value="form.selfEvaluation" placeholder="简要描述自己的优势" :rows="3" />
        </a-form-item>
        <a-form-item label="设为默认">
          <a-switch v-model:checked="form.isDefault" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="0">私密</a-radio>
            <a-radio :value="1">公开</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getMyResumes, createResume, updateResume, deleteResume } from '@/api/resume'

const loading = ref(false)
const submitting = ref(false)
const modalVisible = ref(false)
const editingId = ref(null)
const resumeList = ref([])
const modalTitle = computed(() => (editingId.value ? '编辑简历' : '新增简历'))

const form = reactive({
  title: '',
  educationList: [],
  workList: [],
  projectList: [],
  skills: '',
  awards: '',
  selfEvaluation: '',
  isDefault: false,
  status: 1
})

/** 安全解析 JSON 字符串为数组 */
function parseJsonArray (str) {
  if (!str) return []
  if (Array.isArray(str)) return str
  try { const arr = JSON.parse(str); return Array.isArray(arr) ? arr : [] } catch { return [] }
}

async function fetchResumes () {
  loading.value = true
  try {
    const res = await getMyResumes({})
    const data = res.data || {}
    resumeList.value = data.records || data.list || data.content || []
  } catch { /* handled */ } finally { loading.value = false }
}

function handleCreate () {
  editingId.value = null
  Object.assign(form, {
    title: '', educationList: [], workList: [], projectList: [],
    skills: '', awards: '', selfEvaluation: '', isDefault: false, status: 1
  })
  modalVisible.value = true
}

function handleEdit (resume) {
  editingId.value = resume.id
  Object.assign(form, {
    title: resume.title || '',
    educationList: parseJsonArray(resume.educationExperience),
    workList: parseJsonArray(resume.workExperience),
    projectList: parseJsonArray(resume.projectExperience),
    skills: resume.skills || '',
    awards: resume.awards || '',
    selfEvaluation: resume.selfEvaluation || '',
    isDefault: resume.isDefault || false,
    status: resume.status !== undefined ? resume.status : 1
  })
  modalVisible.value = true
}

function handleDelete (resume) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除简历"${resume.title}"吗？`,
    okType: 'danger',
    onOk: async () => {
      await deleteResume(resume.id)
      message.success('删除成功')
      fetchResumes()
    }
  })
}

async function handleSubmit () {
  if (!form.title) { message.warning('请输入简历标题'); return }
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      educationExperience: JSON.stringify(form.educationList.filter(e => e.school)),
      workExperience: JSON.stringify(form.workList.filter(e => e.company)),
      projectExperience: JSON.stringify(form.projectList.filter(e => e.name)),
      skills: form.skills,
      awards: form.awards,
      selfEvaluation: form.selfEvaluation,
      isDefault: form.isDefault,
      status: form.status
    }
    if (editingId.value) {
      await updateResume(editingId.value, payload)
      message.success('简历更新成功')
    } else {
      await createResume(payload)
      message.success('简历创建成功')
    }
    modalVisible.value = false
    fetchResumes()
  } catch { /* handled */ } finally { submitting.value = false }
}

onMounted(fetchResumes)
</script>

<style scoped>
.resume-list-page { padding: 24px; }
.main-card { border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
.resume-card { height: 100%; transition: all .3s; border: 1px solid #e8e8e8; }
.resume-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,.1); transform: translateY(-2px); }
.default-resume { border-color: #1890ff; background: linear-gradient(to bottom, #e6f7ff 0%, #fff 10%); }
.resume-title { display: flex; align-items: center; gap: 8px; }
.resume-info { font-size: 13px; color: #666; margin: 8px 0 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.resume-actions { margin-top: 16px; display: flex; gap: 8px; }
.exp-block { background: #fafafa; border-radius: 6px; padding: 10px 12px; margin-bottom: 10px; }
</style>

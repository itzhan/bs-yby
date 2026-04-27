<template>
  <div class="profile-page">
    <a-card title="个人档案编辑" :bordered="false" class="profile-card">
      <a-spin :spinning="loading">
        <a-form
          :model="form"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
          @finish="handleSubmit"
        >
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="真实姓名" name="realName" :rules="[{ required: true, message: '请输入真实姓名' }]">
                <a-input v-model:value="form.realName" placeholder="请输入真实姓名" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="性别" name="gender">
                <a-radio-group v-model:value="form.gender">
                  <a-radio value="男">男</a-radio>
                  <a-radio value="女">女</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="出生日期" name="birthDate">
                <a-date-picker v-model:value="form.birthDate" placeholder="请选择出生日期" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="学校" name="school">
                <a-input v-model:value="form.school" placeholder="请输入学校" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="专业" name="major">
                <a-input v-model:value="form.major" placeholder="请输入专业" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="学历" name="education">
                <a-select v-model:value="form.education" placeholder="请选择学历">
                  <a-select-option value="专科">专科</a-select-option>
                  <a-select-option value="本科">本科</a-select-option>
                  <a-select-option value="硕士">硕士</a-select-option>
                  <a-select-option value="博士">博士</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="毕业年份" name="graduationYear">
                <a-input-number v-model:value="form.graduationYear" placeholder="请输入毕业年份" :min="2000" :max="2100" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="期望城市" name="expectedCity">
                <a-input v-model:value="form.expectedCity" placeholder="请输入期望工作城市" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="期望薪资（最低）" name="expectedSalaryMin">
                <a-input-number v-model:value="form.expectedSalaryMin" placeholder="最低薪资" :min="0" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="期望薪资（最高）" name="expectedSalaryMax">
                <a-input-number v-model:value="form.expectedSalaryMax" placeholder="最高薪资" :min="0" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="求职意向" name="jobIntention">
            <a-input v-model:value="form.jobIntention" placeholder="请输入求职意向" />
          </a-form-item>

          <a-form-item label="技能特长" name="skills">
            <a-textarea v-model:value="form.skills" placeholder="请输入技能特长，多个技能用逗号分隔" :rows="4" show-count :maxlength="500" />
          </a-form-item>

          <a-form-item label="自我介绍" name="selfIntroduction">
            <a-textarea v-model:value="form.selfIntroduction" placeholder="请输入自我介绍" :rows="6" show-count :maxlength="1000" />
          </a-form-item>

          <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
            <a-button type="primary" html-type="submit" :loading="submitting" size="large">
              保存修改
            </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getCurrentProfile, updateProfile } from '@/api/studentProfile'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  realName: '',
  gender: undefined,
  birthDate: null,
  school: '',
  major: '',
  education: undefined,
  graduationYear: undefined,
  skills: '',
  jobIntention: '',
  expectedSalaryMin: undefined,
  expectedSalaryMax: undefined,
  expectedCity: '',
  selfIntroduction: ''
})

async function fetchProfile() {
  loading.value = true
  try {
    const res = await getCurrentProfile()
    const data = res.data || {}
    Object.keys(form).forEach((key) => {
      if (data[key] !== undefined && data[key] !== null) {
        if (key === 'birthDate' && data[key]) {
          form[key] = dayjs(data[key])
        } else {
          form[key] = data[key]
        }
      }
    })
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    const submitData = { ...form }
    if (submitData.birthDate) {
      submitData.birthDate = dayjs(submitData.birthDate).format('YYYY-MM-DD')
    }
    await updateProfile(submitData)
    message.success('个人信息更新成功')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-page {
  padding: 24px;
}
.profile-card {
  max-width: 1000px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

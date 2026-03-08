<template>
  <div class="company-profile-page">
    <a-card title="企业信息编辑" :bordered="false" class="profile-card">
      <a-spin :spinning="loading">
        <a-form
          :model="form"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 16 }"
          @finish="handleSubmit"
        >
          <a-form-item label="企业名称" name="companyName" :rules="[{ required: true, message: '请输入企业名称' }]">
            <a-input v-model:value="form.companyName" placeholder="请输入企业名称" />
          </a-form-item>

          <a-form-item label="企业Logo" name="logo">
            <a-upload
              :max-count="1"
              :before-upload="handleBeforeUpload"
              :file-list="fileList"
              list-type="picture-card"
              @remove="handleRemoveLogo"
              :custom-request="handleUploadLogo"
            >
              <div v-if="fileList.length === 0">
                <PlusOutlined />
                <div style="margin-top: 8px">上传Logo</div>
              </div>
            </a-upload>
          </a-form-item>

          <a-form-item label="所属行业" name="industry">
            <a-select v-model:value="form.industry" placeholder="请选择行业">
              <a-select-option v-for="item in industryOptions" :key="item" :value="item">{{ item }}</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="企业规模" name="scale">
            <a-select v-model:value="form.scale" placeholder="请选择企业规模">
              <a-select-option v-for="item in scaleOptions" :key="item" :value="item">{{ item }}</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="所在城市" name="city">
            <a-input v-model:value="form.city" placeholder="请输入城市" />
          </a-form-item>

          <a-form-item label="详细地址" name="address">
            <a-input v-model:value="form.address" placeholder="请输入详细地址" />
          </a-form-item>

          <a-form-item label="企业简介" name="description">
            <a-textarea v-model:value="form.description" placeholder="请输入企业简介" :rows="5" show-count :maxlength="1000" />
          </a-form-item>

          <a-form-item label="企业官网" name="website">
            <a-input v-model:value="form.website" placeholder="请输入企业官网地址" addon-before="https://" />
          </a-form-item>

          <a-form-item label="联系人" name="contactPerson">
            <a-input v-model:value="form.contactPerson" placeholder="请输入联系人" />
          </a-form-item>

          <a-form-item label="联系电话" name="contactPhone">
            <a-input v-model:value="form.contactPhone" placeholder="请输入联系电话" />
          </a-form-item>

          <a-form-item label="联系邮箱" name="contactEmail">
            <a-input v-model:value="form.contactEmail" placeholder="请输入联系邮箱" />
          </a-form-item>

          <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
            <a-button type="primary" html-type="submit" :loading="submitting">保存修改</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getCurrentCompany, updateCompany } from '@/api/company'
import { uploadFile } from '@/api/file'

const loading = ref(false)
const submitting = ref(false)
const fileList = ref([])

const industryOptions = ['互联网', '金融', '教育', '制造', '医疗', '通信', '房地产', '其他']
const scaleOptions = ['0-50人', '50-200人', '200-500人', '500-1000人', '1000人以上']

const form = reactive({
  companyName: '',
  logo: '',
  industry: undefined,
  scale: undefined,
  city: '',
  address: '',
  description: '',
  website: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: ''
})

function handleBeforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB')
  }
  return isImage && isLt2M
}

async function handleUploadLogo({ file, onSuccess, onError }) {
  try {
    const res = await uploadFile(file)
    const url = res.data
    form.logo = url
    fileList.value = [
      {
        uid: '-1',
        name: file.name,
        status: 'done',
        url
      }
    ]
    onSuccess(res, file)
    message.success('Logo上传成功')
  } catch (err) {
    onError(err)
    message.error('Logo上传失败')
  }
}

function handleRemoveLogo() {
  form.logo = ''
  fileList.value = []
}

async function fetchCompany() {
  loading.value = true
  try {
    const res = await getCurrentCompany()
    const data = res.data || {}
    Object.keys(form).forEach((key) => {
      if (data[key] !== undefined && data[key] !== null) {
        form[key] = data[key]
      }
    })
    if (data.logo) {
      fileList.value = [
        {
          uid: '-1',
          name: 'logo',
          status: 'done',
          url: data.logo
        }
      ]
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    await updateCompany({ ...form })
    message.success('企业信息更新成功')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(fetchCompany)
</script>

<style scoped>
.company-profile-page {
  padding: 24px;
}
.profile-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

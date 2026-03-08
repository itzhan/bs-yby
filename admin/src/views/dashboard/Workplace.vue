<template>
  <page-header-wrapper>
    <!-- 统计卡片 -->
    <a-row :gutter="24">
      <a-col :sm="24" :md="6" v-for="(item, index) in statCards" :key="index">
        <a-card :loading="loading" :bordered="false" style="margin-bottom: 24px">
          <a-statistic
            :title="item.title"
            :value="item.value"
            :value-style="{ color: item.color }"
          >
            <template slot="prefix">
              <a-icon :type="item.icon" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近发布岗位 -->
    <a-card title="最近发布岗位" :bordered="false" style="margin-top: 24px">
      <a-table
        :columns="jobColumns"
        :data-source="recentJobs"
        :pagination="false"
        :loading="loading"
        row-key="id"
        size="middle"
      >
        <template slot="status" slot-scope="text">
          <a-tag :color="jobStatusMap[text] ? jobStatusMap[text].color : ''">
            {{ jobStatusMap[text] ? jobStatusMap[text].label : text }}
          </a-tag>
        </template>
      </a-table>
    </a-card>

    <a-row :gutter="24" style="margin-top: 24px">
      <!-- 投递状态分布 -->
      <a-col :sm="24" :md="12">
        <a-card title="投递状态分布" :bordered="false" :loading="loading">
          <div v-if="applicationStats && applicationStats.length">
            <a-tag
              v-for="item in applicationStats"
              :key="item.status"
              :color="applicationStatusMap[item.status] ? applicationStatusMap[item.status].color : ''"
              style="margin-bottom: 8px; padding: 4px 12px; font-size: 14px"
            >
              {{ applicationStatusMap[item.status] ? applicationStatusMap[item.status].label : item.status }}：{{ item.count }}
            </a-tag>
          </div>
          <a-empty v-else description="暂无数据" />
        </a-card>
      </a-col>

      <!-- 行业分布 -->
      <a-col :sm="24" :md="12">
        <a-card title="行业分布 TOP10" :bordered="false" :loading="loading">
          <div v-if="industryStats && industryStats.length">
            <div
              v-for="(item, index) in industryStats"
              :key="index"
              style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0"
            >
              <span>
                <a-badge :color="industryColors[index % industryColors.length]" />
                {{ item.name }}
              </span>
              <span style="font-weight: 500">{{ item.count }} 家</span>
            </div>
          </div>
          <a-empty v-else description="暂无数据" />
        </a-card>
      </a-col>
    </a-row>
  </page-header-wrapper>
</template>

<script>
import { PageHeaderWrapper } from '@ant-design-vue/pro-layout'
import { getDashboardData } from '@/api/dashboard'

export default {
  name: 'Workplace',
  components: {
    PageHeaderWrapper
  },
  data () {
    return {
      loading: true,
      studentCount: 0,
      companyCount: 0,
      jobCount: 0,
      applicationCount: 0,
      recentJobs: [],
      applicationStats: [],
      industryStats: [],
      jobStatusMap: {
        0: { label: '草稿', color: '' },
        1: { label: '待审核', color: 'orange' },
        2: { label: '已发布', color: 'green' },
        3: { label: '已关闭', color: '' },
        4: { label: '已拒绝', color: 'red' }
      },
      applicationStatusMap: {
        0: { label: '待查看', color: '' },
        1: { label: '已查看', color: 'blue' },
        2: { label: '面试中', color: 'orange' },
        3: { label: '已录用', color: 'green' },
        4: { label: '已拒绝', color: 'red' },
        5: { label: '已撤回', color: '' }
      },
      industryColors: ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16', '#a0d911', '#2f54eb'],
      jobColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '岗位名称', dataIndex: 'title', key: 'title', ellipsis: true },
        { title: '企业名称', dataIndex: 'companyName', key: 'companyName', ellipsis: true },
        { title: '城市', dataIndex: 'city', key: 'city', width: 100 },
        { title: '薪资范围', dataIndex: 'salaryRange', key: 'salaryRange', width: 120 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 100, scopedSlots: { customRender: 'status' } },
        { title: '发布时间', dataIndex: 'createTime', key: 'createTime', width: 170 }
      ]
    }
  },
  computed: {
    statCards () {
      return [
        { title: '学生总数', value: this.studentCount, icon: 'user', color: '#1890ff' },
        { title: '企业总数', value: this.companyCount, icon: 'bank', color: '#52c41a' },
        { title: '岗位总数', value: this.jobCount, icon: 'solution', color: '#faad14' },
        { title: '投递总数', value: this.applicationCount, icon: 'file-text', color: '#f5222d' }
      ]
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    loadData () {
      this.loading = true
      getDashboardData().then(res => {
        const data = res.data || {}
        this.studentCount = data.totalStudents || 0
        this.companyCount = data.totalCompanies || 0
        this.jobCount = data.totalJobs || 0
        this.applicationCount = data.totalApplications || 0
        this.recentJobs = (data.recentJobs || []).map(j => ({
          ...j,
          salaryRange: j.salaryMin && j.salaryMax ? `${j.salaryMin}-${j.salaryMax}` : '面议',
          createTime: j.createdAt ? j.createdAt.replace('T', ' ').substring(0, 16) : ''
        }))
        // applicationStatusStats 是 Map<String, Long>，转为数组
        const appStats = data.applicationStatusStats || {}
        this.applicationStats = Object.entries(appStats).map(([status, count]) => ({ status: Number(status), count }))
        // industryStats 是 Map<String, Long>，转为数组
        const indStats = data.industryStats || {}
        this.industryStats = Object.entries(indStats).map(([name, count]) => ({ name, count }))
      }).catch(() => {
        this.$message.error('加载统计数据失败')
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style lang="less" scoped>
</style>

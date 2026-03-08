<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="6" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="岗位名称/企业名称" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="24">
            <a-form-item label="状态">
              <a-select v-model="queryParam.status" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option :value="1">待审核</a-select-option>
                <a-select-option :value="2">已发布</a-select-option>
                <a-select-option :value="3">已关闭</a-select-option>
                <a-select-option :value="4">已拒绝</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="24">
            <a-form-item label="类别">
              <a-input v-model="queryParam.category" placeholder="岗位类别" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="24">
            <span class="table-page-search-submitButtons">
              <a-button type="primary" @click="handleSearch">查询</a-button>
              <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      :loading="loading"
      row-key="id"
      @change="handleTableChange"
    >
      <template slot="status" slot-scope="text">
        <a-tag :color="statusColorMap[text] || ''">{{ statusTextMap[text] || text }}</a-tag>
      </template>
      <template slot="action" slot-scope="text, record">
        <a @click="handleDetail(record)">详情</a>
        <template v-if="record.status === 1">
          <a-divider type="vertical" />
          <a style="color: #52c41a" @click="handleAudit(record, 1)">通过</a>
          <a-divider type="vertical" />
          <a style="color: #f5222d" @click="handleAudit(record, 2)">拒绝</a>
        </template>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除该岗位？" @confirm="handleDelete(record)">
          <a style="color: #f5222d">删除</a>
        </a-popconfirm>
      </template>
    </a-table>

    <!-- 岗位详情弹窗 -->
    <a-modal
      title="岗位详情"
      :visible="detailVisible"
      :footer="null"
      width="700px"
      @cancel="detailVisible = false"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions bordered :column="2" v-if="detailData">
          <a-descriptions-item label="岗位名称">{{ detailData.title }}</a-descriptions-item>
          <a-descriptions-item label="企业名称">{{ detailData.companyName }}</a-descriptions-item>
          <a-descriptions-item label="类别">{{ detailData.category }}</a-descriptions-item>
          <a-descriptions-item label="城市">{{ detailData.city }}</a-descriptions-item>
          <a-descriptions-item label="薪资范围">{{ detailData.salaryRange }}</a-descriptions-item>
          <a-descriptions-item label="工作类型">{{ detailData.jobType }}</a-descriptions-item>
          <a-descriptions-item label="学历要求">{{ detailData.education }}</a-descriptions-item>
          <a-descriptions-item label="经验要求">{{ detailData.experience }}</a-descriptions-item>
          <a-descriptions-item label="截止日期">{{ detailData.deadline }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="statusColorMap[detailData.status]">{{ statusTextMap[detailData.status] }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="岗位描述" :span="2">{{ detailData.description }}</a-descriptions-item>
          <a-descriptions-item label="任职要求" :span="2">{{ detailData.requirement }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detailData.createTime }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ detailData.updateTime }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>

    <!-- 审核备注弹窗 -->
    <a-modal
      :title="auditAction === 1 ? '通过审核' : '拒绝审核'"
      :visible="auditVisible"
      :confirm-loading="auditSubmitting"
      @ok="submitAudit"
      @cancel="auditVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="审核备注">
          <a-textarea
            v-model="auditRemark"
            placeholder="请输入审核备注（可选）"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script>
import { getJobList, getJobById, auditJob, deleteJob } from '@/api/job'

export default {
  name: 'JobList',
  data () {
    return {
      queryParam: {
        keyword: '',
        status: undefined,
        category: ''
      },
      dataSource: [],
      loading: false,
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showTotal: total => `共 ${total} 条`,
        showSizeChanger: true
      },
      statusColorMap: {
        1: 'orange',
        2: 'green',
        3: '',
        4: 'red'
      },
      statusTextMap: {
        1: '待审核',
        2: '已发布',
        3: '已关闭',
        4: '已拒绝'
      },
      // 详情
      detailVisible: false,
      detailLoading: false,
      detailData: null,
      // 审核
      auditVisible: false,
      auditSubmitting: false,
      auditRecord: null,
      auditAction: 2,
      auditRemark: '',
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '岗位名称', dataIndex: 'title', key: 'title', ellipsis: true },
        { title: '企业名称', dataIndex: 'companyName', key: 'companyName', ellipsis: true },
        { title: '类别', dataIndex: 'category', key: 'category', width: 100 },
        { title: '城市', dataIndex: 'city', key: 'city', width: 100 },
        { title: '薪资范围', dataIndex: 'salaryRange', key: 'salaryRange', width: 120 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'status' } },
        { title: '截止日期', dataIndex: 'deadline', key: 'deadline', width: 120 },
        { title: '操作', key: 'action', width: 220, scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    loadData () {
      this.loading = true
      const params = {
        page: this.pagination.current,
        size: this.pagination.pageSize,
        ...this.queryParam
      }
      if (!params.keyword) delete params.keyword
      if (params.status === undefined || params.status === '') delete params.status
      if (!params.category) delete params.category
      getJobList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载岗位列表失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.pagination.pageSize = pagination.pageSize
      this.loadData()
    },
    handleSearch () {
      this.pagination.current = 1
      this.loadData()
    },
    handleReset () {
      this.queryParam = { keyword: '', status: undefined, category: '' }
      this.pagination.current = 1
      this.loadData()
    },
    handleDetail (record) {
      this.detailVisible = true
      this.detailLoading = true
      this.detailData = null
      getJobById(record.id).then(res => {
        this.detailData = res.data
      }).catch(() => {
        this.$message.error('加载岗位详情失败')
      }).finally(() => {
        this.detailLoading = false
      })
    },
    handleAudit (record, action) {
      this.auditRecord = record
      this.auditAction = action
      this.auditRemark = ''
      this.auditVisible = true
    },
    submitAudit () {
      this.auditSubmitting = true
      auditJob(this.auditRecord.id, {
        auditStatus: this.auditAction,
        auditRemark: this.auditRemark
      }).then(() => {
        this.$message.success(this.auditAction === 1 ? '审核通过' : '已拒绝')
        this.auditVisible = false
        this.loadData()
      }).catch(() => {
        this.$message.error('审核操作失败')
      }).finally(() => {
        this.auditSubmitting = false
      })
    },
    handleDelete (record) {
      deleteJob(record.id).then(() => {
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {
        this.$message.error('删除失败')
      })
    }
  }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
</style>

<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="企业名称/联系人" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="审核状态">
              <a-select v-model="queryParam.auditStatus" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option :value="0">待审核</a-select-option>
                <a-select-option :value="1">已通过</a-select-option>
                <a-select-option :value="2">已拒绝</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
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
      <template slot="auditStatus" slot-scope="text">
        <a-tag :color="auditColorMap[text] || ''">{{ auditTextMap[text] || text }}</a-tag>
      </template>
      <template slot="action" slot-scope="text, record">
        <a @click="handleDetail(record)">查看详情</a>
        <template v-if="record.auditStatus === 0">
          <a-divider type="vertical" />
          <a style="color: #52c41a" @click="handleAudit(record, 1)">通过</a>
          <a-divider type="vertical" />
          <a style="color: #f5222d" @click="handleAudit(record, 2)">拒绝</a>
        </template>
      </template>
    </a-table>

    <!-- 企业详情弹窗 -->
    <a-modal
      title="企业详情"
      :visible="detailVisible"
      :footer="null"
      width="700px"
      @cancel="detailVisible = false"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions bordered :column="2" v-if="detailData">
          <a-descriptions-item label="企业名称">{{ detailData.companyName }}</a-descriptions-item>
          <a-descriptions-item label="行业">{{ detailData.industry }}</a-descriptions-item>
          <a-descriptions-item label="规模">{{ detailData.scale }}</a-descriptions-item>
          <a-descriptions-item label="城市">{{ detailData.city }}</a-descriptions-item>
          <a-descriptions-item label="联系人">{{ detailData.contactPerson }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ detailData.contactPhone }}</a-descriptions-item>
          <a-descriptions-item label="联系邮箱">{{ detailData.contactEmail }}</a-descriptions-item>
          <a-descriptions-item label="审核状态">
            <a-tag :color="auditColorMap[detailData.auditStatus]">{{ auditTextMap[detailData.auditStatus] }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="地址" :span="2">{{ detailData.address }}</a-descriptions-item>
          <a-descriptions-item label="企业简介" :span="2">{{ detailData.description }}</a-descriptions-item>
          <a-descriptions-item label="营业执照" :span="2">{{ detailData.license || '未上传' }}</a-descriptions-item>
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
import { getCompanyList, getCompanyById, auditCompany } from '@/api/company'

export default {
  name: 'CompanyList',
  data () {
    return {
      queryParam: {
        keyword: '',
        auditStatus: undefined
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
      auditColorMap: {
        0: 'orange',
        1: 'green',
        2: 'red'
      },
      auditTextMap: {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      },
      // 详情弹窗
      detailVisible: false,
      detailLoading: false,
      detailData: null,
      // 审核弹窗
      auditVisible: false,
      auditSubmitting: false,
      auditRecord: null,
      auditAction: 1,
      auditRemark: '',
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '企业名称', dataIndex: 'companyName', key: 'companyName', ellipsis: true },
        { title: '行业', dataIndex: 'industry', key: 'industry', width: 120 },
        { title: '规模', dataIndex: 'scale', key: 'scale', width: 100 },
        { title: '城市', dataIndex: 'city', key: 'city', width: 100 },
        { title: '审核状态', dataIndex: 'auditStatus', key: 'auditStatus', width: 100, scopedSlots: { customRender: 'auditStatus' } },
        { title: '联系人', dataIndex: 'contactPerson', key: 'contactPerson', width: 100 },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
        { title: '操作', key: 'action', width: 200, scopedSlots: { customRender: 'action' } }
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
      if (params.auditStatus === undefined || params.auditStatus === '') delete params.auditStatus
      getCompanyList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载企业列表失败')
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
      this.queryParam = { keyword: '', auditStatus: undefined }
      this.pagination.current = 1
      this.loadData()
    },
    handleDetail (record) {
      this.detailVisible = true
      this.detailLoading = true
      this.detailData = null
      getCompanyById(record.id).then(res => {
        this.detailData = res.data
      }).catch(() => {
        this.$message.error('加载企业详情失败')
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
      auditCompany(this.auditRecord.id, {
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
    }
  }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
</style>

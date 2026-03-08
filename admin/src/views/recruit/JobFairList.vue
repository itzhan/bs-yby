<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="标题/企业名称" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="状态">
              <a-select v-model="queryParam.status" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option :value="0">待审核</a-select-option>
                <a-select-option :value="1">已通过</a-select-option>
                <a-select-option :value="2">已拒绝</a-select-option>
                <a-select-option :value="3">已完成</a-select-option>
                <a-select-option :value="4">已取消</a-select-option>
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
      <template slot="time" slot-scope="text, record">
        {{ record.startTime }} ~ {{ record.endTime }}
      </template>
      <template slot="capacity" slot-scope="text, record">
        {{ record.currentCount || 0 }} / {{ record.maxCapacity || '-' }}
      </template>
      <template slot="status" slot-scope="text">
        <a-tag :color="statusColorMap[text] || ''">{{ statusTextMap[text] || text }}</a-tag>
      </template>
      <template slot="action" slot-scope="text, record">
        <template v-if="record.status === 0">
          <a style="color: #52c41a" @click="handleAudit(record, 1)">通过</a>
          <a-divider type="vertical" />
          <a style="color: #f5222d" @click="handleAudit(record, 2)">拒绝</a>
          <a-divider type="vertical" />
        </template>
        <a @click="handleBookings(record)">预约列表</a>
      </template>
    </a-table>

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

    <!-- 预约列表弹窗 -->
    <a-modal
      title="预约学生列表"
      :visible="bookingVisible"
      :footer="null"
      width="700px"
      @cancel="bookingVisible = false"
    >
      <a-table
        :columns="bookingColumns"
        :data-source="bookingList"
        :pagination="bookingPagination"
        :loading="bookingLoading"
        row-key="id"
        size="middle"
        @change="handleBookingTableChange"
      >
        <template slot="bookingStatus" slot-scope="text">
          <a-tag :color="text === 1 ? 'green' : text === 2 ? 'red' : 'blue'">
            {{ text === 1 ? '已签到' : text === 2 ? '已取消' : '已预约' }}
          </a-tag>
        </template>
      </a-table>
    </a-modal>
  </a-card>
</template>

<script>
import { getJobFairList, auditJobFair, getJobFairBookings } from '@/api/jobfair'

export default {
  name: 'JobFairList',
  data () {
    return {
      queryParam: {
        keyword: '',
        status: undefined
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
        0: 'orange',
        1: 'green',
        2: 'red',
        3: 'blue',
        4: ''
      },
      statusTextMap: {
        0: '待审核',
        1: '已通过',
        2: '已拒绝',
        3: '已完成',
        4: '已取消'
      },
      // 审核
      auditVisible: false,
      auditSubmitting: false,
      auditRecord: null,
      auditAction: 1,
      auditRemark: '',
      // 预约列表
      bookingVisible: false,
      bookingLoading: false,
      bookingList: [],
      bookingFairId: null,
      bookingPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showTotal: total => `共 ${total} 条`
      },
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
        { title: '企业名称', dataIndex: 'companyName', key: 'companyName', ellipsis: true },
        { title: '地点', dataIndex: 'location', key: 'location', width: 150, ellipsis: true },
        { title: '时间', key: 'time', width: 300, scopedSlots: { customRender: 'time' } },
        { title: '容量', key: 'capacity', width: 100, scopedSlots: { customRender: 'capacity' } },
        { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', width: 200, scopedSlots: { customRender: 'action' } }
      ],
      bookingColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '学生姓名', dataIndex: 'studentName', key: 'studentName' },
        { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'bookingStatus' } },
        { title: '预约时间', dataIndex: 'createTime', key: 'createTime', width: 170 }
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
      getJobFairList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载宣讲会列表失败')
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
      this.queryParam = { keyword: '', status: undefined }
      this.pagination.current = 1
      this.loadData()
    },
    handleAudit (record, action) {
      this.auditRecord = record
      this.auditAction = action
      this.auditRemark = ''
      this.auditVisible = true
    },
    submitAudit () {
      this.auditSubmitting = true
      auditJobFair(this.auditRecord.id, {
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
    handleBookings (record) {
      this.bookingFairId = record.id
      this.bookingPagination.current = 1
      this.bookingVisible = true
      this.loadBookings()
    },
    loadBookings () {
      this.bookingLoading = true
      getJobFairBookings(this.bookingFairId, {
        page: this.bookingPagination.current,
        size: this.bookingPagination.pageSize
      }).then(res => {
        this.bookingList = res.data.records || res.data || []
        this.bookingPagination.total = res.data.total || this.bookingList.length
      }).catch(() => {
        this.$message.error('加载预约列表失败')
      }).finally(() => {
        this.bookingLoading = false
      })
    },
    handleBookingTableChange (pagination) {
      this.bookingPagination.current = pagination.current
      this.bookingPagination.pageSize = pagination.pageSize
      this.loadBookings()
    }
  }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
</style>

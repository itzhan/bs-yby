<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="学生姓名/岗位名称/企业名称" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="状态">
              <a-select v-model="queryParam.status" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option :value="0">待查看</a-select-option>
                <a-select-option :value="1">已查看</a-select-option>
                <a-select-option :value="2">面试中</a-select-option>
                <a-select-option :value="3">已录用</a-select-option>
                <a-select-option :value="4">已拒绝</a-select-option>
                <a-select-option :value="5">已撤回</a-select-option>
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
      <template slot="status" slot-scope="text">
        <a-tag :color="statusColorMap[text] || ''">{{ statusTextMap[text] || text }}</a-tag>
      </template>
      <template slot="action" slot-scope="text, record">
        <a-select
          :value="record.status"
          style="width: 110px"
          size="small"
          @change="(val) => handleStatusChange(record, val)"
        >
          <a-select-option :value="0">待查看</a-select-option>
          <a-select-option :value="1">已查看</a-select-option>
          <a-select-option :value="2">面试中</a-select-option>
          <a-select-option :value="3">已录用</a-select-option>
          <a-select-option :value="4">已拒绝</a-select-option>
          <a-select-option :value="5">已撤回</a-select-option>
        </a-select>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import { getApplicationList, updateApplicationStatus } from '@/api/application'

export default {
  name: 'ApplicationList',
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
        0: 'blue',
        1: 'cyan',
        2: 'orange',
        3: 'green',
        4: 'red',
        5: ''
      },
      statusTextMap: {
        0: '待查看',
        1: '已查看',
        2: '面试中',
        3: '已录用',
        4: '已拒绝',
        5: '已撤回'
      },
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '学生姓名', dataIndex: 'studentName', key: 'studentName', width: 110 },
        { title: '岗位名称', dataIndex: 'jobTitle', key: 'jobTitle', ellipsis: true },
        { title: '企业名称', dataIndex: 'companyName', key: 'companyName', ellipsis: true },
        { title: '简历标题', dataIndex: 'resumeTitle', key: 'resumeTitle', ellipsis: true },
        { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'status' } },
        { title: '投递时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
        { title: '操作', key: 'action', width: 140, scopedSlots: { customRender: 'action' } }
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
      getApplicationList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载投递列表失败')
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
    handleStatusChange (record, newStatus) {
      if (newStatus === record.status) return
      const statusText = this.statusTextMap[newStatus]
      const self = this
      this.$confirm({
        title: '确认操作',
        content: `确定将投递状态更改为「${statusText}」？`,
        onOk () {
          return updateApplicationStatus(record.id, { status: newStatus }).then(() => {
            self.$message.success('状态更新成功')
            self.loadData()
          }).catch(() => {
            self.$message.error('状态更新失败')
          })
        }
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

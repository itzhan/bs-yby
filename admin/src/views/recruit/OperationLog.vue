<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="操作用户/操作内容" allow-clear />
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
      <template slot="result" slot-scope="text">
        <a-tag :color="text === 1 || text === '成功' ? 'green' : 'red'">{{ text === 1 || text === '成功' ? '成功' : '失败' }}</a-tag>
      </template>
      <template slot="duration" slot-scope="text">
        {{ text }} ms
      </template>
    </a-table>
  </a-card>
</template>

<script>
import { getOperationLogList } from '@/api/operationLog'

export default {
  name: 'OperationLog',
  data () {
    return {
      queryParam: {
        keyword: ''
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
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '操作用户', dataIndex: 'username', key: 'username', width: 120 },
        { title: '操作', dataIndex: 'operation', key: 'operation', ellipsis: true },
        { title: '方法', dataIndex: 'method', key: 'method', ellipsis: true },
        { title: 'IP', dataIndex: 'ip', key: 'ip', width: 140 },
        { title: '耗时', dataIndex: 'duration', key: 'duration', width: 100, scopedSlots: { customRender: 'duration' } },
        { title: '结果', dataIndex: 'result', key: 'result', width: 80, scopedSlots: { customRender: 'result' } },
        { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 170 }
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
      getOperationLogList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载操作日志失败')
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
      this.queryParam = { keyword: '' }
      this.pagination.current = 1
      this.loadData()
    }
  }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
</style>

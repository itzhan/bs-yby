<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="用户名/昵称/邮箱" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="角色">
              <a-select v-model="queryParam.role" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="ADMIN">管理员</a-select-option>
                <a-select-option value="STUDENT">学生</a-select-option>
                <a-select-option value="COMPANY">企业</a-select-option>
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
      <template slot="role" slot-scope="text">
        <a-tag :color="roleColorMap[text] || ''">{{ roleTextMap[text] || text }}</a-tag>
      </template>
      <template slot="status" slot-scope="text">
        <a-badge :status="text === 1 ? 'success' : 'error'" :text="text === 1 ? '正常' : '禁用'" />
      </template>
      <template slot="action" slot-scope="text, record">
        <a-popconfirm
          :title="record.status === 1 ? '确定禁用该用户？' : '确定启用该用户？'"
          @confirm="handleToggleStatus(record)"
        >
          <a v-if="record.status === 1" style="color: #faad14">禁用</a>
          <a v-else style="color: #52c41a">启用</a>
        </a-popconfirm>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除该用户？" @confirm="handleDelete(record)">
          <a style="color: #f5222d">删除</a>
        </a-popconfirm>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import { getUserList, updateUserStatus, deleteUser } from '@/api/user'

export default {
  name: 'UserList',
  data () {
    return {
      queryParam: {
        keyword: '',
        role: undefined
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
      roleColorMap: {
        'ADMIN': 'red',
        'STUDENT': 'blue',
        'COMPANY': 'green'
      },
      roleTextMap: {
        'ADMIN': '管理员',
        'STUDENT': '学生',
        'COMPANY': '企业'
      },
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
        { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 120 },
        { title: '角色', dataIndex: 'role', key: 'role', width: 100, scopedSlots: { customRender: 'role' } },
        { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
        { title: '手机', dataIndex: 'phone', key: 'phone', width: 130 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'status' } },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
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
      // 清理空值
      if (!params.role) delete params.role
      if (!params.keyword) delete params.keyword
      getUserList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载用户列表失败')
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
      this.queryParam = { keyword: '', role: undefined }
      this.pagination.current = 1
      this.loadData()
    },
    handleToggleStatus (record) {
      const newStatus = record.status === 1 ? 0 : 1
      updateUserStatus(record.id, { status: newStatus }).then(() => {
        this.$message.success(newStatus === 1 ? '已启用' : '已禁用')
        this.loadData()
      }).catch(() => {
        this.$message.error('操作失败')
      })
    },
    handleDelete (record) {
      deleteUser(record.id).then(() => {
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

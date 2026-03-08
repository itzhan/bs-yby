<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索栏 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="关键词">
                <a-input v-model="queryParam.keyword" placeholder="请输入用户名/昵称/邮箱" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="角色">
                <a-select v-model="queryParam.role" placeholder="请选择角色" allow-clear>
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

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        rowKey="id"
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
            :title="record.status === 1 ? '确认禁用该用户？' : '确认启用该用户？'"
            @confirm="handleToggleStatus(record)"
          >
            <a>{{ record.status === 1 ? '禁用' : '启用' }}</a>
          </a-popconfirm>
          <a-divider type="vertical" />
          <a-popconfirm title="确认删除该用户？" @confirm="handleDelete(record)">
            <a style="color: #f5222d">删除</a>
          </a-popconfirm>
        </template>
      </a-table>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { PageHeaderWrapper } from '@ant-design-vue/pro-layout'
import { getUserList, updateUserStatus, deleteUser } from '@/api/user'

export default {
  name: 'UserList',
  components: {
    PageHeaderWrapper
  },
  data () {
    return {
      loading: false,
      dataSource: [],
      queryParam: {
        keyword: '',
        role: undefined
      },
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showTotal: total => `共 ${total} 条`
      },
      columns: [
        { title: 'ID', dataIndex: 'id', width: 60 },
        { title: '用户名', dataIndex: 'username', width: 120 },
        { title: '昵称', dataIndex: 'nickname', width: 120 },
        { title: '角色', dataIndex: 'role', width: 100, scopedSlots: { customRender: 'role' } },
        { title: '邮箱', dataIndex: 'email', ellipsis: true },
        { title: '手机', dataIndex: 'phone', width: 130 },
        { title: '状态', dataIndex: 'status', width: 100, scopedSlots: { customRender: 'status' } },
        { title: '创建时间', dataIndex: 'createTime', width: 170 },
        { title: '操作', width: 150, scopedSlots: { customRender: 'action' } }
      ],
      roleColorMap: {
        ADMIN: 'red',
        STUDENT: 'blue',
        COMPANY: 'green'
      },
      roleTextMap: {
        ADMIN: '管理员',
        STUDENT: '学生',
        COMPANY: '企业'
      }
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    async loadData () {
      this.loading = true
      try {
        const res = await getUserList({
          page: this.pagination.current,
          size: this.pagination.pageSize,
          keyword: this.queryParam.keyword || undefined,
          role: this.queryParam.role || undefined
        })
        this.dataSource = res.data.records
        this.pagination.total = res.data.total
      } catch (e) {
        this.$message.error('加载用户列表失败')
      } finally {
        this.loading = false
      }
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
    async handleToggleStatus (record) {
      try {
        await updateUserStatus(record.id, { status: record.status === 1 ? 0 : 1 })
        this.$message.success('操作成功')
        this.loadData()
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async handleDelete (record) {
      try {
        await deleteUser(record.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (e) {
        this.$message.error('删除失败')
      }
    }
  }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 24px;
}
.table-page-search-submitButtons {
  display: block;
  margin-bottom: 24px;
}
</style>

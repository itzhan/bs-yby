<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="6" :sm="24">
            <a-form-item label="类型">
              <a-select v-model="queryParam.type" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="system">系统公告</a-select-option>
                <a-select-option value="recruitment">招聘公告</a-select-option>
                <a-select-option value="activity">活动公告</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="24">
            <a-form-item label="状态">
              <a-select v-model="queryParam.status" placeholder="全部" allow-clear>
                <a-select-option value="">全部</a-select-option>
                <a-select-option :value="0">草稿</a-select-option>
                <a-select-option :value="1">已发布</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="24">
            <a-form-item label="关键词">
              <a-input v-model="queryParam.keyword" placeholder="公告标题" allow-clear />
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
    <div style="margin-bottom: 16px">
      <a-button type="primary" icon="plus" @click="handleAdd">新增公告</a-button>
    </div>
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      :loading="loading"
      row-key="id"
      @change="handleTableChange"
    >
      <template slot="type" slot-scope="text">
        <a-tag :color="typeColorMap[text] || ''">{{ typeTextMap[text] || text }}</a-tag>
      </template>
      <template slot="status" slot-scope="text">
        <a-tag :color="text === 1 ? 'green' : ''">{{ text === 1 ? '已发布' : '草稿' }}</a-tag>
      </template>
      <template slot="isTop" slot-scope="text">
        <a-tag :color="text ? 'red' : ''">{{ text ? '是' : '否' }}</a-tag>
      </template>
      <template slot="action" slot-scope="text, record">
        <a @click="handleEdit(record)">编辑</a>
        <a-divider type="vertical" />
        <a-popconfirm title="确定删除该公告？" @confirm="handleDelete(record)">
          <a style="color: #f5222d">删除</a>
        </a-popconfirm>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      :title="formMode === 'add' ? '新增公告' : '编辑公告'"
      :visible="formVisible"
      :confirm-loading="formSubmitting"
      width="650px"
      @ok="submitForm"
      @cancel="formVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="标题">
          <a-input v-model="formData.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea v-model="formData.content" placeholder="请输入公告内容" :rows="6" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="类型">
              <a-select v-model="formData.type" placeholder="请选择类型">
                <a-select-option value="system">系统公告</a-select-option>
                <a-select-option value="recruitment">招聘公告</a-select-option>
                <a-select-option value="activity">活动公告</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="状态">
              <a-select v-model="formData.status" placeholder="请选择状态">
                <a-select-option :value="0">草稿</a-select-option>
                <a-select-option :value="1">已发布</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="是否置顶">
              <a-select v-model="formData.isTop" placeholder="请选择">
                <a-select-option :value="true">是</a-select-option>
                <a-select-option :value="false">否</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script>
import { getAnnouncementList, getAnnouncementById, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'

export default {
  name: 'AnnouncementList',
  data () {
    return {
      queryParam: {
        type: undefined,
        status: undefined,
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
      typeColorMap: {
        'system': 'blue',
        'recruitment': 'green',
        'activity': 'purple'
      },
      typeTextMap: {
        'system': '系统公告',
        'recruitment': '招聘公告',
        'activity': '活动公告'
      },
      // 表单
      formVisible: false,
      formSubmitting: false,
      formMode: 'add',
      formData: {
        title: '',
        content: '',
        type: 'system',
        status: 0,
        isTop: false
      },
      editId: null,
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
        { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
        { title: '类型', dataIndex: 'type', key: 'type', width: 110, scopedSlots: { customRender: 'type' } },
        { title: '状态', dataIndex: 'status', key: 'status', width: 90, scopedSlots: { customRender: 'status' } },
        { title: '是否置顶', dataIndex: 'isTop', key: 'isTop', width: 90, scopedSlots: { customRender: 'isTop' } },
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
      if (!params.type) delete params.type
      if (params.status === undefined || params.status === '') delete params.status
      if (!params.keyword) delete params.keyword
      getAnnouncementList(params).then(res => {
        this.dataSource = res.data.records || []
        this.pagination.total = res.data.total || 0
      }).catch(() => {
        this.$message.error('加载公告列表失败')
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
      this.queryParam = { type: undefined, status: undefined, keyword: '' }
      this.pagination.current = 1
      this.loadData()
    },
    handleAdd () {
      this.formMode = 'add'
      this.editId = null
      this.formData = {
        title: '',
        content: '',
        type: 'system',
        status: 0,
        isTop: false
      }
      this.formVisible = true
    },
    handleEdit (record) {
      this.formMode = 'edit'
      this.editId = record.id
      // 加载最新数据
      getAnnouncementById(record.id).then(res => {
        const d = res.data
        this.formData = {
          title: d.title || '',
          content: d.content || '',
          type: d.type || 'system',
          status: d.status !== undefined ? d.status : 0,
          isTop: !!d.isTop
        }
        this.formVisible = true
      }).catch(() => {
        this.$message.error('加载公告详情失败')
      })
    },
    submitForm () {
      if (!this.formData.title) {
        this.$message.warning('请输入公告标题')
        return
      }
      if (!this.formData.content) {
        this.$message.warning('请输入公告内容')
        return
      }
      this.formSubmitting = true
      const request = this.formMode === 'add'
        ? createAnnouncement(this.formData)
        : updateAnnouncement(this.editId, this.formData)
      request.then(() => {
        this.$message.success(this.formMode === 'add' ? '新增成功' : '更新成功')
        this.formVisible = false
        this.loadData()
      }).catch(() => {
        this.$message.error(this.formMode === 'add' ? '新增失败' : '更新失败')
      }).finally(() => {
        this.formSubmitting = false
      })
    },
    handleDelete (record) {
      deleteAnnouncement(record.id).then(() => {
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

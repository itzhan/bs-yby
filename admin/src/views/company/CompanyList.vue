<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索栏 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="审核状态">
                <a-select v-model="queryParam.auditStatus" placeholder="请选择审核状态" allow-clear>
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option :value="0">待审核</a-select-option>
                  <a-select-option :value="1">已通过</a-select-option>
                  <a-select-option :value="2">已拒绝</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="关键词">
                <a-input v-model="queryParam.keyword" placeholder="请输入企业名称" allow-clear />
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
        <template slot="auditStatus" slot-scope="text">
          <a-tag :color="auditStatusMap[text] ? auditStatusMap[text].color : ''">
            {{ auditStatusMap[text] ? auditStatusMap[text].label : text }}
          </a-tag>
        </template>
        <template slot="action" slot-scope="text, record">
          <a @click="handleAudit(record)" v-if="record.auditStatus === 0">审核</a>
          <span v-else style="color: #999">已审核</span>
        </template>
      </a-table>
    </a-card>

    <!-- 审核弹窗 -->
    <a-modal
      title="企业审核"
      :visible="auditModalVisible"
      :confirm-loading="auditLoading"
      @ok="handleAuditSubmit"
      @cancel="auditModalVisible = false"
    >
      <a-form-item label="审核结果" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-radio-group v-model="auditForm.auditStatus">
          <a-radio :value="1">通过</a-radio>
          <a-radio :value="2">拒绝</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="审核备注" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-textarea
          v-model="auditForm.auditRemark"
          placeholder="请输入审核备注"
          :rows="4"
        />
      </a-form-item>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import { PageHeaderWrapper } from '@ant-design-vue/pro-layout'
import { getCompanyList, auditCompany } from '@/api/company'

export default {
  name: 'CompanyList',
  components: {
    PageHeaderWrapper
  },
  data () {
    return {
      loading: false,
      dataSource: [],
      queryParam: {
        auditStatus: undefined,
        keyword: ''
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
        { title: '企业名称', dataIndex: 'companyName', ellipsis: true },
        { title: '行业', dataIndex: 'industry', width: 120 },
        { title: '规模', dataIndex: 'scale', width: 120 },
        { title: '城市', dataIndex: 'city', width: 100 },
        { title: '联系人', dataIndex: 'contactPerson', width: 100 },
        { title: '审核状态', dataIndex: 'auditStatus', width: 100, scopedSlots: { customRender: 'auditStatus' } },
        { title: '创建时间', dataIndex: 'createTime', width: 170 },
        { title: '操作', width: 100, scopedSlots: { customRender: 'action' } }
      ],
      auditStatusMap: {
        0: { label: '待审核', color: 'orange' },
        1: { label: '已通过', color: 'green' },
        2: { label: '已拒绝', color: 'red' }
      },
      // 审核弹窗
      auditModalVisible: false,
      auditLoading: false,
      currentRecord: null,
      auditForm: {
        auditStatus: 1,
        auditRemark: ''
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
        const res = await getCompanyList({
          page: this.pagination.current,
          size: this.pagination.pageSize,
          auditStatus: this.queryParam.auditStatus !== undefined && this.queryParam.auditStatus !== '' ? this.queryParam.auditStatus : undefined,
          keyword: this.queryParam.keyword || undefined
        })
        this.dataSource = res.data.records
        this.pagination.total = res.data.total
      } catch (e) {
        this.$message.error('加载企业列表失败')
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
      this.queryParam = { auditStatus: undefined, keyword: '' }
      this.pagination.current = 1
      this.loadData()
    },
    handleAudit (record) {
      this.currentRecord = record
      this.auditForm = { auditStatus: 1, auditRemark: '' }
      this.auditModalVisible = true
    },
    async handleAuditSubmit () {
      this.auditLoading = true
      try {
        await auditCompany(this.currentRecord.id, {
          auditStatus: this.auditForm.auditStatus,
          auditRemark: this.auditForm.auditRemark
        })
        this.$message.success('审核操作成功')
        this.auditModalVisible = false
        this.loadData()
      } catch (e) {
        this.$message.error('审核操作失败')
      } finally {
        this.auditLoading = false
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

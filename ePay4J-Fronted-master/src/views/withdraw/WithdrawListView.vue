<template>
  <PageContainer title="提现管理" desc="管理商户提现任务">
    <template #actions>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon>刷新
      </el-button>
    </template>

    <!-- Filter -->
    <div class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent="loadData">
        <el-form-item label="商户名">
          <el-input v-model="query.merchantName" placeholder="搜索商户名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:130px">
            <el-option label="处理中" :value="0" />
            <el-option label="已打款" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Table -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="提现单号" width="100">
          <template #default="{ row }">
            <span class="font-mono" style="font-size:12px">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantId" label="商户ID" width="80">
          <template #default="{ row }">
            <span style="font-family:'Geist Mono',monospace;color:var(--ep-text-secondary)">{{ row.merchantId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户名" min-width="140">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <div class="merchant-avatar">{{ (row.merchantName || '商').charAt(0) }}</div>
              <span style="font-weight:500">{{ row.merchantName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="{ row }">
            <span style="font-weight:700;font-family:'Geist Mono',monospace">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="serviceFee" label="手续费" width="110">
          <template #default="{ row }">
            <span style="font-family:'Geist Mono',monospace;color:var(--ep-text-muted)">
              {{ row.serviceFee != null ? '¥' + formatMoney(row.serviceFee) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="amountCredited" label="实际到账" width="120">
          <template #default="{ row }">
            <span style="font-weight:700;font-family:'Geist Mono',monospace;color:var(--ep-success)">
              {{ row.amountCredited != null ? '¥' + formatMoney(row.amountCredited) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" round>
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transferStatus" label="转账状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.transferStatus" type="info" size="small" round>{{ row.transferStatus }}</el-tag>
            <span v-else style="color:var(--ep-text-muted)">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="alipayOrderId" label="支付宝订单号" min-width="180">
          <template #default="{ row }">
            <span class="font-mono" style="font-size:12px;color:var(--ep-text-secondary)">{{ row.alipayOrderId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140">
          <template #default="{ row }">
            <span style="color:var(--ep-text-secondary)">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--ep-text-secondary)">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" link size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </div>

    <!-- Detail Drawer -->
    <el-drawer v-model="detailVisible" title="提现详情" size="480px">
      <div v-if="currentRecord" class="detail-sections">
        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">提现单号</span>
              <span class="info-value font-mono">{{ currentRecord.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span class="info-value">
                <el-tag :type="getStatusType(currentRecord.status)" size="small">
                  {{ getStatusLabel(currentRecord.status) }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">提现金额</span>
              <span class="info-value" style="font-weight:700;font-family:'Geist Mono',monospace">
                ¥{{ formatMoney(currentRecord.amount) }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">手续费</span>
              <span class="info-value font-mono">
                {{ currentRecord.serviceFee != null ? '¥' + formatMoney(currentRecord.serviceFee) : '-' }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">实际到账</span>
              <span class="info-value font-mono" style="font-weight:700;color:var(--ep-success)">
                {{ currentRecord.amountCredited != null ? '¥' + formatMoney(currentRecord.amountCredited) : '-' }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">申请时间</span>
              <span class="info-value">{{ formatDate(currentRecord.createTime) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">商户信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">商户ID</span>
              <span class="info-value font-mono">{{ currentRecord.merchantId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">商户名称</span>
              <span class="info-value">{{ currentRecord.merchantName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ currentRecord.nickName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value font-mono">{{ currentRecord.phone || '-' }}</span>
            </div>
            <div class="info-item full">
              <span class="info-label">支付宝账号</span>
              <span class="info-value font-mono">{{ currentRecord.alipayAccount || '-' }}</span>
            </div>
          </div>
        </div>

          <template v-if="currentRecord && currentRecord.transferNo">
            <div class="detail-section">
              <h4 class="section-title">转账信息</h4>
              <div class="info-grid">
                <div class="info-item full">
                  <span class="info-label">平台转账单号</span>
                  <span class="info-value font-mono">{{ currentRecord.transferNo }}</span>
                </div>
                <div class="info-item full">
                  <span class="info-label">支付宝订单号</span>
                  <span class="info-value font-mono">{{ currentRecord.alipayOrderId || '-' }}</span>
                </div>
                <div class="info-item full">
                  <span class="info-label">支付宝资金流水</span>
                  <span class="info-value font-mono">{{ currentRecord.payFundOrderId || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">转账状态</span>
                  <span class="info-value">{{ currentRecord.transferStatus || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">打款时间</span>
                  <span class="info-value">{{ formatDate(currentRecord.transferTime) || '-' }}</span>
                </div>
                <div class="info-item full">
                  <span class="info-label">返回信息</span>
                  <span class="info-value">{{ currentRecord.transferMsg || '-' }}</span>
                </div>
              </div>
            </div>
          </template>

        <div class="detail-section" v-if="currentRecord.remark">
          <h4 class="section-title">备注</h4>
          <div class="info-grid">
            <div class="info-item full">
              <span class="info-value">{{ currentRecord.remark }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display:flex;gap:8px">
          <template v-if="currentRecord && currentRecord.status === 0">
            <el-button type="success" @click="handleApprove(currentRecord)">通过</el-button>
            <el-button type="danger" plain @click="handleReject(currentRecord)">拒绝</el-button>
          </template>
          <div style="flex:1"></div>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>
    <!-- Approve Dialog -->
    <el-dialog v-model="approveVisible" title="通过提现" width="440px">
      <div v-if="approveRecord">
        <el-alert type="info" :closable="false" style="margin-bottom:16px"
          :title="`向商户打款 ¥${formatMoney(approveRecord.amountCredited != null ? approveRecord.amountCredited : approveRecord.amount)}`" />
        <div class="payee-info">
          <div class="payee-row"><span>收款人</span><strong>{{ approveRecord.nickName || '-' }}</strong></div>
          <div class="payee-row"><span>手机号</span><strong class="font-mono">{{ approveRecord.phone || '-' }}</strong></div>
          <div class="payee-row"><span>支付宝账号</span><strong class="font-mono">{{ approveRecord.alipayAccount || '-' }}</strong></div>
        </div>
        <div class="approve-actions">
          <el-button type="primary" :loading="approving" @click="doAutoTransfer">
            自动转账（支付宝转账到账户）
          </el-button>
          <el-button type="success" plain :loading="approving" @click="doManualTransfer">
            我已手动转账，标记完成
          </el-button>
        </div>
      </div>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getWithdrawPage, getWithdrawDetail, approveWithdraw, approveWithdrawManual, rejectWithdraw } from '../../api/order'
import { formatMoney, formatDate } from '../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentRecord = ref(null)
const approveVisible = ref(false)
const approveRecord = ref(null)
const approving = ref(false)

const query = reactive({ page: 1, size: 20, merchantId: null, merchantName: '', status: null })

const statusMap = {
  0: { label: '处理中', type: 'warning' },
  1: { label: '已打款', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
}

onMounted(() => loadData())

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

function getStatusLabel(status) {
  return statusMap[status]?.label || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.merchantId) params.merchantId = query.merchantId
    if (query.merchantName) params.merchantName = query.merchantName
    if (query.status !== null && query.status !== '') params.status = query.status
    const res = await getWithdrawPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.merchantId = null
  query.merchantName = ''
  query.status = null
  query.page = 1
  loadData()
}

async function viewDetail(row) {
  try {
    const res = await getWithdrawDetail(row.id)
    currentRecord.value = res.data
  } catch {
    currentRecord.value = row
  }
  detailVisible.value = true
}

async function handleApprove(row) {
  try {
    const res = await getWithdrawDetail(row.id)
    approveRecord.value = res.data
  } catch {
    approveRecord.value = row
  }
  approveVisible.value = true
  if (detailVisible.value) detailVisible.value = false
}

async function doAutoTransfer() {
  try {
    await ElMessageBox.confirm('将立即调用支付宝转账到商户支付宝账号，确定继续？', '自动转账', { type: 'warning' })
  } catch { return }
  approving.value = true
  try {
    await approveWithdraw(approveRecord.value.id)
    ElMessage.success('支付宝转账成功，提现已完成')
    approveVisible.value = false
    loadData()
  } finally {
    approving.value = false
  }
}

async function doManualTransfer() {
  try {
    await ElMessageBox.confirm('请确认已通过支付宝/微信等方式向商户完成线下转账，标记后提现将置为已打款。', '手动转账', { type: 'warning' })
  } catch { return }
  approving.value = true
  try {
    await approveWithdrawManual(approveRecord.value.id)
    ElMessage.success('已标记为手动打款完成')
    approveVisible.value = false
    loadData()
  } finally {
    approving.value = false
  }
}

async function handleReject(row) {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '拒绝提现', {
      inputPlaceholder: '请输入拒绝原因',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝理由不能为空',
    }).then(async ({ value }) => {
      await rejectWithdraw(row.id, { reason: value })
      ElMessage.success('已拒绝')
      detailVisible.value = false
      loadData()
    })
  } catch { /* cancel */ }
}
</script>

<style scoped>
.filter-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 16px 20px;
}

.table-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 16px 20px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.merchant-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--ep-radius-sm);
  background: var(--ep-chip-bg);
  color: var(--ep-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.font-mono {
  font-family: 'Geist Mono', 'SF Mono', Consolas, monospace;
}

.payee-info {
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 4px 16px;
}

.payee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
}

.payee-row + .payee-row {
  border-top: 1px solid var(--ep-border);
}

.payee-row span {
  color: var(--ep-text-muted);
}

.approve-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}

.approve-actions :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section {
  padding: 0;
}

.section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--ep-text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--ep-border);
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
}

.info-item {
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 11px;
  color: var(--ep-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: var(--ep-text-primary);
  font-weight: 500;
}
</style>

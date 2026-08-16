<template>
  <PageContainer title="提现管理" desc="查看提现记录和申请提现">
    <template #actions>
      <el-tooltip :disabled="!withdrawDisabled" :content="withdrawDisabledTip" placement="bottom">
        <el-button type="primary" :disabled="withdrawDisabled" @click="openWithdrawDialog">
          <el-icon><Wallet /></el-icon>申请提现
        </el-button>
      </el-tooltip>
    </template>

    <!-- Balance Summary -->
    <div class="balance-bar">
      <div class="balance-item">
        <span class="balance-label">可提现余额</span>
        <span class="balance-value">¥{{ formatMoney(availableBalance) }}</span>
      </div>
      <div class="balance-divider"></div>
      <div class="balance-item">
        <span class="balance-label">冻结金额</span>
        <span class="balance-value">¥{{ formatMoney(freezeBalance) }}</span>
      </div>
      <div class="balance-divider"></div>
      <div class="balance-item">
        <span class="balance-label">账户余额</span>
        <span class="balance-value">¥{{ formatMoney(totalBalance) }}</span>
      </div>
    </div>

    <div v-if="userStore.isActingMerchantView" class="acting-tip">
      管理员商户视角仅支持查看余额和提现记录，不能代商户发起提现。
    </div>

    <!-- Records Table -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="提现单号" width="100">
          <template #default="{ row }">
            <span class="font-mono" style="font-size:12px;color:var(--ep-text-secondary)">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="{ row }">
            <span style="font-weight:700;font-family:'Geist Mono',monospace;color:var(--ep-text-primary)">¥{{ formatMoney(row.amount) }}</span>
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
        <el-table-column prop="remark" label="备注" min-width="120">
          <template #default="{ row }">
            <span style="color:var(--ep-text-muted)">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--ep-text-muted)">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="处理时间" width="170">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--ep-text-muted)">{{ formatDate(row.updateTime) || '-' }}</span>
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
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- Withdraw Dialog -->
    <el-dialog v-model="dialogVisible" title="申请提现" width="400px">
      <el-form ref="formRef" :model="withdrawForm" :rules="rules" label-width="80px">
        <el-form-item label="提现金额" prop="amount">
          <el-input-number
            v-model="withdrawForm.amount"
            :precision="2"
            :min="0.01"
            :max="availableBalance || undefined"
            controls-position="right"
            style="width:100%"
            placeholder="请输入提现金额"
          />
        </el-form-item>
        <div class="withdraw-tip">
          <el-alert v-if="availableBalance <= 0" type="warning" :closable="false" show-icon>
            <template #title>
              当前无可提现余额，请等待处理中的提现完成
            </template>
          </el-alert>
          <el-alert v-else type="info" :closable="false" show-icon>
            <template #title>
              当前可提现余额：¥{{ formatMoney(availableBalance) }}
            </template>
          </el-alert>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定提现</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getWithdrawRecords, submitWithdraw, getMerchantAccountInfo } from '../../api/auth'
import { useUserStore } from '../../store/modules/user'
import { formatMoney, formatDate } from '../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const availableBalance = ref(0)
const freezeBalance = ref(0)
const totalBalance = ref(0)

const query = reactive({ page: 1, size: 20 })
const withdrawForm = reactive({ amount: null })
const hasPending = computed(() => tableData.value.some(r => r.status === 0))
const withdrawDisabled = computed(() => userStore.isActingMerchantView || hasPending.value)
const withdrawDisabledTip = computed(() => {
  if (userStore.isActingMerchantView) return '管理员商户视角不能代商户发起提现'
  if (hasPending.value) return '有处理中的提现任务，请等待完成后再申请'
  return ''
})
const rules = {
  amount: [
    { required: true, message: '请输入提现金额', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value <= 0) {
          callback(new Error('提现金额必须大于0'))
        } else if (value > availableBalance.value) {
          callback(new Error('超过可提现余额'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const statusMap = {
  0: { label: '处理中', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
}

onMounted(() => {
  loadAccount()
  loadData()
})

async function loadAccount() {
  try {
    const res = await getMerchantAccountInfo()
    const data = res.data
    availableBalance.value = data.availableBalance || 0
    freezeBalance.value = data.frozenBalance || 0
    totalBalance.value = availableBalance.value + freezeBalance.value
  } catch { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getWithdrawRecords(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSizeChange() {
  query.page = 1
  loadData()
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

function getStatusLabel(status) {
  return statusMap[status]?.label || '未知'
}

function openWithdrawDialog() {
  if (userStore.isActingMerchantView) {
    ElMessage.warning('管理员商户视角不能代商户发起提现')
    return
  }
  withdrawForm.amount = null
  dialogVisible.value = true
}

async function handleSubmit() {
  if (userStore.isActingMerchantView) {
    ElMessage.warning('管理员商户视角不能代商户发起提现')
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await submitWithdraw({ amount: String(withdrawForm.amount) })
    ElMessage.success('提现申请已提交')
    dialogVisible.value = false
    loadData()
    loadAccount()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提现申请失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.balance-bar {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 22px 32px;
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 24px;
}

.balance-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.balance-label {
  font-size: 12px;
  color: var(--ep-text-muted);
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.balance-value {
  font-size: 22px;
  font-weight: 700;
  font-family: 'Geist Mono', monospace;
  color: var(--ep-text-primary);
}

.balance-divider {
  width: 1px;
  height: 40px;
  background: var(--ep-border);
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

.withdraw-tip {
  margin-top: 8px;
}

.acting-tip {
  margin-bottom: 16px;
  padding: 10px 12px;
  border: 1px solid rgba(230, 162, 60, 0.32);
  border-radius: var(--ep-radius-sm);
  background: rgba(230, 162, 60, 0.08);
  color: #e6a23c;
  font-size: 13px;
}

.font-mono {
  font-family: 'Geist Mono', 'SF Mono', Consolas, monospace;
}
</style>

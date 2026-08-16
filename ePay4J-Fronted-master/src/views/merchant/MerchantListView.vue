<template>
  <PageContainer title="商户管理" desc="管理接入商户的密钥和状态">
    <template #actions>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增商户
      </el-button>
    </template>

    <!-- Filter -->
    <div class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent="loadData">
        <el-form-item label="商户名">
          <el-input v-model="query.name" placeholder="搜索商户名" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:130px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="id" label="PID" width="80">
          <template #default="{ row }">
            <span style="font-family:'Geist Mono',monospace;font-weight:600;color:var(--ep-text-secondary)">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商户名" min-width="160">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:10px">
              <div class="merchant-avatar">{{ row.name.charAt(0) }}</div>
              <span style="font-weight:500">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="merchantKey" label="密钥" min-width="280">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px">
              <span class="font-mono key-text" :class="{ hidden: !row.showKey }">
                {{ row.showKey ? row.merchantKey : '••••••••••••••••••••••••••••' }}
              </span>
              <el-button type="primary" link size="small" @click="row.showKey = !row.showKey" style="font-size:12px">
                {{ row.showKey ? '隐藏' : '显示' }}
              </el-button>
              <el-button type="primary" link size="small" @click="copyKey(row.merchantKey)" style="font-size:12px">
                复制
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--ep-text-secondary)">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="goToAccount(row)">账户管理</el-button>
            <el-button type="info" link size="small" @click="enterMerchantView(row)">进入商户视角</el-button>
            <el-button v-if="userStore.role === 'SUPER_ADMIN'" type="primary" link size="small" @click="handleBindSelfMerchant(row)">绑定为我的商户</el-button>
            <el-button type="warning" link size="small" @click="handleResetKey(row)">重置密钥</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商户' : '新增商户'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商户名" prop="name">
          <el-input v-model="form.name" placeholder="请输入商户名" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMerchantPage, createMerchant, updateMerchant, updateMerchantStatus, resetMerchantKey, bindCurrentAdminMerchant, deleteMerchant } from '../../api/merchant'
import { useUserStore } from '../../store/modules/user'
import { formatDate } from '../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const query = reactive({ page: 1, size: 20, name: '', status: null })
const form = reactive({ name: '', username: '', password: '' })
const rules = {
  name: [{ required: true, message: '请输入商户名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.name) params.name = query.name
    if (query.status !== null && query.status !== '') params.status = query.status
    const res = await getMerchantPage(params)
    tableData.value = (res.data.records || []).map(r => ({ ...r, showKey: false }))
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.name = ''
  query.status = null
  query.page = 1
  loadData()
}

function openDialog(row) {
  editingId.value = row?.id || null
  form.name = row?.name || ''
  form.username = ''
  form.password = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateMerchant(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createMerchant(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  await updateMerchantStatus(row.id, row.status)
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

async function handleResetKey(row) {
  try {
    await ElMessageBox.confirm('重置后原密钥将失效，确定继续？', '警告', { type: 'warning' })
    await resetMerchantKey(row.id)
    ElMessage.success('密钥已重置')
    loadData()
  } catch { /* cancel */ }
}

async function handleBindSelfMerchant(row) {
  try {
    await ElMessageBox.confirm(
      `确定将商户「${row.name}」绑定为你的自用商户吗？绑定后可在登录页选择“商户”并使用当前超级管理员账号进入商户中心。`,
      '绑定自用商户',
      { type: 'warning' }
    )
    const res = await bindCurrentAdminMerchant(row.id)
    const merchant = res.data || row
    ElMessage.success(`已绑定为你的自用商户：${merchant.name || row.name}`)
    loadData()
  } catch { /* cancel */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此商户？', '警告', { type: 'warning' })
    await deleteMerchant(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch { /* cancel */ }
}

function goToAccount(row) {
  router.push({ name: 'MerchantAccount', params: { merchantId: row.id }, query: { merchantName: row.name } })
}

function enterMerchantView(row) {
  userStore.enterMerchantView(row)
  ElMessage.success(`已进入商户 ${row.name} 的商户视角`)
  router.push('/merchant/dashboard')
}

function copyKey(key) {
  navigator.clipboard.writeText(key).then(() => ElMessage.success('已复制'))
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
  width: 32px;
  height: 32px;
  border-radius: var(--ep-radius-sm);
  background: var(--ep-chip-bg);
  color: var(--ep-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.font-mono {
  font-family: 'Geist Mono', 'SF Mono', 'Consolas', monospace;
}

.key-text {
  font-size: 12px;
  color: var(--ep-text-secondary);
}

.key-text.hidden {
  color: var(--ep-text-muted);
  letter-spacing: 2px;
}
</style>

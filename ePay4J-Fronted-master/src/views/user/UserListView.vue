<template>
  <PageContainer title="用户管理" desc="管理系统用户账号和权限">
    <template #actions>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </template>

    <!-- Admin Card -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title-row">
          <div class="section-icon admin-icon">
            <el-icon :size="18"><Setting /></el-icon>
          </div>
          <span class="section-title">管理员</span>
          <el-tag type="danger" size="small" round class="count-tag">{{ adminList.length }}</el-tag>
        </div>
      </div>
      <el-table :data="adminList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60">
          <template #default="{ row }">
            <span class="id-text">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="140">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:10px">
              <el-avatar :size="30" class="admin-avatar">{{ row.username.charAt(0).toUpperCase() }}</el-avatar>
              <span style="font-weight:500">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="130">
          <template #default="{ row }">
            <span style="color:var(--ep-text-secondary)">{{ row.nickname || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="140">
          <template #default="{ row }">
            <el-tag
              :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'warning'"
              size="small"
              round
            >
              {{ roleMap[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--ep-text-secondary)">{{ formatDate(row.lastLoginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.role === 'SUPER_ADMIN'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Merchant Card -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title-row">
          <div class="section-icon merchant-icon">
            <el-icon :size="18"><Shop /></el-icon>
          </div>
          <span class="section-title">商户</span>
          <el-tag type="success" size="small" round class="count-tag">{{ merchantList.length }}</el-tag>
        </div>
      </div>
      <el-table :data="merchantList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60">
          <template #default="{ row }">
            <span class="id-text">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="140">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:10px">
              <el-avatar :size="30" class="merchant-avatar">{{ row.username.charAt(0).toUpperCase() }}</el-avatar>
              <span style="font-weight:500">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="130">
          <template #default="{ row }">
            <span style="color:var(--ep-text-secondary)">{{ row.nickname || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--ep-text-secondary)">{{ formatDate(row.lastLoginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="可选" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="超级管理员" value="SUPER_ADMIN">
              <div style="display:flex;align-items:center;gap:8px">
                <span style="color:var(--ep-danger);font-size:16px">●</span>
                <span>超级管理员</span>
                <span style="color:var(--ep-text-muted);font-size:12px">拥有所有权限</span>
              </div>
            </el-option>
            <el-option label="管理员" value="ADMIN">
              <div style="display:flex;align-items:center;gap:8px">
                <span style="color:var(--ep-warning);font-size:16px">●</span>
                <span>管理员</span>
                <span style="color:var(--ep-text-muted);font-size:12px">后台管理权限</span>
              </div>
            </el-option>
            <el-option label="商户" value="MERCHANT">
              <div style="display:flex;align-items:center;gap:8px">
                <span style="color:var(--ep-success);font-size:16px">●</span>
                <span>商户</span>
                <span style="color:var(--ep-text-muted);font-size:12px">商户后台登录</span>
              </div>
            </el-option>
          </el-select>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { getUserPage, createUser, updateUser, updateUserStatus, resetPassword, deleteUser } from '../../api/user'
import { formatDate } from '../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'

const roleMap = { SUPER_ADMIN: '超级管理员', ADMIN: '管理员', MERCHANT: '商户' }
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const adminList = computed(() => tableData.value.filter(u => u.role === 'SUPER_ADMIN' || u.role === 'ADMIN'))
const merchantList = computed(() => tableData.value.filter(u => u.role === 'MERCHANT'))

const form = reactive({ username: '', password: '', nickname: '', role: 'MERCHANT' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage({ page: 1, size: 100 })
    tableData.value = res.data.records
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editingId.value = row?.id || null
  form.username = row?.username || ''
  form.password = ''
  form.nickname = row?.nickname || ''
  form.role = row?.role || 'ADMIN'
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, form)
    } else {
      await createUser(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  await updateUserStatus(row.id, row.status)
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

async function handleResetPwd(row) {
  try {
    await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码至少6位',
    }).then(async ({ value }) => {
      await resetPassword(row.id, value)
      ElMessage.success('密码已重置')
    })
  } catch { /* cancel */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此用户？', '警告', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch { /* cancel */ }
}
</script>

<style scoped>
.section-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 20px;
  margin-bottom: 16px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--ep-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-icon.admin-icon {
  background: var(--ep-danger-bg);
  color: var(--ep-danger);
}

.section-icon.merchant-icon {
  background: var(--ep-success-bg);
  color: var(--ep-success);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ep-text-primary);
}

.count-tag {
  margin-left: 2px;
}

.id-text {
  font-family: 'Geist Mono', monospace;
  color: var(--ep-text-secondary);
}

.admin-avatar {
  background: var(--ep-chip-bg) !important;
  color: var(--ep-text-secondary) !important;
  font-weight: 600 !important;
  font-size: 12px !important;
  flex-shrink: 0;
}

.merchant-avatar {
  background: var(--ep-chip-bg) !important;
  color: var(--ep-text-secondary) !important;
  font-weight: 600 !important;
  font-size: 12px !important;
  flex-shrink: 0;
}
</style>

<template>
  <PageContainer title="账户设置" desc="维护您的账户信息">
    <div class="settings-card">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="100px" style="max-width:500px;margin-top:20px">
            <el-form-item label="商户名称" prop="name">
              <el-input v-model="infoForm.name" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="nickName">
              <el-input v-model="infoForm.nickName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="支付宝账号" prop="alipayAccount">
              <el-input v-model="infoForm.alipayAccount" placeholder="请输入支付宝账号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="infoLoading" @click="handleSaveInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <div v-if="userStore.isActingMerchantView" class="acting-tip">
          管理员商户视角下可维护商户基础资料，登录密码修改已禁用。
        </div>

        <!-- 修改密码 -->
        <el-tab-pane v-if="!userStore.isActingMerchantView" label="修改密码" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width:500px;margin-top:20px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../store/modules/user'
import { updateMerchantInfo, updateMerchantPassword, getMerchantInfo } from '../../api/auth'
import { ElMessage } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'

const userStore = useUserStore()
const activeTab = ref('info')

// Info form
const infoFormRef = ref()
const infoLoading = ref(false)
const infoForm = reactive({
  name: '',
  nickName: '',
  phone: '',
  alipayAccount: '',
})
const infoRules = {
  nickName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

// Password form
const pwdFormRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

onMounted(() => loadInfo())

async function loadInfo() {
  try {
    const res = await getMerchantInfo()
    const data = res.data
    infoForm.name = data.name || ''
    infoForm.nickName = data.nickName || ''
    infoForm.phone = data.phone || ''
    infoForm.alipayAccount = data.alipayAccount || ''
  } catch { /* ignore */ }
}

async function handleSaveInfo() {
  await infoFormRef.value.validate()
  infoLoading.value = true
  try {
    await updateMerchantInfo({
      alipayAccount: infoForm.alipayAccount,
      nickName: infoForm.nickName,
      phone: infoForm.phone,
    })
    ElMessage.success('保存成功')
    userStore.fetchMerchantInfo()
  } catch { /* ignore */ } finally {
    infoLoading.value = false
  }
}

async function handleChangePwd() {
  if (userStore.isActingMerchantView) {
    ElMessage.warning('管理员商户视角不能修改商户登录密码')
    return
  }
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await updateMerchantPassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch { /* ignore */ } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.settings-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 24px;
}
.acting-tip {
  margin: 4px 0 18px;
  padding: 10px 12px;
  border: 1px solid rgba(230, 162, 60, 0.32);
  border-radius: var(--ep-radius-sm);
  background: rgba(230, 162, 60, 0.08);
  color: #e6a23c;
  font-size: 13px;
}
</style>

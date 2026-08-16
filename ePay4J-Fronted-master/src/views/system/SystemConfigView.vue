<template>
  <PageContainer title="系统设置" desc="配置全局系统参数">
    <template #actions>
      <el-button type="primary" :loading="saving" @click="handleSave">
        <el-icon><Check /></el-icon>保存设置
      </el-button>
    </template>

    <div class="settings-card">
      <el-tabs v-model="activeGroup" class="settings-tabs">
        <el-tab-pane label="基本设置" name="general">
          <div class="tab-desc">站点基本信息和通用配置</div>
          <el-form label-width="150px" class="settings-form">
            <el-form-item v-for="item in groups.general" :key="item.configKey" :label="item.description || item.configKey">
              <el-input v-model="item.configValue" :placeholder="`请输入${item.description || item.configKey}`" />
            </el-form-item>
            <div v-if="!groups.general.length" class="empty-hint">暂无配置项</div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="支付设置" name="payment">
          <div class="tab-desc">支付相关的全局配置参数</div>
          <el-form label-width="150px" class="settings-form">
            <el-form-item v-for="item in groups.payment" :key="item.configKey" :label="item.description || item.configKey">
              <el-input v-model="item.configValue" :placeholder="`请输入${item.description || item.configKey}`" />
            </el-form-item>
            <div v-if="!groups.payment.length" class="empty-hint">暂无配置项</div>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getConfig, updateConfig } from '../../api/system'
import { ElMessage } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'

const activeGroup = ref('general')
const groups = reactive({ general: [], payment: [] })
const saving = ref(false)

onMounted(async () => {
  const res = await getConfig()
  const items = (res.data || []).filter(i => i.configGroup !== 'notification')
  groups.general = items.filter(i => i.configGroup === 'general')
  groups.payment = items.filter(i => i.configGroup === 'payment')
})

async function handleSave() {
  saving.value = true
  try {
    await updateConfig([...groups.general, ...groups.payment])
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 4px 24px 24px;
}

.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.settings-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.tab-desc {
  font-size: 13px;
  color: var(--ep-text-secondary);
  padding: 12px 0 20px;
  border-bottom: 1px solid var(--ep-border);
  margin-bottom: 20px;
}

.settings-form {
  max-width: 600px;
}

.empty-hint {
  text-align: center;
  padding: 40px 0;
  color: var(--ep-text-muted);
  font-size: 14px;
}
</style>

<template>
  <div class="page-container">
    <div class="page-header" v-if="title || $slots.header">
      <div class="header-left">
        <slot name="header">
          <h2 class="page-title">{{ title }}</h2>
          <p class="page-desc" v-if="desc">{{ desc }}</p>
        </slot>
      </div>
      <div class="header-actions" v-if="$slots.actions">
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: String,
  desc: String,
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--ep-text-primary);
  margin: 0;
}

.page-desc {
  font-size: 13px;
  color: var(--ep-text-secondary);
  margin: 0;
}
</style>

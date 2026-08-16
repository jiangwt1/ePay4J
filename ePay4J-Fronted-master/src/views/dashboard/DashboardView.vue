<template>
  <div class="dashboard-page">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div
        v-for="(item, index) in statsCards"
        :key="item.label"
        class="stat-card"
        :style="{ animationDelay: index * 0.08 + 's' }"
      >
        <div class="stat-card-inner">
          <div class="stat-icon-wrap">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">{{ item.label }}</span>
            <span class="stat-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-grid">
      <!-- Revenue Chart -->
      <div class="chart-card fade-in-up" style="animation-delay:0.2s">
        <div class="chart-header">
          <div class="chart-title-group">
            <h3 class="chart-title">收入趋势</h3>
            <span class="chart-subtitle">近7天</span>
          </div>
          <div class="chart-actions">
            <el-radio-group v-model="chartDays" size="small" @change="refreshRevenueChart">
              <el-radio-button :value="7">7天</el-radio-button>
              <el-radio-button :value="14">14天</el-radio-button>
              <el-radio-button :value="30">30天</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div ref="revenueChartRef" class="chart-body"></div>
      </div>

      <!-- Order Status Chart -->
      <div class="chart-card fade-in-up" style="animation-delay:0.3s">
        <div class="chart-header">
          <div class="chart-title-group">
            <h3 class="chart-title">订单状态分布</h3>
            <span class="chart-subtitle">全部订单</span>
          </div>
        </div>
        <div ref="statusChartRef" class="chart-body"></div>
      </div>
    </div>

    <!-- Recent Orders -->
    <div class="recent-card fade-in-up" style="animation-delay:0.4s">
      <div class="chart-header">
        <div class="chart-title-group">
          <h3 class="chart-title">最近订单</h3>
          <span class="chart-subtitle">最新交易记录</span>
        </div>
        <el-button type="primary" link @click="$router.push('/admin/orders')">
          查看全部
          <el-icon style="margin-left:4px"><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentOrders" v-loading="recentLoading" style="width:100%">
        <el-table-column prop="outTradeNo" label="订单号" min-width="200">
          <template #default="{ row }">
            <span class="font-mono" style="font-size:12px">{{ row.outTradeNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="商品" min-width="140" />
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">
            <span style="font-weight:600;font-family:'Geist Mono',monospace">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)" size="small" round>{{ getOrderStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">
            <span style="color:var(--ep-text-secondary);font-size:13px">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getStats, getRevenueChart, getOrderStatusChart, getRecentOrders } from '../../api/dashboard'
import { formatMoney, formatDate, ORDER_STATUS } from '../../utils/format'

function getOrderStatusType(status) {
  return (ORDER_STATUS[status] || {}).type
}

function getOrderStatusLabel(status) {
  return (ORDER_STATUS[status] || {}).label
}

const revenueChartRef = ref()
const statusChartRef = ref()
const chartDays = ref(7)
const recentOrders = ref([])
const recentLoading = ref(false)

const stats = ref({ todayRevenue: '0.00', todayOrders: 0, successRate: '0%', totalMerchants: 0 })

const statsCards = computed(() => [
  { label: '今日收入', value: '¥' + stats.value.todayRevenue, icon: 'Wallet' },
  { label: '今日订单', value: stats.value.todayOrders, icon: 'Document' },
  { label: '支付成功率', value: stats.value.successRate, icon: 'TrendCharts' },
  { label: '商户总数', value: stats.value.totalMerchants, icon: 'Shop' },
])

let revenueChart = null
let statusChart = null

onMounted(async () => {
  try {
    const res = await getStats()
    stats.value = res.data
  } catch { /* ignore */ }

  initRevenueChart()
  initStatusChart()
  loadRecentOrders()

  window.addEventListener('resize', handleResize)
})

function handleResize() {
  revenueChart?.resize()
  statusChart?.resize()
}

async function loadRecentOrders() {
  recentLoading.value = true
  try {
    const res = await getRecentOrders(8)
    recentOrders.value = res.data || []
  } finally {
    recentLoading.value = false
  }
}

async function refreshRevenueChart() {
  initRevenueChart()
}

async function initRevenueChart() {
  if (!revenueChartRef.value) return
  if (revenueChart) revenueChart.dispose()
  revenueChart = echarts.init(revenueChartRef.value)

  try {
    const res = await getRevenueChart(chartDays.value)
    const data = res.data || []
    const values = data.map(d => Number(d.revenue) || 0)

    revenueChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#1c1c1c',
        borderColor: 'rgba(255,255,255,0.06)',
        borderWidth: 1,
        textStyle: { color: '#e8e8e8', fontSize: 13 },
        axisPointer: {
          type: 'line',
          lineStyle: { color: 'rgba(255,255,255,0.1)', type: 'dashed' },
        },
        formatter: params => {
          const p = params[0]
          return `<div style="font-weight:600;margin-bottom:4px">${p.axisValue}</div>
                  <div style="color:#e8e8e8;font-weight:700;font-size:16px">¥${Number(p.value).toFixed(2)}</div>`
        },
      },
      grid: { left: 60, right: 20, top: 20, bottom: 35 },
      xAxis: {
        type: 'category',
        data: data.map(d => d.date),
        boundaryGap: false,
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
        axisTick: { show: false },
        axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 12 },
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)', type: 'dashed' } },
        axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 12, formatter: v => '¥' + v },
        min: 0,
        minInterval: 0.01,
        scale: true,
      },
      series: [{
        data: values,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { width: 2, color: '#888888' },
        itemStyle: { color: '#888888', borderWidth: 2, borderColor: '#1c1c1c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 255, 255, 0.06)' },
            { offset: 1, color: 'rgba(255, 255, 255, 0.01)' },
          ]),
        },
      }],
    })
  } catch { /* ignore */ }
}

async function initStatusChart() {
  if (!statusChartRef.value) return
  if (statusChart) statusChart.dispose()
  statusChart = echarts.init(statusChartRef.value)

  const statusMap = { '0': '待支付', '1': '已支付', '2': '已关闭', '3': '已退款' }
  const colorMap = { '0': '#a08a50', '1': '#5a9a6a', '2': '#555555', '3': '#a05050' }

  try {
    const res = await getOrderStatusChart()
    const data = res.data || []
    statusChart.setOption({
      tooltip: {
        backgroundColor: '#1c1c1c',
        borderColor: 'rgba(255,255,255,0.06)',
        borderWidth: 1,
        textStyle: { color: '#e8e8e8', fontSize: 13 },
      },
      series: [{
        type: 'pie',
        radius: ['52%', '78%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#161616', borderWidth: 3 },
        label: {
          show: true,
          formatter: '{b}\n{c} 笔',
          color: 'rgba(255,255,255,0.4)',
          fontSize: 12,
          lineHeight: 18,
        },
        labelLine: { length: 12, length2: 16, smooth: true },
        emphasis: {
          label: { fontSize: 14, fontWeight: 'bold' },
        },
        data: data.map(d => ({
          name: statusMap[d.status] || d.status,
          value: d.count,
          itemStyle: { color: colorMap[d.status] },
        })),
      }],
    })
  } catch { /* ignore */ }
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== Stats Grid ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  animation: fadeInUp 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.stat-card-inner {
  position: relative;
  padding: 20px;
  border-radius: var(--ep-radius);
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  transition: border-color 0.15s ease;
}

.stat-card-inner:hover {
  border-color: var(--ep-border-active);
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--ep-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.04);
  color: var(--ep-text-muted);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--ep-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  font-family: 'Geist Mono', 'JetBrains Mono', monospace;
  color: var(--ep-text-primary);
}

/* ===== Charts Grid ===== */
.charts-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
}

.chart-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  overflow: hidden;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 0;
}

.chart-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ep-text-primary);
  margin: 0;
}

.chart-subtitle {
  font-size: 12px;
  color: var(--ep-text-muted);
}

.chart-actions {
  display: flex;
  align-items: center;
}

.chart-body {
  height: 320px;
  padding: 0 12px 12px;
}

/* ===== Recent Orders ===== */
.recent-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 0 20px 16px;
  overflow: hidden;
}

.recent-card .chart-header {
  padding: 18px 0 12px;
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}

/* ===== Animation ===== */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

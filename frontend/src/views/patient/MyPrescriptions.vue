<template>
  <div class="my-prescriptions page-container">
    <h2 class="page-title">我的处方</h2>

    <app-loading :visible="presStore.loading" />
    <div v-if="!presStore.loading && presStore.records.length === 0">
      <app-empty description="暂无处方记录" />
    </div>
    <div v-else class="prescription-list">
      <el-card v-for="item in presStore.records" :key="item.id" class="prescription-card">
        <div class="prescription-header">
          <span>处方单 #{{ item.id }}</span>
          <el-tag :type="getStatusTag(item.status)">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="医生">{{ item.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ item.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="开具时间">{{ formatDateTime(item.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="drug-list">
          <h4>药品明细</h4>
          <el-table :data="item.items" size="small" border>
            <el-table-column prop="drugName" label="药品名称" min-width="140" />
            <el-table-column prop="dosage" label="用量" width="100" />
            <el-table-column prop="frequency" label="频次" width="100" />
            <el-table-column prop="duration" label="疗程" width="100" />
            <el-table-column prop="note" label="备注" min-width="120" />
          </el-table>
        </div>
        <div v-if="item.aiCheckResult" class="ai-result" style="margin-top:12px;">
          <div class="ai-result-title">AI 审核结果</div>
          <p>{{ item.aiCheckResult }}</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue"
import { usePrescriptionStore } from "@/stores/prescription"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"

const presStore = usePrescriptionStore()
const userStore = useUserStore()

onMounted(async () => {
  if (userStore.userId) {
    await presStore.fetchList({ patientId: userStore.userId })
  }
})
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.prescription-list { display: flex; flex-direction: column; gap: 16px; }
.prescription-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 16px; font-weight: 600; }
.drug-list { margin-top: 12px; }
.drug-list h4 { margin-bottom: 8px; color: #606266; }
</style>

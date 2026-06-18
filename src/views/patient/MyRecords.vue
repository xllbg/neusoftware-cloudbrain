<template>
  <div class="my-records page-container">
    <h2 class="page-title">电子病历</h2>

    <app-loading :visible="loading" />
    <div v-if="!loading && records.length === 0">
      <app-empty description="暂无病历记录" />
    </div>
    <div v-else class="record-list">
      <el-card v-for="record in records" :key="record.id" class="record-card">
        <div class="record-header">
          <span>病历 #{{ record.id }}</span>
          <el-tag v-if="record.aiGenerated" type="warning">AI 生成</el-tag>
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="医生">{{ record.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ record.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="主诉">{{ record.chiefComplaint }}</el-descriptions-item>
          <el-descriptions-item label="现病史" :span="2">{{ record.presentIllness }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 8px; text-align: right;">
          <el-button size="small" @click="showDetail(record)">查看完整病历</el-button>
        </div>
      </el-card>
    </div>

    <!-- 病历详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="病历详情" width="700px">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="患者">{{ currentRecord.patientName }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ currentRecord.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="主诉">{{ currentRecord.chiefComplaint }}</el-descriptions-item>
        <el-descriptions-item label="现病史">{{ currentRecord.presentIllness }}</el-descriptions-item>
        <el-descriptions-item label="既往史">{{ currentRecord.pastHistory || "无" }}</el-descriptions-item>
        <el-descriptions-item label="检查结果">{{ currentRecord.examinationResult || "无" }}</el-descriptions-item>
        <el-descriptions-item label="诊断">{{ currentRecord.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="治疗方案">{{ currentRecord.treatmentPlan || "无" }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useUserStore } from "@/stores/user"
import { formatDateTime } from "@/utils/format"
import type { MedicalRecord } from "@/types"

const recordStore = useMedicalRecordStore()
const userStore = useUserStore()
const loading = ref(false)
const records = ref<MedicalRecord[]>([])
const currentRecord = ref<MedicalRecord | null>(null)
const dialogVisible = ref(false)

onMounted(async () => {
  if (!userStore.userId) return
  loading.value = true
  try {
    const res = await recordStore.fetchList({ patientId: userStore.userId })
    records.value = res || []
  } finally {
    loading.value = false
  }
})

function showDetail(record: MedicalRecord) {
  currentRecord.value = record
  dialogVisible.value = true
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.record-list { display: flex; flex-direction: column; gap: 16px; }
.record-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 16px; font-weight: 600; }
</style>

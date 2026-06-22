<template>
  <div class="history-page page-container">
    <h2 class="page-title">历史记录</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="处方记录" name="prescription">
        <app-loading :visible="presLoading" />
        <div v-if="!presLoading && prescriptions.length === 0">
          <app-empty description="暂无处方记录" />
        </div>
        <el-table v-else :data="prescriptions" stripe border>
          <el-table-column prop="id" label="处方号" width="80" />
          <el-table-column prop="patientName" label="患者" width="100" />
          <el-table-column prop="diagnosis" label="诊断" min-width="150" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="开具时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="病历记录" name="record">
        <app-loading :visible="recLoading" />
        <div v-if="!recLoading && medicalRecords.length === 0">
          <app-empty description="暂无病历记录" />
        </div>
        <el-table v-else :data="medicalRecords" stripe border>
          <el-table-column prop="id" label="病历号" width="80" />
          <el-table-column prop="patientName" label="患者" width="100" />
          <el-table-column prop="diagnosis" label="诊断" min-width="150" show-overflow-tooltip />
          <el-table-column label="来源" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.aiGenerated" type="warning">AI生成</el-tag>
              <el-tag v-else type="info">手动</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="viewRecord(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 病历详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="病历详情" width="700px">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="患者">{{ currentRecord.patientName }}</el-descriptions-item>
        <el-descriptions-item label="主诉">{{ currentRecord.chiefComplaint }}</el-descriptions-item>
        <el-descriptions-item label="现病史">{{ currentRecord.presentIllness }}</el-descriptions-item>
        <el-descriptions-item label="诊断">{{ currentRecord.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="治疗方案">{{ currentRecord.treatmentPlan || "无" }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { usePrescriptionStore } from "@/stores/prescription"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"
import type { PrescriptionRecord, MedicalRecord } from "@/types"

const presStore = usePrescriptionStore()
const recordStore = useMedicalRecordStore()
const userStore = useUserStore()

const activeTab = ref("prescription")
const presLoading = ref(false)
const recLoading = ref(false)
const prescriptions = ref<PrescriptionRecord[]>([])
const medicalRecords = ref<MedicalRecord[]>([])
const dialogVisible = ref(false)
const currentRecord = ref<MedicalRecord | null>(null)

onMounted(async () => {
  if (!userStore.userId) return
  presLoading.value = true
  try {
    const res = await presStore.fetchList({ doctorId: userStore.userId })
    prescriptions.value = res || []
  } finally { presLoading.value = false }

  recLoading.value = true
  try {
    const res = await recordStore.fetchList({ doctorId: userStore.userId })
    medicalRecords.value = res || []
  } finally { recLoading.value = false }
})

function viewRecord(record: MedicalRecord) {
  currentRecord.value = record
  dialogVisible.value = true
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
</style>

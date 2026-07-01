<template>
  <div class="my-registrations page-container">
    <h2 class="page-title">我的挂号记录</h2>

    <app-loading :visible="loading" />
    <div v-if="!loading && records.length === 0">
      <app-empty description="暂无挂号记录" :show-action="true" action-text="去挂号" @action="router.push('/patient/registration')" />
    </div>
    <div v-else class="record-list">
      <el-card v-for="record in records" :key="record.id" class="record-card">
        <div class="record-header">
          <span class="record-doctor">{{ record.doctorName || '急诊科' }}</span>
          <el-tag :type="getStatusTag(record.status)">{{ getStatusLabel(record.status) }}</el-tag>
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="科室">{{ record.department }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ record.registrationDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ record.timeSlot === "MORNING" ? "上午" : "下午" }}</el-descriptions-item>
          <el-descriptions-item label="症状">{{ record.symptom || "无" }}</el-descriptions-item>
        </el-descriptions>
        <div class="record-actions" v-if="record.status === 'pending'">
          <el-button type="danger" size="small" @click="handleCancel(record.id)">取消挂号</el-button>
        </div>
        <div class="record-actions" v-if="record.status === 'completed'">
          <el-button size="small" @click="handleViewMedicalRecord(record.id)">查看病历</el-button>
          <el-button size="small" type="primary" @click="handleViewPrescription(record.id)">查看处方</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="medicalRecordDialogVisible" title="病历详情" width="700px">
      <el-descriptions :column="1" border v-if="currentMedicalRecord">
        <el-descriptions-item label="患者">{{ currentMedicalRecord.patientName }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ currentMedicalRecord.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="主诉">{{ currentMedicalRecord.chiefComplaint }}</el-descriptions-item>
        <el-descriptions-item label="现病史">{{ currentMedicalRecord.presentIllness }}</el-descriptions-item>
        <el-descriptions-item label="既往史">{{ currentMedicalRecord.pastHistory || "无" }}</el-descriptions-item>
        <el-descriptions-item label="检查结果">{{ currentMedicalRecord.examinationResult || "无" }}</el-descriptions-item>
        <el-descriptions-item label="诊断">{{ currentMedicalRecord.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="治疗方案">{{ currentMedicalRecord.treatmentPlan || "无" }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentMedicalRecord.createTime || currentMedicalRecord.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无病历记录" />
    </el-dialog>

    <el-dialog v-model="prescriptionDialogVisible" title="处方详情" width="700px">
      <div v-if="currentPrescription">
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="医生">{{ currentPrescription.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ currentPrescription.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="开具时间" :span="2">{{ formatDateTime(currentPrescription.createTime || currentPrescription.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        <div class="drug-list">
          <h4>药品明细</h4>
          <el-table :data="parsedMedicineList" size="small" border>
            <el-table-column prop="name" label="药品名称" min-width="140" />
            <el-table-column prop="dose" label="用量" width="100" />
            <el-table-column prop="frequency" label="频次" width="100" />
          </el-table>
        </div>
        <div v-if="currentPrescription.usage" style="margin-top: 12px;">
          <div style="font-weight: 600; margin-bottom: 6px; color: #606266;">用药说明</div>
          <p>{{ currentPrescription.usage }}</p>
        </div>
        <div v-if="currentPrescription.aiCheckResult" class="ai-result" style="margin-top:12px;">
          <div class="ai-result-title">AI 审核结果</div>
          <p>{{ currentPrescription.aiCheckResult.checkResult }}</p>
        </div>
      </div>
      <el-empty v-else description="暂无处方记录" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { usePrescriptionStore } from "@/stores/prescription"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"
import { storeToRefs } from "pinia"
import type { MedicalRecord, PrescriptionRecord } from "@/types"

const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()
const medicalRecordStore = useMedicalRecordStore()
const prescriptionStore = usePrescriptionStore()
const { records, loading } = storeToRefs(regStore)

const medicalRecordDialogVisible = ref(false)
const prescriptionDialogVisible = ref(false)
const currentMedicalRecord = ref<MedicalRecord | null>(null)
const currentPrescription = ref<PrescriptionRecord | null>(null)
const parsedMedicineList = ref<any[]>([])

async function loadData() {
  if (!userStore.userId) return
  await regStore.fetchList({ patientId: userStore.userId })
}

async function handleCancel(id: number) {
  if (!userStore.userId) return
  try {
    await ElMessageBox.confirm("确定要取消该挂号吗？", "确认", { type: "warning" })
    await regStore.cancel(id, userStore.userId)
    ElMessage.success("已取消")
  } catch {
    // 用户取消对话框
  }
}

async function handleViewMedicalRecord(registrationId: number) {
  currentMedicalRecord.value = null
  medicalRecordDialogVisible.value = true
  const record = await medicalRecordStore.fetchByRegistration(registrationId)
  currentMedicalRecord.value = record || null
}

async function handleViewPrescription(registrationId: number) {
  currentPrescription.value = null
  parsedMedicineList.value = []
  prescriptionDialogVisible.value = true
  const prescription = await prescriptionStore.fetchByRegistration(registrationId)
  currentPrescription.value = prescription || null
  if (prescription && prescription.medicineList) {
    try {
      parsedMedicineList.value = JSON.parse(prescription.medicineList)
    } catch (e) {
      parsedMedicineList.value = []
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.record-list { display: flex; flex-direction: column; gap: 16px; }
.record-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.record-doctor { font-size: 16px; font-weight: 600; color: #303133; }
.record-actions { margin-top: 12px; display: flex; justify-content: flex-end; gap: 8px; }
.drug-list { margin-top: 12px; }
.drug-list h4 { margin-bottom: 8px; color: #606266; }
.ai-result-title { font-weight: 600; margin-bottom: 6px; color: #606266; }
</style>

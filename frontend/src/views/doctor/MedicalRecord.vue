<template>
  <div class="medical-record-page page-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">电子病历</h2>
      <div class="header-actions">
        <el-button type="primary" @click="saveRecord" :loading="saving">
          <el-icon><Check /></el-icon>
          保存病历
        </el-button>
      </div>
    </div>

    <el-card class="page-card patient-info-card">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="患者">{{ patientName }}</el-descriptions-item>
        <el-descriptions-item label="患者ID">{{ patientId }}</el-descriptions-item>
        <el-descriptions-item label="挂号ID">{{ registrationId || "-" }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ doctorName }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="page-card info-tip-card" v-if="fromConsultation">
      <div class="tip-content">
        <el-icon><InfoFilled /></el-icon>
        <span>病历内容已从问诊记录同步，请检查并修改后保存</span>
      </div>
    </el-card>

    <el-card class="page-card">
      <template #header>
        <span>病历详情</span>
      </template>

      <el-form :model="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主诉">
              <el-input
                v-model="form.chiefComplaint"
                type="textarea"
                :rows="2"
                placeholder="请输入患者主诉"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现病史">
              <el-input
                v-model="form.presentIllness"
                type="textarea"
                :rows="2"
                placeholder="请输入现病史"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="既往史">
              <el-input
                v-model="form.pastHistory"
                type="textarea"
                :rows="2"
                placeholder="请输入既往史"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体格检查">
              <el-input
                v-model="form.physicalExamination"
                type="textarea"
                :rows="2"
                placeholder="请输入体格检查结果"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="诊断">
              <el-input
                v-model="form.diagnosis"
                type="textarea"
                :rows="2"
                placeholder="请输入诊断结果"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="治疗方案">
              <el-input
                v-model="form.treatmentPlan"
                type="textarea"
                :rows="2"
                placeholder="请输入治疗方案"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowLeft, Check, InfoFilled } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import type { MedicalRecordForm, RegistrationRecord } from "@/types"
import { getConsultationRecord } from "@/api/doctor"

const route = useRoute()
const router = useRouter()
const recordStore = useMedicalRecordStore()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const patientId = Number(route.params.patientId)
const registrationId = Number(route.query.registrationId) || 0
const patientName = ref("患者")
const doctorName = ref(userStore.userName || "医生")
const fromConsultation = ref(false)

const saving = ref(false)

const form = reactive<MedicalRecordForm>({
  patientId,
  doctorId: userStore.userId,
  registrationId: registrationId || undefined,
  chiefComplaint: "",
  presentIllness: "",
  pastHistory: "",
  physicalExamination: "",
  diagnosis: "",
  treatmentPlan: "",
})

onMounted(async () => {
  if (userStore.userName) {
    doctorName.value = userStore.userName
  }
  await loadPatientInfo()
  await loadConsultationRecord()
})

async function loadPatientInfo() {
  if (!registrationId || !userStore.userId) return

  const regs = await regStore.fetchList({ doctorId: userStore.userId })
  const reg = (regs || []).find((r: RegistrationRecord) => r.id === registrationId)
  if (reg) {
    patientName.value = reg.patientName
  }
}

async function loadConsultationRecord() {
  if (!registrationId) return

  try {
    const res = await getConsultationRecord(registrationId)
    if (res.code === 200 && res.data) {
      const data = res.data
      // 从问诊记录填充病历
      if (data.presentIllness) form.presentIllness = data.presentIllness
      if (data.pastHistory) form.pastHistory = data.pastHistory
      if (data.physicalExamination) form.physicalExamination = data.physicalExamination
      if (data.diagnosis) form.diagnosis = data.diagnosis
      if (data.chiefComplaint) form.chiefComplaint = data.chiefComplaint
      if (data.treatmentPlan) form.treatmentPlan = data.treatmentPlan

      // 只有当问诊记录有数据时才显示提示
      if (form.presentIllness || form.diagnosis) {
        fromConsultation.value = true
      }
    }
  } catch (e) {
    console.error("加载问诊记录失败", e)
  }
}

async function saveRecord() {
  if (!form.diagnosis.trim()) {
    ElMessage.warning("请填写诊断结果")
    return
  }

  saving.value = true
  try {
    form.patientId = patientId
    form.doctorId = userStore.userId
    form.registrationId = registrationId || undefined
    await recordStore.save(form)
    ElMessage.success("病历保存成功")
    router.back()
  } catch (e: any) {
    ElMessage.error(e?.message || "保存失败")
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  font-size: 22px;
  color: #303133;
  margin: 0;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}
.header-actions {
  display: flex;
  gap: 8px;
}
.patient-info-card {
  margin-bottom: 12px;
}
.info-tip-card {
  margin-bottom: 12px;
  background: #ecf5ff;
  border-color: #409eff;
}
.tip-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-size: 14px;
}
</style>

<template>
  <div class="medical-record-page page-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">电子病历</h2>
      <div class="header-actions">
        <el-button @click="generateRecord" :loading="generating">
          <el-icon><MagicStick /></el-icon>
          AI一键生成
        </el-button>
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
import { ArrowLeft, MagicStick, Check } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useConsultationStore } from "@/stores/consultation"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import type { MedicalRecordForm, ConsultationMessage, RegistrationRecord } from "@/types"

const route = useRoute()
const router = useRouter()
const recordStore = useMedicalRecordStore()
const consultStore = useConsultationStore()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const patientId = Number(route.params.patientId)
const registrationId = Number(route.query.registrationId) || 0
const patientName = ref("患者")
const doctorName = ref(userStore.userName || "医生")

const saving = ref(false)
const generating = ref(false)

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
})

async function loadPatientInfo() {
  if (!registrationId || !userStore.userId) return

  const regs = await regStore.fetchList({ doctorId: userStore.userId })
  const reg = (regs || []).find((r: RegistrationRecord) => r.id === registrationId)
  if (reg) {
    patientName.value = reg.patientName
    if (reg.symptom) {
      form.chiefComplaint = reg.symptom
    }
    if (reg.triageResult) {
      form.presentIllness = reg.triageResult
    }
  }
}

async function generateRecord() {
  if (!patientId) {
    ElMessage.warning("缺少患者信息")
    return
  }

  const dialogueText = await buildDialogueText()

  if (!dialogueText && !form.chiefComplaint) {
    ElMessage.warning("暂无对话记录或症状描述，无法生成病历")
    return
  }

  generating.value = true
  try {
    const result = await recordStore.generate(patientId, dialogueText)
    if (result) {
      if (result.presentIllness && !form.presentIllness) {
        form.presentIllness = result.presentIllness
      } else if (result.presentIllness) {
        form.presentIllness = result.presentIllness
      }
      if (result.pastHistory) form.pastHistory = result.pastHistory
      if (result.physicalExamination) form.physicalExamination = result.physicalExamination
      if (result.diagnosis) form.diagnosis = result.diagnosis
      if (result.treatmentPlan) form.treatmentPlan = result.treatmentPlan
    }
    ElMessage.success("AI生成成功，请检查并修改后保存")
  } catch (e: any) {
    ElMessage.error(e?.message || "生成失败")
  } finally {
    generating.value = false
  }
}

async function buildDialogueText(): Promise<string> {
  const parts: string[] = []

  if (form.chiefComplaint) {
    parts.push(`患者主诉：${form.chiefComplaint}`)
  }
  if (form.presentIllness) {
    parts.push(`分诊结果：${form.presentIllness}`)
  }

  if (registrationId) {
    try {
      const messages = await consultStore.fetchMessages(registrationId)
      if (messages && messages.length > 0) {
        parts.push("\n问诊对话：")
        messages.forEach((msg: ConsultationMessage) => {
          const role = msg.senderType === "DOCTOR" ? "医生" : "患者"
          parts.push(`${role}：${msg.content}`)
        })
      }
    } catch {
      // ignore
    }
  }

  return parts.join("\n")
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
  margin-bottom: 16px;
}
</style>

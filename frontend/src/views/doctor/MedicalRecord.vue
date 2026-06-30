<template>
  <div class="medical-record-page page-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">电子病历</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleAiOptimize" :loading="aiOptimizing">
          <el-icon><MagicStick /></el-icon>
          AI优化病历
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

    <el-card class="page-card info-tip-card" v-if="fromConsultation">
      <div class="tip-content">
        <el-icon><InfoFilled /></el-icon>
        <span>病历内容已从问诊记录同步，点击"AI优化病历"可生成规范病历</span>
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
              <!-- AI推荐结果 -->
              <div v-if="aiResults.chiefComplaint && !aiResultsTaken.chiefComplaint" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI优化</span>
                </div>
                <div class="ai-result-content">{{ aiResults.chiefComplaint }}</div>
                <div class="ai-result-actions">
                  <el-button type="primary" size="small" @click="takeAiResult('chiefComplaint')">
                    采纳
                  </el-button>
                </div>
              </div>
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
              <!-- AI推荐结果 -->
              <div v-if="aiResults.presentIllness && !aiResultsTaken.presentIllness" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI优化</span>
                </div>
                <div class="ai-result-content">{{ aiResults.presentIllness }}</div>
                <div class="ai-result-actions">
                  <el-button type="primary" size="small" @click="takeAiResult('presentIllness')">
                    采纳
                  </el-button>
                </div>
              </div>
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
              <!-- AI推荐结果 -->
              <div v-if="aiResults.pastHistory && !aiResultsTaken.pastHistory" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI优化</span>
                </div>
                <div class="ai-result-content">{{ aiResults.pastHistory }}</div>
                <div class="ai-result-actions">
                  <el-button type="primary" size="small" @click="takeAiResult('pastHistory')">
                    采纳
                  </el-button>
                </div>
              </div>
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
              <!-- AI推荐结果 -->
              <div v-if="aiResults.physicalExamination && !aiResultsTaken.physicalExamination" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI优化</span>
                </div>
                <div class="ai-result-content">{{ aiResults.physicalExamination }}</div>
                <div class="ai-result-actions">
                  <el-button type="primary" size="small" @click="takeAiResult('physicalExamination')">
                    采纳
                  </el-button>
                </div>
              </div>
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
              <!-- AI推荐结果 -->
              <div v-if="aiResults.diagnosis && !aiResultsTaken.diagnosis" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI优化</span>
                </div>
                <div class="ai-result-content">{{ aiResults.diagnosis }}</div>
                <div class="ai-result-actions">
                  <el-button type="primary" size="small" @click="takeAiResult('diagnosis')">
                    采纳
                  </el-button>
                </div>
              </div>
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
              <!-- AI推荐结果 -->
              <div v-if="aiResults.treatmentPlan && !aiResultsTaken.treatmentPlan" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI优化</span>
                </div>
                <div class="ai-result-content">{{ aiResults.treatmentPlan }}</div>
                <div class="ai-result-actions">
                  <el-button type="primary" size="small" @click="takeAiResult('treatmentPlan')">
                    采纳
                  </el-button>
                </div>
              </div>
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
import { ArrowLeft, Check, InfoFilled, MagicStick } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import type { MedicalRecordForm, RegistrationRecord } from "@/types"
import { getConsultationRecord } from "@/api/doctor"
import { optimizeMedicalRecord } from "@/api/medicalRecord"

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
const aiOptimizing = ref(false)

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

// AI优化结果
const aiResults = reactive({
  chiefComplaint: "",
  presentIllness: "",
  pastHistory: "",
  physicalExamination: "",
  diagnosis: "",
  treatmentPlan: "",
})

// AI结果是否已被采纳
const aiResultsTaken = reactive({
  chiefComplaint: false,
  presentIllness: false,
  pastHistory: false,
  physicalExamination: false,
  diagnosis: false,
  treatmentPlan: false,
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

async function handleAiOptimize() {
  const hasContent = form.chiefComplaint || form.presentIllness || form.diagnosis ||
    form.pastHistory || form.physicalExamination || form.treatmentPlan

  if (!hasContent) {
    ElMessage.warning("暂无问诊记录内容，无法进行AI优化")
    return
  }

  aiOptimizing.value = true
  try {
    const res = await optimizeMedicalRecord({
      chiefComplaint: form.chiefComplaint,
      presentIllness: form.presentIllness,
      pastHistory: form.pastHistory,
      physicalExamination: form.physicalExamination,
      diagnosis: form.diagnosis,
      treatmentPlan: form.treatmentPlan,
    })

    if (res.code === 200 && res.data) {
      const data = res.data

      // 清空之前的采纳状态
      Object.keys(aiResultsTaken).forEach(key => {
        (aiResultsTaken as any)[key] = false
      })

      // 填充AI优化结果（不清空输入框原有内容）
      if (data.chiefComplaint && data.chiefComplaint !== form.chiefComplaint) {
        aiResults.chiefComplaint = data.chiefComplaint
      }
      if (data.presentIllness && data.presentIllness !== form.presentIllness) {
        aiResults.presentIllness = data.presentIllness
      }
      if (data.pastHistory && data.pastHistory !== form.pastHistory) {
        aiResults.pastHistory = data.pastHistory
      }
      if (data.physicalExamination && data.physicalExamination !== form.physicalExamination) {
        aiResults.physicalExamination = data.physicalExamination
      }
      if (data.diagnosis && data.diagnosis !== form.diagnosis) {
        aiResults.diagnosis = data.diagnosis
      }
      if (data.treatmentPlan && data.treatmentPlan !== form.treatmentPlan) {
        aiResults.treatmentPlan = data.treatmentPlan
      }

      ElMessage.success("AI优化已完成，请查看并选择采纳")
    } else {
      ElMessage.error("AI优化失败")
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "AI优化失败")
  } finally {
    aiOptimizing.value = false
  }
}

// 采纳AI推荐结果
function takeAiResult(field: keyof typeof aiResults) {
  const value = aiResults[field]
  if (value) {
    (form as any)[field] = value
    aiResultsTaken[field] = true
    aiResults[field] = ""
    ElMessage.success("已采纳AI优化内容")
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

/* AI推荐结果样式 */
.ai-result-box {
  margin-top: 10px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
}
.ai-result-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #1890ff;
  font-weight: 500;
  margin-bottom: 8px;
}
.ai-result-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 10px;
}
.ai-result-actions {
  display: flex;
  justify-content: flex-end;
}
</style>

<template>
  <div class="consultation-page page-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <h2 class="page-title">问诊</h2>
      <div class="header-actions">
        <el-button type="warning" @click="completeConsultation" :disabled="record?.status === 'completed'">
          <el-icon><Check /></el-icon>
          完成问诊
        </el-button>
        <el-button type="primary" @click="goToMedicalRecord">
          <el-icon><Notebook /></el-icon>
          生成病历
        </el-button>
        <el-button type="success" @click="goToPrescription">
          <el-icon><Document /></el-icon>
          开具处方
        </el-button>
      </div>
    </div>

    <!-- 患者信息卡片 -->
    <el-card class="page-card patient-info-card">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="患者">{{ record?.patientName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ record?.department }}</el-descriptions-item>
        <el-descriptions-item label="挂号日期">{{ record?.registrationDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="getStatusTag(record?.status || '')">{{ getStatusLabel(record?.status || '') }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 患者自述与AI分诊 -->
    <el-card class="page-card info-highlight-card">
      <div class="highlight-section">
        <div class="section-item patient-symptom">
          <div class="section-label">
            <el-icon><Warning /></el-icon>
            <span>患者自述症状</span>
          </div>
          <div class="section-content">{{ record?.symptom || "无" }}</div>
        </div>
        <div class="section-item ai-triage" v-if="record?.triageResult">
          <div class="section-label">
            <el-icon><DataAnalysis /></elIcon>
            <span>AI分诊结果</span>
          </div>
          <div class="section-content">{{ record.triageResult }}</div>
        </div>
      </div>
    </el-card>

    <!-- 问诊信息表单 -->
    <el-card class="page-card consultation-form-card">
      <template #header>
        <div class="card-header">
          <span>问诊记录</span>
          <div class="header-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleAiRecommend"
              :loading="aiRecommending"
              :disabled="isCompleted || !record?.symptom"
            >
              <el-icon><MagicStick /></el-icon>
              AI推荐疗法
            </el-button>
            <el-button size="small" @click="resetForm" v-if="!isCompleted">重置</el-button>
          </div>
        </div>
      </template>

      <el-form :model="consultForm" label-width="100px" class="consult-form">
        <!-- 现病史 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="现病史">
              <el-input
                v-model="consultForm.presentIllness"
                type="textarea"
                :rows="3"
                placeholder="请详细描述患者本次发病经过、主要症状、伴随症状等"
                :disabled="isCompleted"
              />
              <!-- AI推荐结果 -->
              <div v-if="aiResults.presentIllness && !aiResultsTaken.presentIllness" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI推荐</span>
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

        <!-- 既往史 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="既往史">
              <el-input
                v-model="consultForm.pastHistory"
                type="textarea"
                :rows="2"
                placeholder="请描述患者既往病史、过敏史、手术史等"
                :disabled="isCompleted"
              />
              <!-- AI推荐结果 -->
              <div v-if="aiResults.pastHistory && !aiResultsTaken.pastHistory" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI推荐</span>
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
        </el-row>

        <!-- 体格检查 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="体格检查">
              <el-input
                v-model="consultForm.physicalExamination"
                type="textarea"
                :rows="2"
                placeholder="请描述体温、血压、心率等体格检查结果"
                :disabled="isCompleted"
              />
              <!-- AI推荐结果 -->
              <div v-if="aiResults.physicalExamination && !aiResultsTaken.physicalExamination" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI推荐</span>
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

        <el-divider />

        <!-- 初步诊断 + 主诉 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="初步诊断">
              <el-input
                v-model="consultForm.diagnosis"
                placeholder="请输入初步诊断"
                :disabled="isCompleted"
              />
              <!-- AI推荐结果 -->
              <div v-if="aiResults.diagnosis && !aiResultsTaken.diagnosis" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI推荐</span>
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
            <el-form-item label="主诉">
              <el-input
                v-model="consultForm.chiefComplaint"
                placeholder="请输入主诉"
                :disabled="isCompleted"
              />
              <!-- AI推荐结果 -->
              <div v-if="aiResults.chiefComplaint && !aiResultsTaken.chiefComplaint" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI推荐</span>
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
        </el-row>

        <!-- 治疗意见 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="治疗意见">
              <el-input
                v-model="consultForm.treatmentPlan"
                type="textarea"
                :rows="3"
                placeholder="请输入治疗意见、注意事项等"
                :disabled="isCompleted"
              />
              <!-- AI推荐结果 -->
              <div v-if="aiResults.treatmentPlan && !aiResultsTaken.treatmentPlan" class="ai-result-box">
                <div class="ai-result-label">
                  <el-icon><MagicStick /></el-icon>
                  <span>AI推荐</span>
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

        <el-row v-if="!isCompleted">
          <el-col :span="24">
            <div class="form-actions">
              <el-button type="primary" @click="handleSave" :loading="saving">
                <el-icon><Check /></el-icon>
                保存问诊记录
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowLeft, Notebook, Document, Check, Warning, DataAnalysis, MagicStick } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel } from "@/utils/format"
import type { RegistrationRecord } from "@/types"
import {
  getConsultationRecord,
  saveConsultationRecord,
  recommendConsultationByAi,
  type ConsultationRecordRecommendData,
} from "@/api/doctor"

const route = useRoute()
const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const aiRecommending = ref(false)
const record = ref<RegistrationRecord | null>(null)
const consultationRecordId = ref<number | null>(null)
const registrationId = Number(route.params.registrationId)

// AI推荐结果
const aiResults = reactive({
  presentIllness: "",
  pastHistory: "",
  physicalExamination: "",
  diagnosis: "",
  chiefComplaint: "",
  treatmentPlan: "",
})

// AI结果是否已被采纳
const aiResultsTaken = reactive({
  presentIllness: false,
  pastHistory: false,
  physicalExamination: false,
  diagnosis: false,
  chiefComplaint: false,
  treatmentPlan: false,
})

const consultForm = reactive({
  presentIllness: "",
  pastHistory: "",
  physicalExamination: "",
  diagnosis: "",
  chiefComplaint: "",
  treatmentPlan: "",
})

const isCompleted = computed(() => record.value?.status === "completed")

onMounted(async () => {
  if (!registrationId) return
  await loadRecord()
})

async function loadRecord() {
  if (!userStore.userId) return
  loading.value = true
  try {
    // 加载挂号记录
    const res = await regStore.fetchList({ doctorId: userStore.userId })
    const found = (res || []).find((r: RegistrationRecord) => r.id === registrationId)
    if (found) {
      record.value = found
      // 如果有症状，自动填充到现病史
      if (found.symptom && !consultForm.presentIllness) {
        consultForm.presentIllness = `患者自述：${found.symptom}`
        consultForm.chiefComplaint = found.symptom.substring(0, 50)
      }
    }

    // 加载已保存的问诊记录
    await loadConsultationRecord()
  } finally {
    loading.value = false
  }
}

async function loadConsultationRecord() {
  if (!registrationId) return
  try {
    const res = await getConsultationRecord(registrationId)
    if (res.code === 200 && res.data) {
      const data = res.data
      consultationRecordId.value = data.id
      // 填充已保存的问诊记录
      if (data.presentIllness) consultForm.presentIllness = data.presentIllness
      if (data.pastHistory) consultForm.pastHistory = data.pastHistory
      if (data.physicalExamination) consultForm.physicalExamination = data.physicalExamination
      if (data.diagnosis) consultForm.diagnosis = data.diagnosis
      if (data.chiefComplaint) consultForm.chiefComplaint = data.chiefComplaint
      if (data.treatmentPlan) consultForm.treatmentPlan = data.treatmentPlan
    }
  } catch (e) {
    console.error("加载问诊记录失败", e)
  }
}

async function handleSave() {
  if (!userStore.userId || !record.value) {
    ElMessage.warning("未获取到患者信息")
    return
  }

  if (!consultForm.diagnosis.trim()) {
    ElMessage.warning("请填写初步诊断")
    return
  }

  saving.value = true
  try {
    await saveConsultationRecord({
      registrationId: registrationId,
      patientId: record.value.patientId,
      doctorId: userStore.userId,
      presentIllness: consultForm.presentIllness,
      pastHistory: consultForm.pastHistory,
      physicalExamination: consultForm.physicalExamination,
      diagnosis: consultForm.diagnosis,
      chiefComplaint: consultForm.chiefComplaint,
      treatmentPlan: consultForm.treatmentPlan,
      aiRecommended: false,
    })
    ElMessage.success("问诊记录保存成功")
  } catch (e: any) {
    ElMessage.error(e?.message || "保存失败")
  } finally {
    saving.value = false
  }
}

async function handleAiRecommend() {
  if (!record.value?.symptom) {
    ElMessage.warning("患者暂无自述症状，无法进行AI推荐")
    return
  }

  aiRecommending.value = true
  try {
    const res = await recommendConsultationByAi(registrationId)
    if (res.code === 200 && res.data) {
      const data = res.data as ConsultationRecordRecommendData

      // 清空之前的采纳状态
      Object.keys(aiResultsTaken).forEach(key => {
        (aiResultsTaken as any)[key] = false
      })

      // 填充AI推荐结果（不清空输入框原有内容）
      if (data.presentIllness) aiResults.presentIllness = data.presentIllness
      if (data.pastHistory) aiResults.pastHistory = data.pastHistory
      if (data.physicalExamination) aiResults.physicalExamination = data.physicalExamination
      if (data.diagnosis) aiResults.diagnosis = data.diagnosis
      if (data.chiefComplaint) aiResults.chiefComplaint = data.chiefComplaint
      if (data.treatmentPlan) aiResults.treatmentPlan = data.treatmentPlan

      ElMessage.success("AI推荐已完成，请查看并选择采纳")
    } else {
      ElMessage.error("AI推荐失败")
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "AI推荐失败")
  } finally {
    aiRecommending.value = false
  }
}

// 采纳AI推荐结果
function takeAiResult(field: keyof typeof aiResults) {
  const value = aiResults[field]
  if (value) {
    (consultForm as any)[field] = value
    aiResultsTaken[field] = true
    aiResults[field] = ""
    ElMessage.success("已采纳AI推荐内容")
  }
}

function resetForm() {
  consultForm.presentIllness = record.value?.symptom ? `患者自述：${record.value.symptom}` : ""
  consultForm.pastHistory = ""
  consultForm.physicalExamination = ""
  consultForm.diagnosis = ""
  consultForm.chiefComplaint = record.value?.symptom ? record.value.symptom.substring(0, 50) : ""
  consultForm.treatmentPlan = ""
  // 清空AI结果
  Object.keys(aiResults).forEach(key => {
    (aiResults as any)[key] = ""
  })
  Object.keys(aiResultsTaken).forEach(key => {
    (aiResultsTaken as any)[key] = false
  })
}

async function completeConsultation() {
  if (!registrationId || !userStore.userId) return

  // 先保存问诊记录
  if (consultForm.diagnosis.trim()) {
    await handleSave()
  }

  try {
    await ElMessageBox.confirm(
      "确认完成本次问诊吗？完成后将无法修改问诊记录。",
      "提示",
      { type: "warning" }
    )
  } catch {
    return
  }

  try {
    const result = await regStore.completeConsultation(registrationId, userStore.userId)
    if (result) {
      record.value = result
    }
    ElMessage.success("问诊已完成")
  } catch (e: any) {
    ElMessage.error(e?.message || "操作失败")
  }
}

function goToMedicalRecord() {
  if (record.value) {
    router.push(`/doctor/medical-record/${record.value.patientId}?registrationId=${record.value.id}`)
  }
}

function goToPrescription() {
  if (record.value) {
    router.push(`/doctor/prescription/${record.value.patientId}?registrationId=${record.value.id}`)
  }
}

function goBack() {
  router.push("/doctor/patients")
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

/* 患者自述与AI分诊高亮卡片 */
.info-highlight-card {
  margin-bottom: 12px;
  background: linear-gradient(135deg, #fdf6ec 0%, #ecf5ff 100%);
}
.highlight-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.section-item {
  padding: 12px 16px;
  border-radius: 8px;
}
.section-item.patient-symptom {
  background: #fff9e6;
  border-left: 4px solid #e6a23c;
}
.section-item.ai-triage {
  background: #ecf5ff;
  border-left: 4px solid #409eff;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
}
.section-item.patient-symptom .section-label {
  color: #e6a23c;
}
.section-item.ai-triage .section-label {
  color: #409eff;
}
.section-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 问诊表单卡片 */
.consultation-form-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header .header-actions {
  display: flex;
  gap: 8px;
}
.consult-form :deep(.el-textarea__inner) {
  font-size: 14px;
}
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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

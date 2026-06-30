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

    <!-- 主体区域：左侧聊天 + 右侧问诊记录 -->
    <div class="main-content">
      <!-- 左侧聊天区域 -->
      <el-card class="chat-card">
        <template #header>
          <div class="chat-header">
            <span>问诊对话</span>
            <el-tag size="small" type="info">共 {{ messages.length }} 条消息</el-tag>
          </div>
        </template>

        <div class="chat-container">
          <!-- 患者自述症状（固定在聊天顶部） -->
          <div class="patient-symptom-banner">
            <div class="symptom-label">
              <el-icon><Warning /></el-icon>
              <span>患者自述症状</span>
            </div>
            <div class="symptom-content">{{ record?.symptom || "无" }}</div>
            <div v-if="record?.triageResult" class="triage-result">
              <el-icon><DataAnalysis /></el-icon>
              <span>AI分诊：{{ record.triageResult }}</span>
            </div>
          </div>

          <!-- 聊天消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-item"
              :class="msg.senderType === 'DOCTOR' ? 'message-doctor' : 'message-patient'"
            >
              <div class="message-avatar">
                <el-avatar :size="36">
                  {{ msg.senderName?.charAt(0) || msg.senderType === 'DOCTOR' ? '医' : '患' }}
                </el-avatar>
              </div>
              <div class="message-body">
                <div class="message-sender">
                  {{ msg.senderName }}
                  <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
                </div>
                <div class="message-bubble">{{ msg.content }}</div>
              </div>
            </div>

            <div v-if="messages.length === 0" class="empty-messages">
              <el-empty description="暂无对话记录，开始问诊吧" :image-size="80" />
            </div>
          </div>

          <!-- 消息输入区域 -->
          <div class="message-input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="输入消息，按 Enter 发送，Shift+Enter 换行"
              :disabled="isCompleted"
              @keydown.enter.exact="handleSendMessage"
            />
            <div class="input-actions">
              <el-button type="primary" @click="handleSendMessage" :loading="sendingMessage" :disabled="isCompleted || !inputMessage.trim()">
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 右侧问诊记录表单 -->
      <el-card class="record-card">
        <template #header>
          <div class="card-header">
            <span>问诊记录</span>
            <div class="header-actions">
              <el-button
                type="primary"
                size="small"
                @click="handleAiRecommend"
                :loading="aiRecommending"
                :disabled="isCompleted"
              >
                <el-icon><MagicStick /></el-icon>
                AI推荐疗法
              </el-button>
              <el-button size="small" @click="resetForm" v-if="!isCompleted">重置</el-button>
            </div>
          </div>
        </template>

        <el-form :model="consultForm" label-width="90px" class="consult-form">
          <!-- 现病史 -->
          <el-form-item label="现病史">
            <el-input
              v-model="consultForm.presentIllness"
              type="textarea"
              :rows="2"
              placeholder="请详细描述患者本次发病经过、主要症状、伴随症状等"
              :disabled="isCompleted"
            />
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

          <!-- 既往史 -->
          <el-form-item label="既往史">
            <el-input
              v-model="consultForm.pastHistory"
              type="textarea"
              :rows="2"
              placeholder="请描述患者既往病史、过敏史、手术史等"
              :disabled="isCompleted"
            />
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

          <!-- 体格检查 -->
          <el-form-item label="体格检查">
            <el-input
              v-model="consultForm.physicalExamination"
              type="textarea"
              :rows="2"
              placeholder="请描述体温、血压、心率等体格检查结果"
              :disabled="isCompleted"
            />
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

          <!-- 初步诊断 + 主诉 -->
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="初步诊断">
                <el-input
                  v-model="consultForm.diagnosis"
                  placeholder="请输入初步诊断"
                  :disabled="isCompleted"
                />
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
          <el-form-item label="治疗意见">
            <el-input
              v-model="consultForm.treatmentPlan"
              type="textarea"
              :rows="2"
              placeholder="请输入治疗意见、注意事项等"
              :disabled="isCompleted"
            />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowLeft, Notebook, Document, Check, Warning, DataAnalysis, MagicStick, Promotion } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel } from "@/utils/format"
import type { RegistrationRecord, ConsultationMessage } from "@/types"
import {
  getConsultationRecord,
  saveConsultationRecord,
  recommendConsultationByAi,
  type ConsultationRecordRecommendData,
} from "@/api/doctor"
import { sendConsultationMessage, getConsultationMessages } from "@/api/consultation"

const route = useRoute()
const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const aiRecommending = ref(false)
const sendingMessage = ref(false)
const record = ref<RegistrationRecord | null>(null)
const consultationRecordId = ref<number | null>(null)
const registrationId = Number(route.params.registrationId)
const messageListRef = ref<HTMLElement | null>(null)

// 聊天消息
const messages = ref<ConsultationMessage[]>([])
const inputMessage = ref("")

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
  await loadMessages()
})

async function loadRecord() {
  if (!userStore.userId) return
  loading.value = true
  try {
    const res = await regStore.fetchList({ doctorId: userStore.userId })
    const found = (res || []).find((r: RegistrationRecord) => r.id === registrationId)
    if (found) {
      record.value = found
      if (found.symptom && !consultForm.presentIllness) {
        consultForm.presentIllness = `患者自述：${found.symptom}`
        consultForm.chiefComplaint = found.symptom.substring(0, 50)
      }
    }
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

async function loadMessages() {
  if (!registrationId) return
  try {
    const res = await getConsultationMessages(registrationId)
    if (res.code === 200 && res.data) {
      messages.value = res.data
      await nextTick()
      scrollToBottom()
    }
  } catch (e) {
    console.error("加载聊天消息失败", e)
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSendMessage() {
  if (!inputMessage.value.trim() || !userStore.userId || !record.value) return

  sendingMessage.value = true
  try {
    const res = await sendConsultationMessage({
      registrationId: registrationId,
      senderType: "DOCTOR",
      senderId: userStore.userId,
      content: inputMessage.value.trim(),
    })

    if (res.code === 200 && res.data) {
      messages.value.push(res.data)
      inputMessage.value = ""
      await nextTick()
      scrollToBottom()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "发送失败")
  } finally {
    sendingMessage.value = false
  }
}

function formatTime(timeStr: string) {
  if (!timeStr) return ""
  const date = new Date(timeStr)
  const hours = date.getHours().toString().padStart(2, "0")
  const minutes = date.getMinutes().toString().padStart(2, "0")
  const month = (date.getMonth() + 1).toString().padStart(2, "0")
  const day = date.getDate().toString().padStart(2, "0")
  return `${month}-${day} ${hours}:${minutes}`
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
  aiRecommending.value = true
  try {
    const res = await recommendConsultationByAi(registrationId)
    if (res.code === 200 && res.data) {
      const data = res.data as ConsultationRecordRecommendData

      Object.keys(aiResultsTaken).forEach(key => {
        (aiResultsTaken as any)[key] = false
      })

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
  Object.keys(aiResults).forEach(key => {
    (aiResults as any)[key] = ""
  })
  Object.keys(aiResultsTaken).forEach(key => {
    (aiResultsTaken as any)[key] = false
  })
}

async function completeConsultation() {
  if (!registrationId || !userStore.userId) return

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

/* 主体布局 */
.main-content {
  display: flex;
  gap: 16px;
  height: calc(100vh - 240px);
  min-height: 600px;
}

/* 左侧聊天卡片 */
.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 患者自述横幅 */
.patient-symptom-banner {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff9e6 0%, #fef0f0 100%);
  border-radius: 8px;
  margin-bottom: 12px;
  border-left: 4px solid #e6a23c;
}
.symptom-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
  margin-bottom: 6px;
}
.symptom-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
.triage-result {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #409eff;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.empty-messages {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.message-item {
  display: flex;
  gap: 10px;
}
.message-item.message-doctor {
  flex-direction: row-reverse;
}
.message-avatar {
  flex-shrink: 0;
}
.message-body {
  max-width: 70%;
}
.message-item.message-doctor .message-body {
  text-align: right;
}
.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.message-time {
  margin-left: 8px;
  color: #c0c4cc;
}
.message-item.message-doctor .message-time {
  margin-left: 0;
  margin-right: 8px;
}
.message-bubble {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}
.message-item.message-patient .message-bubble {
  background: #f5f7fa;
  color: #303133;
  border-top-left-radius: 4px;
}
.message-item.message-doctor .message-bubble {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

/* 消息输入区 */
.message-input-area {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

/* 右侧问诊记录卡片 */
.record-card {
  width: 480px;
  flex-shrink: 0;
  overflow-y: auto;
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
  font-size: 13px;
}
.consult-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* AI推荐结果样式 */
.ai-result-box {
  margin-top: 8px;
  padding: 10px;
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
}
.ai-result-box--warning {
  background: #fff9e6;
  border-color: #e6a23c;
}
.ai-result-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
  margin-bottom: 6px;
}
.ai-result-label--warning {
  color: #b8860b;
}
.ai-result-content {
  font-size: 13px;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 8px;
}
.ai-result-actions {
  display: flex;
  justify-content: flex-end;
}
</style>

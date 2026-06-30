<template>
  <div class="consultation-page page-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <div class="header-actions">
        <el-button size="small" @click="goToHistory"><el-icon><Document /></el-icon>历史记录</el-button>
        <el-button type="warning" @click="completeConsultation" :disabled="record?.status === 'completed'">
          <el-icon><Check /></el-icon>
          完成问诊
        </el-button>
        <el-button type="primary" @click="viewMedicalRecord" :disabled="generatingRecord">
          <el-icon><Notebook /></el-icon>
          {{ medicalRecordId || consultationRecordId || recordGenerated ? '查看病例' : '生成病历' }}
        </el-button>
        <el-button type="success" @click="goToPrescription">
          <el-icon><Document /></el-icon>
          开具处方
        </el-button>
        <el-button type="danger" @click="generateMedicalRecordFromChat" :loading="generatingRecord" :disabled="recordGenerated">
          <el-icon><MagicStick /></el-icon>
          AI生成病历
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
            <div class="sender-toggle">
              <span class="toggle-label">消息发送者：</span>
              <el-radio-group v-model="senderType" size="small">
                <el-radio-button value="DOCTOR"><el-icon><User /></el-icon>医生</el-radio-button>
                <el-radio-button value="PATIENT"><el-icon><UserFilled /></el-icon>患者</el-radio-button>
              </el-radio-group>
            </div>
            <div class="input-toolbar">
              <div class="toolbar-left">
                <el-button size="small" :type="isListening ? 'danger' : 'default'" @click="toggleVoiceInput" :disabled="isCompleted">
                  <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm-1-9c0-.55.45-1 1-1s1 .45 1 1v6c0 .55-.45 1-1 1s-1-.45-1-1V5zm6 6c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/></svg></el-icon>{{ isListening ? '停止录音' : '语音输入' }}</el-button>
                <span v-if="voiceTranscript" class="voice-hint">{{ voiceTranscript }}</span>
              </div>
              <div class="toolbar-right">
                <el-button type="primary" @click="handleSendMessage" :loading="sendingMessage" :disabled="isCompleted || !inputMessage.trim()">
                  <el-icon><Promotion /></el-icon>
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from "vue"
import { useRoute, useRouter, onBeforeRouteUpdate } from "vue-router"
import { ArrowLeft, Notebook, Document, Check, Warning, DataAnalysis, MagicStick, Promotion, User, UserFilled } from "@element-plus/icons-vue"
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
const generatingRecord = ref(false)
const recordGenerated = ref(false)
const sendingMessage = ref(false)
const record = ref<RegistrationRecord | null>(null)
const consultationRecordId = ref<number | null>(null)
const medicalRecordId = ref<number | null>(null)
const registrationId = Number(route.params.registrationId)
const messageListRef = ref<HTMLElement | null>(null)

// 聊天消息
const messages = ref<ConsultationMessage[]>([])
const inputMessage = ref("")
const senderType = ref("DOCTOR")
const isListening = ref(false)
const voiceTranscript = ref("")
const recognition = ref<any>(null)
const voiceBusy = ref(false)
const voiceTimeout = ref<any>(null)

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
    await checkMedicalRecord()
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

async function checkMedicalRecord() {
  if (!registrationId || !record.value?.patientId) return
  try {
    const { getMedicalRecordList } = await import("@/api/medicalRecord")
    const res = await getMedicalRecordList({ patientId: record.value.patientId })
    const list = res || []
    const match = list.find((r: any) => r.registrationId === registrationId)
    if (match) {
      medicalRecordId.value = match.id
    }
  } catch (e) {
    // 没有病历记录是正常情况
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

function toggleVoiceInput() {
  if (isListening.value) {
    isListening.value = false
    if (voiceTimeout.value) { clearTimeout(voiceTimeout.value); voiceTimeout.value = null }
    if (recognition.value) {
      try { recognition.value.onend = null; recognition.value.onresult = null; recognition.value.onerror = null; recognition.value.stop() } catch(e) {}
      recognition.value = null
    }
    return
  }
  if (voiceBusy.value) return
  voiceBusy.value = true
  const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SR) { ElMessage.warning("当前浏览器不支持语音识别，请用Chrome或Edge"); voiceBusy.value = false; return }
  try {
    const rec = new SR()
    rec.lang = "zh-CN"; rec.continuous = true; rec.interimResults = true; rec.maxAlternatives = 1
    rec.onresult = function(event) {
      let interimT = ""; let finalT = ""
      for (let i = event.resultIndex; i < event.results.length; i++) {
        if (event.results[i].isFinal) finalT += event.results[i][0].transcript
        else interimT += event.results[i][0].transcript
      }
      if (interimT) voiceTranscript.value = interimT
      if (finalT) { voiceTranscript.value = finalT; inputMessage.value = (inputMessage.value || "") + finalT }
    }
    rec.onerror = function() { isListening.value = false; voiceBusy.value = false; recognition.value = null }
    rec.onend = function() { isListening.value = false; voiceBusy.value = false; recognition.value = null }
    rec.start()
    recognition.value = rec; isListening.value = true; voiceBusy.value = false
    voiceTimeout.value = setTimeout(function() {
      if (isListening.value) { isListening.value = false; voiceBusy.value = false; try { rec.stop() } catch(e) {}; recognition.value = null }
    }, 15000)
  } catch (e) { isListening.value = false; voiceBusy.value = false; recognition.value = null; ElMessage.warning("语音启动失败，可直接打字发送") }
}

async function handleSendMessage() {
  if (!inputMessage.value.trim() || isCompleted.value) return
  sendingMessage.value = true
  try {
    let senderId = userStore.userId
    if (senderType.value === "PATIENT" && record.value) {
      senderId = record.value.patientId
    }
    const res = await sendConsultationMessage({
      registrationId: registrationId,
      senderType: senderType.value,
      senderId: senderId,
      content: inputMessage.value.trim(),
    })
    if (res.code === 200 && res.data) {
      messages.value.push(res.data)
      inputMessage.value = ""
      voiceTranscript.value = ""
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
    const saveRes = await saveConsultationRecord({
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
    if (saveRes.code === 200 && saveRes.data && saveRes.data.id) {
      consultationRecordId.value = saveRes.data.id
    }
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

async function generateMedicalRecordFromChat() {
  if (messages.value.length === 0) { ElMessage.warning("暂无对话内容，无法生成病历"); return }
  if (!record.value) { ElMessage.warning("缺少患者信息"); return }
  generatingRecord.value = true
  try {
    var conversationText = messages.value.map(function(m) { return m.senderName + "(" + (m.senderType === "DOCTOR" ? "医生" : "患者") + "): " + m.content }).join("\n")
    var res = await fetch("/api/medical-record/generate?patientId=" + record.value.patientId, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ dialogueText: conversationText })
    })
    var data = await res.json()
    if (data.code === 200 && data.data) {
      sessionStorage.setItem("aiGeneratedRecord", JSON.stringify(data.data))
      recordGenerated.value = true
      ElMessage.success("AI病历生成成功，正在跳转...")
      router.push({ path: "/doctor/medical-record/" + record.value.patientId, query: { registrationId: record.value.id, fromAiGenerate: true } })
    } else {
      ElMessage.error(data.message || "AI生成病历失败")
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e.message || "AI生成病历失败，请检查网络连接")
  } finally { generatingRecord.value = false }
}

function goToMedicalRecord() {
  if (record.value) {
    router.push(`/doctor/medical-record/${record.value.patientId}?registrationId=${record.value.id}`)
  }
}
function viewMedicalRecord() {
  if (!record.value || !record.value.patientId) {
    ElMessage.warning("数据加载中，请稍候");
    return
  }
  var readonly = medicalRecordId.value || consultationRecordId.value || recordGenerated.value ? "true" : ""
  var pid = record.value.patientId
  var rid = record.value.id || registrationId
  var target = medicalRecordId.value
    ? `/doctor/medical-record/${pid}?registrationId=${rid}&medicalRecordId=${medicalRecordId.value}`
    : `/doctor/medical-record/${pid}?registrationId=${rid}` + (readonly ? `&readonly=${readonly}` : "")
  router.push(target)
}
function goToPrescription() {
  if (record.value) {
    router.push(`/doctor/prescription/${record.value.patientId}?registrationId=${record.value.id}`)
  }
}

function goBack() {
  router.push("/doctor/patients")
}

function goToHistory() {
  router.push("/doctor/history")
}


let pollTimer = null
onMounted(() => {
  if (record.value?.status !== "completed") {
    pollTimer = setInterval(() => { loadMessages() }, 5000)
  }
})
// 监听路由变化（保存病历后返回时重新检查）
onBeforeRouteUpdate((to) => {
  if (to.query.saved) {
    setTimeout(() => checkMedicalRecord(), 500)
  }
})

onUnmounted(() => {
  if (voiceTimeout.value) { clearTimeout(voiceTimeout.value); voiceTimeout.value = null }
  if (recognition.value) { try { recognition.value.onend = null; recognition.value.onresult = null; recognition.value.onerror = null; recognition.value.abort() } catch(e) {}; recognition.value = null }
  if (pollTimer) clearInterval(pollTimer)
})
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

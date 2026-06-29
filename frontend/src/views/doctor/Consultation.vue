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

    <el-card class="page-card patient-info-card">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="患者">{{ record?.patientName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ record?.department }}</el-descriptions-item>
        <el-descriptions-item label="症状">{{ record?.symptom || "无" }}</el-descriptions-item>
        <el-descriptions-item label="分诊结果">{{ record?.triageResult || "无" }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="page-card chat-card">
      <template #header>
        <div class="chat-header">
          <span>问诊对话</span>
          <el-tag size="small" :type="getStatusTag(record?.status || '')">{{ getStatusLabel(record?.status || '') }}</el-tag>
        </div>
      </template>

      <div ref="chatContainer" class="chat-messages">
        <app-loading :visible="loading" />
        <div v-if="!loading">
          <div v-if="record?.symptom" class="system-message">
            <el-icon><Warning /></el-icon>
            <div class="system-content">
              <div class="system-title">患者自述症状</div>
              <div class="system-text">{{ record.symptom }}</div>
            </div>
          </div>
          <div v-if="record?.triageResult" class="system-message system-triage">
            <el-icon><DataAnalysis /></el-icon>
            <div class="system-content">
              <div class="system-title">AI分诊结果</div>
              <div class="system-text">{{ record.triageResult }}</div>
            </div>
          </div>
          <div v-if="messages.length === 0 && !record?.symptom" class="empty-tip">
            暂无对话记录，开始问诊吧
          </div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{ 'is-doctor': msg.senderType === 'DOCTOR', 'is-patient': msg.senderType === 'PATIENT' }"
          >
            <div class="message-avatar">
              <el-avatar :size="36">
                {{ msg.senderName?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="message-body">
              <div class="message-sender">{{ msg.senderName }} <span class="message-time">{{ formatTime(msg.createdAt) }}</span></div>
              <div class="message-bubble">{{ msg.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="输入问诊内容，按 Enter 发送，Shift+Enter 换行"
          @keydown.enter.exact="handleSend"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="sending" @click="handleSend">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowLeft, Notebook, Document, Promotion, Check, Warning, DataAnalysis } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useConsultationStore } from "@/stores/consultation"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"
import type { RegistrationRecord, ConsultationMessage } from "@/types"

const route = useRoute()
const router = useRouter()
const consultStore = useConsultationStore()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const chatContainer = ref<HTMLElement | null>(null)
const loading = ref(false)
const sending = ref(false)
const inputMessage = ref("")
const record = ref<RegistrationRecord | null>(null)
const messages = ref<ConsultationMessage[]>([])

const registrationId = Number(route.params.registrationId)

onMounted(async () => {
  if (!registrationId) return
  await loadRecord()
  await loadMessages()
})

async function loadRecord() {
  if (!userStore.userId) return
  const res = await regStore.fetchList({ doctorId: userStore.userId })
  const found = (res || []).find((r: RegistrationRecord) => r.id === registrationId)
  if (found) {
    record.value = found
  }
}

async function loadMessages() {
  if (!registrationId) return
  loading.value = true
  try {
    const res = await consultStore.fetchMessages(registrationId)
    messages.value = res || []
    await nextTick()
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (!inputMessage.value.trim() || sending.value) return
  if (!userStore.userId || !registrationId) return

  sending.value = true
  try {
    await consultStore.sendMessage({
      registrationId,
      senderType: "DOCTOR",
      senderId: userStore.userId,
      content: inputMessage.value.trim(),
    })
    messages.value = [...consultStore.messages]
    inputMessage.value = ""
    await nextTick()
    scrollToBottom()
  } catch (e: any) {
    ElMessage.error(e?.message || "发送失败")
  } finally {
    sending.value = false
  }
}

async function completeConsultation() {
  if (!registrationId || !userStore.userId) return

  try {
    await ElMessageBox.confirm("确认完成本次问诊吗？完成后状态将变为已完成。", "提示", {
      type: "warning",
    })
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

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ""
  return formatDateTime(dateStr)
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
  margin-bottom: 16px;
}
.chat-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 220px);
}
.chat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  padding: 0;
}
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  font-weight: 600;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
.message-item {
  display: flex;
  margin-bottom: 16px;
  gap: 10px;
}
.message-item.is-doctor {
  flex-direction: row-reverse;
}
.message-avatar {
  flex-shrink: 0;
}
.message-body {
  max-width: 70%;
}
.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.message-item.is-doctor .message-sender {
  text-align: right;
}
.message-time {
  margin-left: 8px;
  color: #c0c4cc;
}
.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.5;
}
.message-item.is-doctor .message-bubble {
  background: #ecf5ff;
  color: #303133;
}
.system-message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fdf6ec;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}
.system-message.system-triage {
  background: #ecf5ff;
  border-left-color: #409eff;
}
.system-message .el-icon {
  font-size: 20px;
  color: #e6a23c;
  margin-top: 2px;
  flex-shrink: 0;
}
.system-message.system-triage .el-icon {
  color: #409eff;
}
.system-content {
  flex: 1;
}
.system-title {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
  margin-bottom: 4px;
}
.system-message.system-triage .system-title {
  color: #409eff;
}
.system-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
.chat-input-area {
  border-top: 1px solid #ebeef5;
  padding: 16px 20px;
  background: #fff;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>

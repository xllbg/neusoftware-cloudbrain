<template>
  <div class="consultation-page page-container">
    <h2 class="page-title">问诊</h2>

    <el-card class="page-card">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="患者">{{ record?.patientName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ record?.department }}</el-descriptions-item>
        <el-descriptions-item label="症状">{{ record?.symptom }}</el-descriptions-item>
        <el-descriptions-item label="分诊结果">{{ record?.triageResult || "无" }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="page-card" style="margin-top: 16px;">
      <template #header>问诊记录</template>
      <el-input
        v-model="consultationNote"
        type="textarea"
        :rows="6"
        placeholder="请在此录入问诊对话记录、初步诊断意见..."
        maxlength="1000"
        show-word-limit
      />
    </el-card>

    <div class="action-bar">
      <el-button type="primary" size="large" @click="goToMedicalRecord">
        <el-icon><Notebook /></el-icon>
        AI 生成病历
      </el-button>
      <el-button type="success" size="large" @click="goToPrescription">
        <el-icon><Document /></el-icon>
        开具处方
      </el-button>
      <el-button @click="goBack">返回列表</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { Notebook, Document } from "@element-plus/icons-vue"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import type { RegistrationRecord } from "@/types"

const route = useRoute()
const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()
const record = ref<RegistrationRecord | null>(null)
const consultationNote = ref("")

onMounted(async () => {
  const registrationId = Number(route.params.registrationId)
  if (!registrationId) return
  const res = await regStore.fetchList({ doctorId: userStore.userId })
  const found = (res || []).find((r: any) => r.id === registrationId)
  if (found) record.value = found
})

function goToMedicalRecord() {
  if (record.value) {
    router.push(`/doctor/medical-record/${record.value.patientId}`)
  }
}

function goToPrescription() {
  if (record.value) {
    router.push(`/doctor/prescription/${record.value.patientId}`)
  }
}

function goBack() {
  router.push("/doctor/patients")
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.action-bar { margin-top: 20px; display: flex; gap: 12px; }
</style>

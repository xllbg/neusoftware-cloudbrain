<template>
  <div class="medical-record-page page-container">
    <h2 class="page-title">病历录入</h2>

    <el-card class="page-card">
      <!-- AI 降级提示 -->
      <el-alert
        v-if="fallbackMode"
        title="AI 病历生成服务暂不可用，您已切换为手动填写模式"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom:16px;"
      />

      <el-form :model="form" label-width="120px">
        <el-form-item label="主诉">
          <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" placeholder="患者主诉内容" />
        </el-form-item>
        <el-form-item label="现病史">
          <el-input v-model="form.presentIllness" type="textarea" :rows="3" placeholder="现病史描述" />
        </el-form-item>
        <el-form-item label="既往史">
          <el-input v-model="form.pastHistory" type="textarea" :rows="2" placeholder="既往病史（如有）" />
        </el-form-item>
        <el-form-item label="检查结果">
          <el-input v-model="form.examinationResult" type="textarea" :rows="2" placeholder="检查结果" />
        </el-form-item>
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" placeholder="诊断结果" />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input v-model="form.treatmentPlan" type="textarea" :rows="2" placeholder="治疗方案" />
        </el-form-item>

        <el-form-item>
          <div style="display:flex; gap:12px;">
            <el-button type="primary" size="large" :loading="saving" @click="handleSave">
              保存病历
            </el-button>
            <el-button type="warning" size="large" :loading="generating" @click="handleAIGenerate" :disabled="fallbackMode">
              <el-icon><MagicStick /></el-icon>
              AI 生成病历
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- AI 生成结果 -->
      <div v-if="generatedRecord" class="ai-result" style="margin-top: 20px;">
        <div class="ai-result-title">AI 生成病历结果</div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="主诉">{{ generatedRecord.chiefComplaint }}</el-descriptions-item>
          <el-descriptions-item label="现病史">{{ generatedRecord.presentIllness }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ generatedRecord.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="治疗方案">{{ generatedRecord.treatmentPlan }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:12px;">
          <el-button type="success" @click="fillGenerated">使用此结果</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { MagicStick } from "@element-plus/icons-vue"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useUserStore } from "@/stores/user"
import { callAIService } from "@/api"
import type { MedicalRecord } from "@/types"

const route = useRoute()
const router = useRouter()
const recordStore = useMedicalRecordStore()
const userStore = useUserStore()

const saving = ref(false)
const generating = ref(false)
const generatedRecord = ref<MedicalRecord | null>(null)
const fallbackMode = ref(false)

const form = reactive({
  chiefComplaint: "",
  presentIllness: "",
  pastHistory: "",
  examinationResult: "",
  diagnosis: "",
  treatmentPlan: "",
})

async function handleSave() {
  if (!userStore.userId) return
  saving.value = true
  try {
    await recordStore.save({
      patientId: Number(route.params.patientId),
      doctorId: userStore.userId,
      ...form,
    })
    ElMessage.success("病历保存成功")
    router.push("/doctor/history")
  } finally {
    saving.value = false
  }
}

async function handleAIGenerate() {
  if (!form.chiefComplaint && !form.presentIllness) {
    ElMessage.warning("请至少填写主诉或现病史")
    return
  }
  generating.value = true
  try {
    const res = await callAIService(
      () => recordStore.generate({
        chiefComplaint: form.chiefComplaint,
        presentIllness: form.presentIllness,
        pastHistory: form.pastHistory || undefined,
      }),
      {
        loadingText: "AI 正在生成病历...",
        fallbackMessage: "AI 病历生成服务暂时不可用",
        allowFallback: true,
      }
    )
    if (res.fallback) {
      fallbackMode.value = true
      ElMessage.info("已切换到手动填写模式")
    } else if (res.success && res.data) {
      generatedRecord.value = res.data as MedicalRecord
    }
  } finally {
    generating.value = false
  }
}

function fillGenerated() {
  if (!generatedRecord.value) return
  form.chiefComplaint = generatedRecord.value.chiefComplaint || form.chiefComplaint
  form.presentIllness = generatedRecord.value.presentIllness || form.presentIllness
  form.diagnosis = generatedRecord.value.diagnosis || form.diagnosis
  form.treatmentPlan = generatedRecord.value.treatmentPlan || form.treatmentPlan
  generatedRecord.value = null
  ElMessage.success("已填充 AI 生成结果")
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
</style>

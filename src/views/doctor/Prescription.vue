<template>
  <div class="prescription-page page-container">
    <h2 class="page-title">开具处方</h2>

    <el-card class="page-card">
      <el-form :model="form" label-width="100px">
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" placeholder="请输入诊断结果" />
        </el-form-item>

        <el-form-item label="药品明细">
          <div class="drug-items">
            <div v-for="(item, idx) in form.items" :key="idx" class="drug-row">
              <el-input v-model="item.drugName" placeholder="药品名称" style="width:150px" />
              <el-input v-model="item.dosage" placeholder="用量" style="width:100px" />
              <el-input v-model="item.frequency" placeholder="频次" style="width:100px" />
              <el-input v-model="item.duration" placeholder="疗程" style="width:100px" />
              <el-input v-model="item.note" placeholder="备注" style="width:130px" />
              <el-button type="danger" :icon="Delete" circle @click="removeDrug(idx)" />
            </div>
            <el-button type="primary" link @click="addDrug">
              <el-icon><Plus /></el-icon> 添加药品
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <div style="display:flex; gap:12px;">
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              提交处方
            </el-button>
            <el-button
              type="warning"
              size="large"
              :loading="aiChecking"
              :disabled="!createdPrescriptionId"
              @click="handleAICheck"
            >
              <el-icon><MagicStick /></el-icon>
              AI 审核
            </el-button>
          </div>
          <div v-if="!createdPrescriptionId" style="color:#909399; font-size:12px; margin-top:4px;">
            ⚠ 请先提交处方，再使用 AI 审核
          </div>
        </el-form-item>
      </el-form>

      <!-- AI 降级提示 -->
      <el-alert
        v-if="fallbackMode"
        title="AI 审核服务暂不可用，您可以直接提交处方"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom:16px;"
      />

      <!-- AI 审核结果 -->
      <div v-if="aiResult" class="ai-result">
        <div class="ai-result-title">AI 审核结果</div>
        <div style="display:flex; align-items:center; gap:8px; margin-bottom:12px;">
          <el-tag :type="aiResult.approved ? 'success' : 'danger'" size="large">
            {{ aiResult.approved ? "审核通过" : "审核不通过" }}
          </el-tag>
          <el-tag :class="riskClass">
            风险等级：{{ riskLabel }}
          </el-tag>
        </div>
        <div v-if="aiResult.warnings.length" style="margin-top:8px;">
          <p style="font-weight:600; color:#f56c6c;">⚠ 警告：</p>
          <p v-for="(w, i) in aiResult.warnings" :key="i" class="warning-item">{{ i+1 }}. {{ w }}</p>
        </div>
        <div v-if="aiResult.suggestions.length" style="margin-top:8px;">
          <p style="font-weight:600; color:#67c23a;">💡 建议：</p>
          <p v-for="(s, i) in aiResult.suggestions" :key="i" class="suggestion-item">{{ i+1 }}. {{ s }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { Delete, Plus, MagicStick } from "@element-plus/icons-vue"
import { usePrescriptionStore } from "@/stores/prescription"
import { useUserStore } from "@/stores/user"
import { callAIService } from "@/api"
import type { PrescriptionItem, AICheckResult } from "@/types"

const route = useRoute()
const router = useRouter()
const presStore = usePrescriptionStore()
const userStore = useUserStore()

const submitting = ref(false)
const aiChecking = ref(false)
const aiResult = ref<AICheckResult | null>(null)
const createdPrescriptionId = ref<number | null>(null)
const fallbackMode = ref(false)

const form = reactive({
  diagnosis: "",
  items: [{ drugName: "", dosage: "", frequency: "", duration: "", note: "" } as PrescriptionItem],
})

const riskClass = computed(() => {
  if (!aiResult.value) return ""
  return aiResult.value.riskLevel === "HIGH" ? "risk-high"
    : aiResult.value.riskLevel === "MEDIUM" ? "risk-medium" : "risk-low"
})

const riskLabel = computed(() => {
  if (!aiResult.value) return ""
  return aiResult.value.riskLevel === "HIGH" ? "高风险"
    : aiResult.value.riskLevel === "MEDIUM" ? "中风险" : "低风险"
})

function addDrug() {
  form.items.push({ drugName: "", dosage: "", frequency: "", duration: "", note: "" })
}

function removeDrug(idx: number) {
  form.items.splice(idx, 1)
}

async function handleSubmit() {
  if (!form.diagnosis || form.items.length === 0) {
    ElMessage.warning("请填写诊断和至少一种药品")
    return
  }
  if (!userStore.userId) return

  submitting.value = true
  try {
    const result = await presStore.create({
      patientId: Number(route.params.patientId),
      doctorId: userStore.userId,
      diagnosis: form.diagnosis,
      items: form.items,
    })
    createdPrescriptionId.value = result.id
    ElMessage.success("处方已提交，可使用 AI 审核")
  } finally {
    submitting.value = false
  }
}

async function handleAICheck() {
  if (!createdPrescriptionId.value) {
    ElMessage.warning("请先提交处方")
    return
  }
  aiChecking.value = true
  try {
    const res = await callAIService(
      () => presStore.check(createdPrescriptionId.value!),
      {
        loadingText: "AI 正在审核处方...",
        fallbackMessage: "AI 审核服务暂时不可用",
        allowFallback: true,
      }
    )
    if (res.fallback) {
      fallbackMode.value = true
      // 降级后仍然可以提交
      ElMessage.success("处方已提交，您可以稍后重试 AI 审核")
      router.push("/doctor/history")
    } else if (res.success && res.data) {
      aiResult.value = res.data as AICheckResult
    }
  } finally {
    aiChecking.value = false
  }
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.drug-items { width: 100%; }
.drug-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.warning-item { color: #f56c6c; padding: 4px 0; }
.suggestion-item { color: #67c23a; padding: 2px 0; }
</style>

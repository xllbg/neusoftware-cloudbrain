<template>
  <div class="triage-page page-container">
    <h2 class="page-title">智能分诊</h2>
    <p class="page-desc">请描述您的症状，AI 将为您推荐合适的科室和医生</p>

    <el-card class="page-card">
      <el-input
        v-model="symptom"
        type="textarea"
        :rows="4"
        placeholder="请详细描述您的症状，例如：胸痛伴气短、头痛三天、发烧38.5度..."
        maxlength="500"
        show-word-limit
      />
      <div class="action-bar">
        <el-button type="primary" size="large" :loading="loading" :disabled="!symptom.trim()" @click="handleTriage">
          <el-icon><MagicStick /></el-icon>
          AI 智能分诊
        </el-button>
      </div>

      <!-- AI 降级提示 -->
      <el-alert
        v-if="fallbackMode"
        title="AI 服务暂不可用，您可以直接查看科室列表并选择挂号"
        type="warning"
        show-icon
        :closable="false"
        style="margin-top:16px;"
      >
        <template #default>
          <el-button size="small" type="warning" @click="goToRegistration" style="margin-top:8px;">
            去挂号（手动选择）
          </el-button>
        </template>
      </el-alert>

      <!-- 分诊结果 -->
      <div v-if="result && !fallbackMode" class="ai-result" style="margin-top:20px;">
        <div class="ai-result-title">
          <el-icon><Promotion /></el-icon>
          分诊结果
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="推荐科室">
            <el-tag type="primary" size="large">{{ result.department }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="推荐医生">{{ result.doctorName }}（{{ result.doctorTitle }}）</el-descriptions-item>
          <el-descriptions-item label="匹配度">
            <el-progress :percentage="Math.round(result.confidence * 100)" />
          </el-descriptions-item>
          <el-descriptions-item label="诊断依据">{{ result.reason }}</el-descriptions-item>
          <el-descriptions-item label="建议">
            <ul>
              <li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li>
            </ul>
          </el-descriptions-item>
        </el-descriptions>
        <div class="action-bar" style="margin-top:16px;">
          <el-button type="success" size="large" @click="goToRegistration">
            <el-icon><Select /></el-icon>
            去挂号
          </el-button>
          <el-button @click="reset">重新分诊</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { MagicStick, Promotion, Select } from "@element-plus/icons-vue"
import { triageConsult } from "@/api/triage"
import { callAIService } from "@/api"
import type { TriageResult } from "@/types"

const router = useRouter()
const symptom = ref("")
const loading = ref(false)
const result = ref<TriageResult | null>(null)
const fallbackMode = ref(false)

async function handleTriage() {
  if (!symptom.value.trim()) {
    ElMessage.warning("请先描述您的症状")
    return
  }
  loading.value = true
  try {
    const res = await callAIService(
      () => triageConsult({ symptom: symptom.value }),
      {
        loadingText: "AI 正在分析您的症状...",
        fallbackMessage: "AI 分诊服务暂时不可用",
        allowFallback: true,
      }
    )
    if (res.fallback) {
      fallbackMode.value = true
    } else if (res.success && res.data) {
      result.value = res.data as TriageResult
    }
  } finally {
    loading.value = false
  }
}

function goToRegistration() {
  router.push("/patient/registration")
}

function reset() {
  symptom.value = ""
  result.value = null
  fallbackMode.value = false
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 8px; }
.page-desc { color: #909399; margin-bottom: 24px; }
.action-bar { margin-top: 16px; display: flex; gap: 12px; }
.ai-result ul { margin: 0; padding-left: 20px; }
.ai-result li { margin-bottom: 4px; line-height: 1.6; }
</style>

<template>
  <div class="triage-page page-container">
    <div class="page-title-row">
      <div>
        <h2 class="page-title">智能分诊</h2>
        <p class="page-desc">请描述您的症状，系统将智能匹配科室与医生</p>
      </div>
    </div>

    <!-- 紧急提示 -->
    <el-alert type="error" :closable="false" class="emergency-alert">
      <template #title>
        <el-icon><WarningFilled /></el-icon>
        <span>如遇胸痛、大出血、呼吸困难、昏迷等危急情况，请立即前往急诊或拨打 <strong>120</strong>。本系统仅提供参考，不代替医生诊断。</span>
      </template>
    </el-alert>

    <div class="triage-card">
      <div class="input-section">
        <div class="input-label">
          <el-icon><EditPen /></el-icon>
          <span>症状描述</span>
        </div>
        <el-input
          v-model="symptoms"
          type="textarea"
          :rows="4"
          placeholder="请详细描述您的症状，例如：胸痛伴气短、头痛三天、发烧38.5度..."
          maxlength="500"
          show-word-limit
        />
        <div class="symptom-quick">
          <span>快速输入（可多选）：</span>
          <el-check-tag
            v-for="s in quickSymptoms"
            :key="s"
            :checked="selectedQuickSymptoms.includes(s)"
            @change="toggleQuickSymptom(s)"
            class="quick-tag"
          >
            {{ s }}
          </el-check-tag>
        </div>
        <div class="action-bar">
          <el-button type="primary" size="large" :loading="loading" :disabled="!symptoms.trim()" @click="handleTriage">
            <el-icon><MagicStick /></el-icon>
            AI 智能分诊
          </el-button>
        </div>
      </div>

      <el-alert v-if="fallbackMode" title="AI 服务暂时不可用，您可以手动选择科室进行挂号" type="warning" show-icon :closable="false" class="fallback-alert">
        <template #default>
          <el-button size="small" type="warning" @click="goToRegistration">去挂号（手动选择）</el-button>
        </template>
      </el-alert>

      <!-- 追问提示 -->
      <el-alert
        v-if="showFollowUp"
        type="info"
        show-icon
        :closable="false"
        class="followup-alert"
      >
        <template #title>
          <el-icon><QuestionFilled /></el-icon>
          <span>为更精准分诊，请补充以下信息：</span>
        </template>
        <div class="followup-questions">
          <p v-for="q in followUpQuestions" :key="q">{{ q }}</p>
        </div>
        <el-input
          v-model="followUpAnswer"
          type="textarea"
          :rows="2"
          placeholder="请补充描述上述问题..."
          class="followup-input"
        />
        <el-button type="primary" size="small" @click="handleFollowUpSubmit">提交补充</el-button>
      </el-alert>

      <!-- 分诊结果 -->
      <transition name="fade">
        <div v-if="result && !fallbackMode && !showFollowUp" class="result-section">
          <div class="result-divider">
            <el-icon><Promotion /></el-icon>
            <span>分诊结果</span>
          </div>

          <!-- 无法判断时显示追问 -->
          <el-alert
            v-if="result.needFollowUp"
            type="info"
            show-icon
            :closable="false"
            class="result-followup"
          >
            <template #title>
              <span>{{ result.reasoning }}</span>
            </template>
            <div v-if="result.followUpQuestions?.length">
              <p class="followup-prompt">请补充以下信息：</p>
              <p v-for="q in result.followUpQuestions" :key="q" class="followup-q-item">• {{ q }}</p>
              <el-input
                v-model="followUpAnswer"
                type="textarea"
                :rows="2"
                placeholder="请描述..."
                class="followup-input"
              />
              <el-button type="primary" size="small" @click="handleFollowUpSubmit" class="followup-submit-btn">提交</el-button>
            </div>
          </el-alert>

          <!-- 正常结果 -->
          <template v-else>
            <div class="result-grid">
              <div class="result-item">
                <span class="result-label">推荐科室</span>
                <el-tag type="primary" size="large" class="result-value-tag">{{ result.department }}</el-tag>
              </div>
              <div class="result-item result-item-full" v-if="result.confidence !== undefined">
                <span class="result-label">置信度</span>
                <div class="confidence-bar-wrap">
                  <el-progress
                    :percentage="result.confidence"
                    :color="getConfidenceColor(result.confidence)"
                    :format="format => `${result.confidence}%`"
                    style="width: 200px;"
                  />
                  <span class="confidence-hint">
                    {{ result.confidence >= 80 ? '高置信度' : result.confidence >= 60 ? '中置信度' : '低置信度，建议补充症状描述' }}
                  </span>
                </div>
              </div>
              <div class="result-item result-item-full">
                <span class="result-label">诊断依据</span>
                <p class="result-text">{{ result.reasoning }}</p>
              </div>
              <div class="result-item result-item-full" v-if="result.doctors?.length">
                <span class="result-label">推荐医生</span>
                <div class="doctor-list">
                  <div v-for="doc in result.doctors" :key="doc.id" class="doctor-item">
                    <span class="doctor-name">{{ doc.name }}</span>
                    <el-tag size="small">{{ doc.title }}</el-tag>
                    <span class="doctor-hospital">{{ doc.hospital }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 免责声明 -->
            <el-alert type="info" :closable="false" class="disclaimer-alert">
              <span>以上建议基于您的症状初步分析，准确率约 {{ result.confidence || 80 }}%，最终请以医生诊断为准。</span>
            </el-alert>

            <div class="action-bar">
              <el-button type="primary" size="large" @click="goToRegistration">
                <el-icon><Select /></el-icon> 去挂号
              </el-button>
              <el-button @click="reset">重新分诊</el-button>
            </div>
          </template>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { MagicStick, Promotion, Select, EditPen, WarningFilled, QuestionFilled } from "@element-plus/icons-vue"
import { triageConsult } from "@/api/triage"
import { callAIService } from "@/api"
import { useUserStore } from "@/stores/user"
import type { TriageResult } from "@/types"

const router = useRouter()
const userStore = useUserStore()
const symptoms = ref("")
const loading = ref(false)
const result = ref<TriageResult | null>(null)
const fallbackMode = ref(false)
const showFollowUp = ref(false)
const followUpQuestions = ref<string[]>([])
const followUpAnswer = ref("")

// 快速输入症状标签（可多选）
const quickSymptoms = [
  "胸痛", "头痛", "发热", "咳嗽", "腹痛",
  "腹泻", "呕吐", "关节痛", "腰痛", "头晕",
  "心悸", "气短", "乏力", "恶心", "鼻塞"
]
const selectedQuickSymptoms = ref<string[]>([])

// 点击快速标签：切换选中状态，已选中的项累加到症状文本
function toggleQuickSymptom(symptom: string) {
  const idx = selectedQuickSymptoms.value.indexOf(symptom)
  if (idx >= 0) {
    selectedQuickSymptoms.value.splice(idx, 1)
  } else {
    selectedQuickSymptoms.value.push(symptom)
  }
  // 合并到症状文本
  symptoms.value = selectedQuickSymptoms.value.join("、")
}

async function handleTriage(isFollowUp = false) {
  if (!symptoms.value.trim()) { ElMessage.warning("请描述症状"); return }
  if (!userStore.userId) { ElMessage.warning("请先登录"); router.push("/patient/login"); return }

  loading.value = true
  if (!isFollowUp) {
    showFollowUp.value = false
    followUpAnswer.value = ""
  }
  try {
    const userInfo = userStore.userInfo
    const res = await callAIService(
      () => triageConsult({
        patientId: userStore.userId,
        age: userInfo?.age || 30,
        gender: userInfo?.gender || "男",
        symptoms: symptoms.value
      }),
      { loadingText: isFollowUp ? "AI 正在给出最终分诊结果..." : "AI 正在分析症状...", fallbackMessage: "AI 分诊服务暂时不可用" }
    )
    if (res.fallback) {
      fallbackMode.value = true
    } else if (res.success && res.data) {
      const data = res.data as TriageResult
      // 如果AI返回需要追问，显示追问界面
      if (data.needFollowUp) {
        showFollowUp.value = true
        followUpQuestions.value = data.followUpQuestions || []
      } else {
        result.value = data
      }
    }
  } finally {
    loading.value = false
  }
}

function extractQuestions(reasoning: string): string[] {
  if (!reasoning) return []
  // 从reasoning中提取追问问题（格式：追问:问题1|问题2）
  const match = reasoning.match(/\[追问\](.*)/)
  if (match) return match[1].split("|").filter(Boolean)
  return []
}

async function handleFollowUpSubmit() {
  if (!followUpAnswer.value.trim()) { ElMessage.warning("请补充描述"); return }
  symptoms.value = symptoms.value + "。" + followUpAnswer.value
  showFollowUp.value = false
  followUpAnswer.value = ""
  // 追问回答后，强制返回最终结果（不再次追问）
  await handleTriage(true)
}

function getConfidenceColor(confidence: number): string {
  if (confidence >= 80) return "#67c23a"
  if (confidence >= 60) return "#e6a23c"
  return "#f56c6c"
}

function goToRegistration() {
  if (result.value) {
    const firstDoctor = result.value.doctors?.[0]
    const triageText = `建议${result.value.department}就诊，${result.value.reasoning}`
    router.push({
      path: "/patient/registration",
      query: {
        department: result.value.department,
        doctorId: firstDoctor?.id?.toString() || "",
        doctorName: firstDoctor?.name || "",
        triageResult: triageText,
      }
    })
  } else {
    router.push("/patient/registration")
  }
}
function reset() {
  symptoms.value = ""
  result.value = null
  fallbackMode.value = false
  showFollowUp.value = false
  followUpAnswer.value = ""
  selectedQuickSymptoms.value = []
}
</script>

<style scoped>
.page-title-row { margin-bottom: 16px; }
.emergency-alert { margin-bottom: 16px; font-size: 13px; }
.emergency-alert :deep(.el-alert__title) { display: flex; align-items: center; gap: 6px; font-size: 13px; line-height: 1.6; }
.triage-card { background: #fff; border-radius: var(--radius); padding: 28px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); }
.input-section { max-width: 700px; }
.input-label { display: flex; align-items: center; gap: 6px; font-size: 15px; font-weight: 600; margin-bottom: 10px; }
.symptom-quick { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; margin-top: 12px; font-size: 13px; color: var(--text-muted); }
.quick-tag { cursor: pointer; user-select: none; }
.fallback-alert { margin-top: 16px; }
.followup-alert { margin-top: 16px; }
.followup-alert :deep(.el-alert__title) { display: flex; align-items: flex-start; gap: 6px; font-size: 13px; }
.followup-questions { margin: 8px 0; font-size: 13px; color: var(--text-secondary); }
.followup-questions p { margin: 4px 0; }
.followup-input { margin: 10px 0; }
.result-section { margin-top: 24px; }
.result-divider { display: flex; align-items: center; gap: 6px; font-size: 16px; font-weight: 600; color: var(--primary); padding-bottom: 14px; border-bottom: 2px solid #ebf4ff; margin-bottom: 18px; }
.result-grid { display: grid; gap: 16px; }
.result-item { display: flex; flex-direction: column; gap: 6px; }
.result-item-full { grid-column: 1 / -1; }
.result-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
.result-text { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
.result-value-tag { font-size: 14px; }
.confidence-bar-wrap { display: flex; align-items: center; gap: 12px; }
.confidence-hint { font-size: 12px; color: var(--text-muted); }
.doctor-list { display: flex; flex-direction: column; gap: 8px; }
.doctor-item { display: flex; align-items: center; gap: 8px; }
.doctor-name { font-size: 14px; font-weight: 500; }
.doctor-hospital { font-size: 12px; color: var(--text-muted); }
.result-followup { margin-bottom: 16px; }
.followup-prompt { font-weight: 600; margin: 8px 0 4px; }
.followup-q-item { font-size: 13px; margin: 4px 0; color: var(--text-secondary); }
.followup-submit-btn { margin-top: 8px; }
.disclaimer-alert { margin-top: 16px; font-size: 12px; }
.action-bar { margin-top: 20px; }
.fade-enter-active { transition: all 0.3s ease; }
.fade-enter-from { opacity: 0; transform: translateY(10px); }
</style>

<template>
  <div class="triage-page page-container">
    <div class="page-title-row">
      <div>
        <h2 class="page-title">智能分诊</h2>
        <p class="page-desc">请描述您的症状，系统将智能匹配科室与医生</p>
      </div>
    </div>

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
          <span>快速输入：</span>
          <el-tag v-for="(s, i) in quickSymptoms" :key="i" :type="symptoms === s ? 'primary' : 'info'" @click="symptoms = s">
            {{ s }}
          </el-tag>
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

      <transition name="fade">
        <div v-if="result && !fallbackMode" class="result-section">
          <div class="result-divider">
            <el-icon><Promotion /></el-icon>
            <span>分诊结果</span>
          </div>
          <div class="result-grid">
            <div class="result-item">
              <span class="result-label">推荐科室</span>
              <el-tag type="primary" size="large" class="result-value-tag">{{ result.department }}</el-tag>
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
          <div class="action-bar">
            <el-button type="primary" size="large" @click="goToRegistration">
              <el-icon><Select /></el-icon> 去挂号
            </el-button>
            <el-button @click="reset">重新分诊</el-button>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { MagicStick, Promotion, Select, EditPen } from "@element-plus/icons-vue"
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

const quickSymptoms = ["胸痛伴气短", "头痛发热", "腹痛腹泻", "咳嗽三天", "关节疼痛"]

async function handleTriage() {
  if (!symptoms.value.trim()) { ElMessage.warning("请描述症状"); return }
  if (!userStore.userId) { ElMessage.warning("请先登录"); router.push("/patient/login"); return }

  loading.value = true
  try {
    const res = await callAIService(
      () => triageConsult({
        patientId: userStore.userId,
        age: 30,
        gender: "男",
        symptoms: symptoms.value
      }),
      { loadingText: "AI 正在分析症状...", fallbackMessage: "AI 分诊服务暂时不可用" }
    )
    if (res.fallback) fallbackMode.value = true
    else if (res.success && res.data) result.value = res.data as TriageResult
  } finally { loading.value = false }
}

function goToRegistration() { router.push("/patient/registration") }
function reset() { symptoms.value = ""; result.value = null; fallbackMode.value = false }
</script>

<style scoped>
.page-title-row { margin-bottom: 20px; }
.triage-card { background: #fff; border-radius: var(--radius); padding: 28px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); }
.input-section { max-width: 700px; }
.input-label { display: flex; align-items: center; gap: 6px; font-size: 15px; font-weight: 600; margin-bottom: 10px; }
.symptom-quick { display: flex; align-items: center; gap: 6px; margin-top: 12px; font-size: 13px; color: var(--text-muted); }
.fallback-alert { margin-top: 16px; }
.result-section { margin-top: 24px; }
.result-divider { display: flex; align-items: center; gap: 6px; font-size: 16px; font-weight: 600; color: var(--primary); padding-bottom: 14px; border-bottom: 2px solid #ebf4ff; margin-bottom: 18px; }
.result-grid { display: grid; gap: 16px; }
.result-item { display: flex; flex-direction: column; gap: 6px; }
.result-item-full { grid-column: 1 / -1; }
.result-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
.result-text { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
.result-value-tag { font-size: 14px; }
.doctor-list { display: flex; flex-direction: column; gap: 8px; }
.doctor-item { display: flex; align-items: center; gap: 8px; }
.doctor-name { font-size: 14px; font-weight: 500; }
.doctor-hospital { font-size: 12px; color: var(--text-muted); }
.fade-enter-active { transition: all 0.3s ease; }
.fade-enter-from { opacity: 0; transform: translateY(10px); }
</style>

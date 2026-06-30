<template>
  <div class="prescription-page page-container">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">开具处方</h2>
      <el-button type="primary" @click="submitPrescription" :loading="submitting">
        <el-icon><Check /></el-icon>
        保存处方
      </el-button>
    </div>

    <el-card class="page-card patient-info-card">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="患者">{{ patientName }}</el-descriptions-item>
        <el-descriptions-item label="患者ID">{{ patientId }}</el-descriptions-item>
        <el-descriptions-item label="挂号ID">{{ registrationId || "-" }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ doctorName }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>药品列表</span>
          <div class="header-actions">
            <el-button type="warning" size="small" @click="handleAiCheck" :loading="checking" :disabled="!hasValidMedicine">
              <el-icon><DocumentChecked /></el-icon>
              AI审核处方
            </el-button>
            <el-button type="primary" size="small" @click="addMedicine">
              <el-icon><Plus /></el-icon>
              添加药品
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="medicineList" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="药品名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.name" placeholder="请输入药品名称" />
          </template>
        </el-table-column>
        <el-table-column label="剂量" width="150">
          <template #default="{ row }">
            <el-input v-model="row.dose" placeholder="如：2片" />
          </template>
        </el-table-column>
        <el-table-column label="用法" width="200">
          <template #default="{ row }">
            <el-input v-model="row.frequency" placeholder="如：每日3次，饭后服用" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" size="small" link @click="removeMedicine($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-form label-width="100px" class="form-extra">
        <el-form-item label="总用量">
          <el-input v-model="dosage" placeholder="如：共7天用量" style="width: 300px" />
        </el-form-item>
        <el-form-item label="用药说明">
          <el-input
            v-model="usage"
            type="textarea"
            :rows="3"
            placeholder="请输入用药说明和注意事项"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- AI审核结果弹窗 -->
    <el-dialog v-model="checkDialogVisible" title="AI 处方审核结果" width="700px" :close-on-click-modal="false">
      <div v-if="checking" class="checking-status">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>AI正在审核处方，请稍候...</span>
      </div>
      <div v-else-if="checkResult" class="check-result-content">
        <el-alert
          :title="checkResult.checkResult"
          :type="getCheckResultType(checkResult.checkResult)"
          :description="getCheckResultDescription(checkResult.checkResult)"
          show-icon
          :closable="false"
          class="check-result-alert"
        />

        <el-divider content-position="left">审核详情</el-divider>

        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="风险等级">
            <el-tag :type="getRiskLevelType(checkResult.riskLevel)" size="large">
              {{ checkResult.riskLevel || '未评估' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">用药建议</el-divider>
        <div class="result-section">
          <div v-if="checkResult.medicationSuggestions" class="result-content">
            {{ checkResult.medicationSuggestions }}
          </div>
          <div v-else class="result-empty">暂无建议</div>
        </div>

        <el-divider content-position="left">药物相互作用检测</el-divider>
        <div class="result-section">
          <div v-if="checkResult.interactionDetection" class="result-content">
            {{ checkResult.interactionDetection }}
          </div>
          <div v-else class="result-empty">未检测到相互作用</div>
        </div>

        <el-divider content-position="left">风险提示</el-divider>
        <div class="result-section">
          <div v-if="checkResult.riskHints" class="result-content risk-hints">
            {{ checkResult.riskHints }}
          </div>
          <div v-else class="result-empty">暂无风险提示</div>
        </div>

        <div v-if="checkResult.riskLevel === '高风险' || checkResult.riskLevel === '中风险'" class="risk-warning">
          <el-alert
            title="请根据审核结果调整处方内容后再次提交"
            type="warning"
            :closable="false"
          />
        </div>
      </div>
      <div v-else class="check-error">
        <el-alert title="审核失败，请稍后重试" type="error" show-icon />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="checkDialogVisible = false">确认已阅</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowLeft, Plus, Check, DocumentChecked, Loading } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { usePrescriptionStore } from "@/stores/prescription"
import { useUserStore } from "@/stores/user"
import { useRegistrationStore } from "@/stores/registration"
import type { AiCheckResult, PrescriptionMedicineItem, RegistrationRecord } from "@/types"

const route = useRoute()
const router = useRouter()
const prescriptionStore = usePrescriptionStore()
const userStore = useUserStore()
const regStore = useRegistrationStore()

const patientId = Number(route.params.patientId)
const registrationId = Number(route.query.registrationId) || 0
const patientName = ref("患者")
const doctorName = ref(userStore.userName || "医生")
const currentRecord = ref<RegistrationRecord | null>(null)
const currentPatientInfo = ref<{ age?: number; gender?: string } | null>(null)

const submitting = ref(false)
const checking = ref(false)
const checkDialogVisible = ref(false)
const checkResult = ref<AiCheckResult | null>(null)

const medicineList = reactive<PrescriptionMedicineItem[]>([
  { name: "", dose: "", frequency: "" },
])
const dosage = ref("")
const usage = ref("")

// 是否有有效药品
const hasValidMedicine = computed(() => {
  return medicineList.some((m) => m.name.trim())
})

onMounted(async () => {
  if (userStore.userName) {
    doctorName.value = userStore.userName
  }
  await loadRegistrationInfo()
})

async function loadRegistrationInfo() {
  if (!registrationId || !userStore.userId) return
  try {
    const list = await regStore.fetchList({ doctorId: userStore.userId })
    const found = (list || []).find((r: RegistrationRecord) => r.id === registrationId)
    if (found) {
      currentRecord.value = found
      patientName.value = found.patientName || "患者"
      // 获取患者详细信息
      await loadPatientInfo(found.patientId)
    }
  } catch (e) {
    console.error("加载挂号信息失败", e)
  }
}

async function loadPatientInfo(pid: number) {
  try {
    const patientRes = await fetch(`/api/patient/detail?id=${pid}`)
    const patientData = await patientRes.json()
    if (patientData.code === 200 && patientData.data) {
      currentPatientInfo.value = {
        age: patientData.data.age,
        gender: patientData.data.gender,
      }
    }
  } catch (e) {
    console.error("加载患者信息失败", e)
  }
}

function addMedicine() {
  medicineList.push({ name: "", dose: "", frequency: "" })
}

function removeMedicine(index: number) {
  if (medicineList.length <= 1) {
    ElMessage.warning("至少保留一个药品")
    return
  }
  medicineList.splice(index, 1)
}

function validate(): boolean {
  const validMeds = medicineList.filter((m) => m.name.trim())
  if (validMeds.length === 0) {
    ElMessage.warning("请至少添加一个药品")
    return false
  }
  return true
}

function buildMedicineListJson(): string {
  const validMeds = medicineList.filter((m) => m.name.trim())
  return JSON.stringify(validMeds)
}

function buildMedicineDisplayText(): string {
  const validMeds = medicineList.filter((m) => m.name.trim())
  return validMeds
    .map((m) => {
      let text = m.name
      if (m.dose) text += `，剂量：${m.dose}`
      if (m.frequency) text += `，用法：${m.frequency}`
      return text
    })
    .join("\n")
}

async function submitPrescription() {
  if (!validate()) return

  try {
    await ElMessageBox.confirm("确认保存处方吗？", "提示", {
      type: "info",
    })
  } catch {
    return
  }

  submitting.value = true
  try {
    const result = await prescriptionStore.create({
      patientId,
      doctorId: userStore.userId,
      registrationId: registrationId || undefined,
      medicineList: buildMedicineListJson(),
      dosage: dosage.value,
      usage: usage.value,
    })

    // 如果有审核结果，保存审核记录
    if (checkResult.value) {
      try {
        await prescriptionStore.saveCheckResult({
          prescriptionId: result.id,
          checkResult: checkResult.value.checkResult || "",
          medicationSuggestions: checkResult.value.medicationSuggestions || "",
          interactionDetection: checkResult.value.interactionDetection || "",
          riskLevel: checkResult.value.riskLevel || "",
          riskHints: checkResult.value.riskHints || "",
        })
      } catch (e) {
        console.warn("保存审核记录失败", e)
      }
    }

    ElMessage.success("处方保存成功")
    router.back()
  } catch (e: any) {
    ElMessage.error(e?.message || "保存失败")
  } finally {
    submitting.value = false
  }
}

// AI审核处方
async function handleAiCheck() {
  if (!validate()) {
    ElMessage.warning("请先添加至少一个药品")
    return
  }

  checkDialogVisible.value = true
  checking.value = true
  checkResult.value = null

  try {
    // 构建药品列表文本
    const medicineText = buildMedicineDisplayText()

    // 构建患者信息
    const patientInfo = [
      `患者ID: ${patientId}`,
      `患者姓名: ${patientName.value}`,
      currentPatientInfo.value?.age ? `年龄: ${currentPatientInfo.value.age}岁` : "",
      currentPatientInfo.value?.gender ? `性别: ${currentPatientInfo.value.gender}` : "",
      currentRecord.value?.symptom ? `主诉症状: ${currentRecord.value.symptom}` : "",
      currentRecord.value?.department ? `就诊科室: ${currentRecord.value.department}` : "",
    ]
      .filter(Boolean)
      .join("\n")

    // 调用AI审核接口
    const result = await prescriptionStore.checkWithAi(medicineText, patientInfo)

    if (result) {
      checkResult.value = result
    } else {
      checkResult.value = {
        checkResult: "审核服务暂不可用",
        riskLevel: "未知",
        medicationSuggestions: "",
        interactionDetection: "",
        riskHints: "",
      }
    }
  } catch (e: any) {
    console.error("AI审核失败", e)
    checkResult.value = {
      checkResult: "审核失败",
      riskLevel: "未知",
      medicationSuggestions: "",
      interactionDetection: "",
      riskHints: e?.message || "AI审核服务暂时不可用，请稍后重试",
    }
  } finally {
    checking.value = false
  }
}

function getCheckResultType(result: string): "success" | "warning" | "error" {
  if (result?.includes("通过") || result?.includes("安全")) return "success"
  if (result?.includes("警告") || result?.includes("注意")) return "warning"
  return "error"
}

function getCheckResultDescription(result: string): string {
  if (result?.includes("通过") || result?.includes("安全"))
    return "处方符合用药安全规范"
  if (result?.includes("警告") || result?.includes("注意"))
    return "处方存在一定风险，请仔细核对后提交"
  return "处方存在较高风险，请根据提示修改后提交"
}

function getRiskLevelType(level: string): "success" | "warning" | "danger" {
  if (level?.includes("低") || level?.includes("安全")) return "success"
  if (level?.includes("中")) return "warning"
  return "danger"
}

function goBack() {
  router.back()
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
.patient-info-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.form-extra {
  margin-top: 20px;
}

/* AI审核结果弹窗样式 */
.checking-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  font-size: 16px;
  color: #606266;
}
.checking-status .el-icon {
  font-size: 24px;
  color: #409eff;
}

.check-result-content {
  padding: 0 8px;
}

.check-result-alert {
  margin-bottom: 16px;
}

.result-section {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 14px;
  color: #303133;
}

.result-empty {
  color: #909399;
  font-style: italic;
}

.risk-hints {
  color: #e6a23c;
  font-weight: 500;
}

.risk-warning {
  margin-top: 20px;
}

.check-error {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

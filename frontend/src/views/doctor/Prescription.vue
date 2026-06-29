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
            <el-button type="success" size="small" @click="handleRecommendMedicine" :loading="recommending">
              <el-icon><MagicStick /></el-icon>
              AI推荐用药
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

    <el-dialog v-model="checkDialogVisible" title="AI 处方审核" width="600px">
      <app-loading :visible="checking" />
      <div v-if="!checking && checkResult">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="审核结果">
            <el-tag :type="checkResult.checkResult === '通过' ? 'success' : 'warning'">
              {{ checkResult.checkResult }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="checkResult.riskLevel === '低风险' ? 'success' : checkResult.riskLevel === '中风险' ? 'warning' : 'danger'">
              {{ checkResult.riskLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用药建议">
            <div style="white-space: pre-wrap; line-height: 1.6;">{{ checkResult.medicationSuggestions }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="相互作用检测">
            <div style="white-space: pre-wrap; line-height: 1.6;">{{ checkResult.interactionDetection }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="风险提示">
            <div style="white-space: pre-wrap; line-height: 1.6; color: #e6a23c;">{{ checkResult.riskHints }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ArrowLeft, Plus, Check, MagicStick } from "@element-plus/icons-vue"
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

const submitting = ref(false)
const checking = ref(false)
const recommending = ref(false)
const checkDialogVisible = ref(false)
const checkResult = ref<AiCheckResult | null>(null)

const medicineList = reactive<PrescriptionMedicineItem[]>([
  { name: "", dose: "", frequency: "" },
])
const dosage = ref("")
const usage = ref("")

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
    }
  } catch (e) {
    console.error("加载挂号信息失败", e)
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
    await prescriptionStore.create({
      patientId,
      doctorId: userStore.userId,
      registrationId: registrationId || undefined,
      medicineList: buildMedicineListJson(),
      dosage: dosage.value,
      usage: usage.value,
    })
    ElMessage.success("处方保存成功")
    router.back()
  } catch (e: any) {
    ElMessage.error(e?.message || "保存失败")
  } finally {
    submitting.value = false
  }
}

async function handleRecommendMedicine() {
  if (!currentRecord.value) {
    ElMessage.warning("未获取到患者挂号信息")
    return
  }

  const symptomText = currentRecord.value.symptom || ""
  if (!symptomText.trim()) {
    ElMessage.warning("患者暂无自述症状，无法进行AI推荐")
    return
  }

  recommending.value = true
  try {
    const result = await prescriptionStore.recommend({
      symptoms: symptomText,
      diagnosis: "",
      department: currentRecord.value.department || "",
    })

    if (result && result.length > 0) {
      medicineList.splice(0, medicineList.length, ...result)
      ElMessage.success(`AI已推荐${result.length}种药品，您可以调整后保存`)
    } else {
      ElMessage.info("AI暂未推荐药品，请手动添加")
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "AI推荐失败，请手动添加药品")
  } finally {
    recommending.value = false
  }
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
</style>

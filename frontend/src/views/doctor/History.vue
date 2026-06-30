<template>
  <div class="history-page page-container">
    <div class="page-header">
      <el-button type="primary" text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">历史记录</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 问诊记录 -->
      <el-tab-pane label="问诊记录" name="consultation">
        <div class="search-bar">
          <el-input
            v-model="consultSearchKeyword"
            placeholder="搜索患者姓名或挂号号"
            clearable
            style="width: 200px; margin-bottom: 12px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <app-loading :visible="consultLoading" />
        <div v-if="!consultLoading && filteredConsultRecords.length === 0">
          <app-empty :description="consultSearchKeyword ? '未找到匹配的问诊记录' : '暂无问诊记录'" />
        </div>
        <el-table v-else :data="filteredConsultRecords" stripe border>
          <el-table-column prop="id" label="记录号" width="100" />
          <el-table-column prop="registrationId" label="挂号号" width="100" />
          <el-table-column label="诊断" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.diagnosis || '-' }}</template>
          </el-table-column>
          <el-table-column label="主诉" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.chiefComplaint || '-' }}</template>
          </el-table-column>
          <el-table-column label="来源" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.aiRecommended" type="warning">AI推荐</el-tag>
              <el-tag v-else type="info">手动</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.updatedAt || row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewConsultation(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 处方记录 -->
      <el-tab-pane label="处方记录" name="prescription">
        <div class="search-bar">
          <el-input
            v-model="prescSearchKeyword"
            placeholder="搜索患者姓名或处方号"
            clearable
            style="width: 200px; margin-bottom: 12px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <app-loading :visible="presLoading" />
        <div v-if="!presLoading && filteredPrescriptions.length === 0">
          <app-empty :description="prescSearchKeyword ? '未找到匹配的处方记录' : '暂无处方记录'" />
        </div>
        <el-table v-else :data="filteredPrescriptions" stripe border>
          <el-table-column prop="id" label="处方号" width="100" />
          <el-table-column prop="patientName" label="患者姓名" width="100" />
          <el-table-column prop="doctorName" label="开方医生" width="100" />
          <el-table-column label="AI审核" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.hasCheckResult" :type="getRiskTagType(row.checkRiskLevel)" size="small">
                {{ row.checkRiskLevel || '已审核' }}
              </el-tag>
              <el-tag v-else type="info" size="small">未审核</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="开具时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="viewPrescription(row)">查看处方</el-button>
                <el-button v-if="row.hasCheckResult" type="warning" size="small" @click="viewCheckResult(row)">
                  审核详情
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 病历记录 -->
      <el-tab-pane label="病历记录" name="record">
        <div class="search-bar">
          <el-input
            v-model="recordSearchKeyword"
            placeholder="搜索患者姓名或ID"
            clearable
            style="width: 200px; margin-bottom: 12px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <app-loading :visible="recLoading" />
        <div v-if="!recLoading && filteredMedicalRecords.length === 0">
          <app-empty :description="recordSearchKeyword ? '未找到匹配的患者病历' : '暂无病历记录'" />
        </div>
        <el-table v-else :data="filteredMedicalRecords" stripe border>
          <el-table-column prop="id" label="病历号" width="100" />
          <el-table-column prop="patientName" label="患者姓名" width="100" />
          <el-table-column prop="doctorName" label="接诊医生" width="100" />
          <el-table-column prop="diagnosis" label="诊断" min-width="150" show-overflow-tooltip />
          <el-table-column label="来源" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.aiGenerated" type="warning">AI生成</el-tag>
              <el-tag v-else type="info">手动</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="viewRecord(row)">查看病历</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 问诊记录详情弹窗 -->
    <el-dialog v-model="consultDialogVisible" title="问诊记录详情" width="700px" :close-on-click-modal="false">
      <div class="consultation-record-form" v-if="currentConsultation">
        <div class="record-section">
          <div class="section-label">主诉</div>
          <div class="section-content">{{ currentConsultation.chiefComplaint || '无' }}</div>
        </div>
        <div class="record-section">
          <div class="section-label">现病史</div>
          <div class="section-content">{{ currentConsultation.presentIllness || '无' }}</div>
        </div>
        <div class="record-section">
          <div class="section-label">既往史</div>
          <div class="section-content">{{ currentConsultation.pastHistory || '无' }}</div>
        </div>
        <div class="record-section">
          <div class="section-label">体格检查</div>
          <div class="section-content">{{ currentConsultation.physicalExamination || '无' }}</div>
        </div>
        <div class="record-section">
          <div class="section-label ">初步诊断</div>
          <div class="section-content ">{{ currentConsultation.diagnosis || '无' }}</div>
        </div>
        <div class="record-section">
          <div class="section-label">治疗意见</div>
          <div class="section-content">{{ currentConsultation.treatmentPlan || '无' }}</div>
        </div>
        <div class="source-tag" v-if="currentConsultation.aiRecommended">
          <el-tag type="warning" size="large">* 包含AI推荐内容</el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 处方详情弹窗 - 处方单格式 -->
    <el-dialog v-model="prescriptionDialogVisible" title="处方笺" width="700px" :close-on-click-modal="false">
      <div class="prescription-form" v-if="currentPrescription">
        <!-- AI审核结果提示 -->
        <el-alert
          v-if="currentPrescriptionCheckResult"
          :title="currentPrescriptionCheckResult.checkResult"
          :type="getCheckResultType(currentPrescriptionCheckResult.checkResult)"
          :description="`风险等级：${currentPrescriptionCheckResult.riskLevel || '未知'}`"
          show-icon
          :closable="false"
          class="check-result-alert"
        />

        <!-- 处方单头部 -->
        <div class="prescription-header">
          <h3>医 疗 处 方 笺</h3>
          <div class="prescription-meta">
            <span>处方编号：{{ currentPrescription.id }}</span>
            <span>开具时间：{{ formatDateTime(currentPrescription.createTime) }}</span>
          </div>
        </div>

        <!-- 患者和医生信息 -->
        <table class="info-table">
          <tr>
            <td class="label">患者姓名</td>
            <td>{{ currentPrescription.patientName }}</td>
            <td class="label">处方医生</td>
            <td>{{ currentPrescription.doctorName }}</td>
          </tr>
          <tr>
            <td class="label">处方状态</td>
            <td colspan="3">
              <el-tag :type="getStatusTag(currentPrescription.status)">{{ getStatusLabel(currentPrescription.status) }}</el-tag>
            </td>
          </tr>
        </table>

        <!-- 药品清单 -->
        <div class="section-title">Rp</div>
        <table class="medicine-table">
          <thead>
            <tr>
              <th style="width: 35%">药品名称</th>
              <th style="width: 20%">剂量</th>
              <th style="width: 30%">用法</th>
              <th style="width: 15%">数量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in parsedMedicineList" :key="index">
              <td>{{ item.name || item.medicineName || '-' }}</td>
              <td>{{ item.dose || item.dosage || '-' }}</td>
              <td>{{ item.frequency || item.usage || '-' }}</td>
              <td>-</td>
            </tr>
            <tr v-if="parsedMedicineList.length === 0">
              <td colspan="4" class="empty-cell">暂无药品信息</td>
            </tr>
          </tbody>
        </table>

        <!-- 用法说明 -->
        <table class="info-table mt-20" v-if="currentPrescription.usage">
          <tr>
            <td class="label" style="width: 100px;">用药说明</td>
            <td>{{ currentPrescription.usage }}</td>
          </tr>
        </table>

        <!-- AI审核详情 -->
        <div v-if="currentPrescriptionCheckResult" class="ai-check-details">
          <el-divider content-position="left">AI审核详情</el-divider>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="审核结果">
              <el-tag :type="getCheckResultType(currentPrescriptionCheckResult.checkResult)">
                {{ currentPrescriptionCheckResult.checkResult }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskTagType(currentPrescriptionCheckResult.riskLevel)" size="large">
                {{ currentPrescriptionCheckResult.riskLevel || '未知' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用药建议">
              <div style="white-space: pre-wrap;">{{ currentPrescriptionCheckResult.medicationSuggestions || '无' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="相互作用检测">
              <div style="white-space: pre-wrap;">{{ currentPrescriptionCheckResult.interactionDetection || '未检测到相互作用' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="风险提示">
              <div style="white-space: pre-wrap; color: #e6a23c;">{{ currentPrescriptionCheckResult.riskHints || '暂无' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 处方单底部 -->
        <div class="prescription-footer">
          <div class="signature-line">
            <span>医生签名：{{ currentPrescription.doctorName }}</span>
            <span>日期：{{ formatDateTime(currentPrescription.createTime) }}</span>
          </div>
          <div class="hospital-info">智慧云脑诊疗平台</div>
        </div>
      </div>
    </el-dialog>

    <!-- 审核详情弹窗 -->
    <el-dialog v-model="checkResultDialogVisible" title="AI处方审核详情" width="600px">
      <div v-if="currentCheckResult" class="check-result-detail">
        <el-alert
          :title="currentCheckResult.checkResult"
          :type="getCheckResultType(currentCheckResult.checkResult)"
          :description="`风险等级：${currentCheckResult.riskLevel || '未知'}`"
          show-icon
          :closable="false"
          class="check-result-alert"
        />

        <el-divider content-position="left">审核详情</el-divider>

        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="风险等级">
            <el-tag :type="getRiskTagType(currentCheckResult.riskLevel)" size="large">
              {{ currentCheckResult.riskLevel || '未知' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">用药建议</el-divider>
        <div class="result-section">
          <div v-if="currentCheckResult.medicationSuggestions" class="result-content">
            {{ currentCheckResult.medicationSuggestions }}
          </div>
          <div v-else class="result-empty">暂无建议</div>
        </div>

        <el-divider content-position="left">药物相互作用检测</el-divider>
        <div class="result-section">
          <div v-if="currentCheckResult.interactionDetection" class="result-content">
            {{ currentCheckResult.interactionDetection }}
          </div>
          <div v-else class="result-empty">未检测到相互作用</div>
        </div>

        <el-divider content-position="left">风险提示</el-divider>
        <div class="result-section">
          <div v-if="currentCheckResult.riskHints" class="result-content risk-hints">
            {{ currentCheckResult.riskHints }}
          </div>
          <div v-else class="result-empty">暂无风险提示</div>
        </div>
      </div>
      <div v-else>
        <el-empty description="暂无审核结果" />
      </div>
    </el-dialog>

    <!-- 病历详情弹窗 - 正式病历表格式 -->
    <el-dialog v-model="recordDialogVisible" title="门诊病历" width="750px" :close-on-click-modal="false">
      <div class="medical-record-form" v-if="currentRecord">
        <!-- 病历头部 -->
        <div class="record-header">
          <h3>门 诊 病 历</h3>
          <div class="record-meta">
            <span>病历编号：{{ currentRecord.id }}</span>
            <span>就诊时间：{{ formatDateTime(currentRecord.createTime) }}</span>
          </div>
        </div>

        <!-- 基本信息 -->
        <table class="info-table">
          <tr>
            <td class="label">患者姓名</td>
            <td>{{ currentRecord.patientName }}</td>
            <td class="label">接诊医生</td>
            <td>{{ currentRecord.doctorName }}</td>
          </tr>
        </table>

        <!-- 主诉 -->
        <div class="record-section">
          <div class="section-label">主诉（Chief Complaint）</div>
          <div class="section-content">
            {{ currentRecord.chiefComplaint || '无' }}
          </div>
        </div>

        <!-- 现病史 -->
        <div class="record-section">
          <div class="section-label">现病史（Present Illness）</div>
          <div class="section-content">
            {{ currentRecord.presentIllness || '无' }}
          </div>
        </div>

        <!-- 既往史 -->
        <div class="record-section">
          <div class="section-label">既往史（Past History）</div>
          <div class="section-content">
            {{ currentRecord.pastHistory || '无' }}
          </div>
        </div>

        <!-- 体格检查 -->
        <div class="record-section">
          <div class="section-label">体格检查（Physical Examination）</div>
          <div class="section-content">
            {{ currentRecord.physicalExamination || '无' }}
          </div>
        </div>

        <!-- 初步诊断 -->
        <div class="record-section">
          <div class="section-label">初步诊断（Preliminary Diagnosis）</div>
          <div class="section-content">
            {{ currentRecord.diagnosis || '无' }}
          </div>
        </div>

        <!-- 治疗意见 -->
        <div class="record-section">
          <div class="section-label">治疗意见（Treatment Plan）</div>
          <div class="section-content">
            {{ currentRecord.treatmentPlan || '无' }}
          </div>
        </div>

        <!-- 来源标识 -->
        <div class="source-tag" v-if="currentRecord.aiGenerated">
          <el-tag type="warning" size="large">* 此病历由AI辅助生成</el-tag>
        </div>

        <!-- 病历底部签名 -->
        <div class="record-footer">
          <div class="signature-line">
            <span>医生签名：{{ currentRecord.doctorName }}</span>
            <span>日期：{{ formatDateTime(currentRecord.createTime) }}</span>
          </div>
          <div class="hospital-info">智慧云脑诊疗平台</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import { ArrowLeft, Search } from "@element-plus/icons-vue"
import { usePrescriptionStore } from "@/stores/prescription"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"
import type { PrescriptionRecord, MedicalRecord, AiCheckResult } from "@/types"
import { ElMessage } from "element-plus"
import { getConsultationRecordList, type ConsultationRecordData } from "@/api/doctor"

const router = useRouter()
const presStore = usePrescriptionStore()
const recordStore = useMedicalRecordStore()
const userStore = useUserStore()

function goBack() {
  router.push("/doctor/patients")
}

const activeTab = ref("consultation")
const consultLoading = ref(false)
const presLoading = ref(false)
const recLoading = ref(false)
const consultationRecords = ref<ConsultationRecordData[]>([])
const prescriptions = ref<(PrescriptionRecord & { hasCheckResult?: boolean; checkRiskLevel?: string })[]>([])
const medicalRecords = ref<MedicalRecord[]>([])

// 问诊记录弹窗
const consultDialogVisible = ref(false)
const currentConsultation = ref<ConsultationRecordData | null>(null)

// 处方弹窗
const prescriptionDialogVisible = ref(false)
const currentPrescription = ref<PrescriptionRecord | null>(null)
const currentPrescriptionCheckResult = ref<AiCheckResult | null>(null)

// 审核详情弹窗
const checkResultDialogVisible = ref(false)
const currentCheckResult = ref<AiCheckResult | null>(null)

// 解析药品列表
const parsedMedicineList = ref<any[]>([])

const recordDialogVisible = ref(false)
const currentRecord = ref<MedicalRecord | null>(null)
const recordSearchKeyword = ref("")
const consultSearchKeyword = ref("")
const prescSearchKeyword = ref("")

// 过滤后的问诊记录
const filteredConsultRecords = computed(() => {
  if (!consultSearchKeyword.value) {
    return consultationRecords.value
  }
  const keyword = consultSearchKeyword.value.toLowerCase()
  return consultationRecords.value.filter((record) => {
    const patientName = (record.patientName || "").toLowerCase()
    const regId = String(record.registrationId || "")
    const diagnosis = (record.diagnosis || "").toLowerCase()
    return patientName.includes(keyword) || regId.includes(keyword) || diagnosis.includes(keyword)
  })
})

// 过滤后的处方记录
const filteredPrescriptions = computed(() => {
  if (!prescSearchKeyword.value) {
    return prescriptions.value
  }
  const keyword = prescSearchKeyword.value.toLowerCase()
  return prescriptions.value.filter((record) => {
    const patientName = (record.patientName || "").toLowerCase()
    const prescId = String(record.id || "")
    const doctorName = (record.doctorName || "").toLowerCase()
    return patientName.includes(keyword) || prescId.includes(keyword) || doctorName.includes(keyword)
  })
})

// 过滤后的病历记录
const filteredMedicalRecords = computed(() => {
  if (!recordSearchKeyword.value) {
    return medicalRecords.value
  }
  const keyword = recordSearchKeyword.value.toLowerCase()
  return medicalRecords.value.filter((record) => {
    const patientName = (record.patientName || "").toLowerCase()
    const patientId = String(record.patientId || "")
    const diagnosis = (record.diagnosis || "").toLowerCase()
    return patientName.includes(keyword) || patientId.includes(keyword) || diagnosis.includes(keyword)
  })
})

onMounted(async () => {
  if (!userStore.userId) {
    ElMessage.warning("请先登录")
    return
  }

  // 加载问诊记录
  consultLoading.value = true
  try {
    const res = await getConsultationRecordList(userStore.userId)
    consultationRecords.value = (res.data as ConsultationRecordData[]) || []
  } finally { consultLoading.value = false }

  // 加载处方记录
  presLoading.value = true
  try {
    const res = await presStore.fetchList({ doctorId: userStore.userId })
    // 获取每个处方的审核结果
    const presList = res || []
    for (const p of presList) {
      const checkResult = await presStore.getCheckResult(p.id)
      p.hasCheckResult = !!checkResult
      p.checkRiskLevel = checkResult?.riskLevel || ''
    }
    prescriptions.value = presList
  } finally { presLoading.value = false }

  // 加载病历记录
  recLoading.value = true
  try {
    const res = await recordStore.fetchList({ doctorId: userStore.userId })
    medicalRecords.value = res || []
  } finally { recLoading.value = false }
})

// 查看问诊记录详情
function viewConsultation(record: ConsultationRecordData) {
  currentConsultation.value = record
  consultDialogVisible.value = true
}

// 查看处方详情
async function viewPrescription(prescription: PrescriptionRecord) {
  currentPrescription.value = {
    ...prescription,
    createTime: prescription.createTime || (prescription as any).createdAt,
  }
  // 解析药品列表
  try {
    if (prescription.medicineList) {
      parsedMedicineList.value = JSON.parse(prescription.medicineList)
    } else {
      parsedMedicineList.value = []
    }
  } catch (e) {
    parsedMedicineList.value = []
  }
  // 获取审核结果
  currentPrescriptionCheckResult.value = await presStore.getCheckResult(prescription.id)
  prescriptionDialogVisible.value = true
}

// 查看审核详情
async function viewCheckResult(prescription: any) {
  const result = await presStore.getCheckResult(prescription.id)
  currentCheckResult.value = result
  checkResultDialogVisible.value = true
}

// 查看病历详情
function viewRecord(record: MedicalRecord) {
  currentRecord.value = {
    ...record,
    createTime: record.createTime || (record as any).createdAt,
  }
  recordDialogVisible.value = true
}

// 获取风险标签类型
function getRiskTagType(level: string | undefined): string {
  if (!level) return 'info'
  if (level.includes('低') || level.includes('安全')) return 'success'
  if (level.includes('中')) return 'warning'
  return 'danger'
}

// 获取审核结果类型
function getCheckResultType(result: string | undefined): "success" | "warning" | "error" | "info" {
  if (!result) return 'info'
  if (result.includes('通过') || result.includes('安全')) return 'success'
  if (result.includes('警告') || result.includes('注意')) return 'warning'
  return 'error'
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.page-title { font-size: 22px; color: #303133; margin: 0; }

/* 操作列按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.action-buttons .el-button + .el-button {
  margin-left: 0;
}

/* 问诊记录详情样式 */
.consultation-record-form {
  padding: 10px 0;
}

/* ===== 处方单样式 ===== */
.prescription-form {
  background: #fff;
  padding: 20px;
}

.prescription-header {
  text-align: center;
  border-bottom: 2px solid #333;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.prescription-header h3 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 10px 0;
  letter-spacing: 4px;
}

.prescription-meta {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 13px;
  color: #666;
}

.info-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.info-table td {
  border: 1px solid #ddd;
  padding: 8px 12px;
  font-size: 14px;
}

.info-table .label {
  background: #f5f5f5;
  font-weight: 500;
  width: 100px;
  text-align: center;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin: 20px 0 10px 0;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.medicine-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.medicine-table th,
.medicine-table td {
  border: 1px solid #333;
  padding: 10px 8px;
  text-align: center;
  font-size: 14px;
}

.medicine-table th {
  background: #f5f5f5;
  font-weight: 500;
}

.medicine-table .empty-cell {
  color: #999;
  font-style: italic;
}

.prescription-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

.signature-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.hospital-info {
  text-align: center;
  color: #999;
  font-size: 13px;
}

.mt-20 { margin-top: 20px; }

/* ===== 病历表样式 ===== */
.medical-record-form {
  background: #fff;
  padding: 20px;
}

.record-header {
  text-align: center;
  border-bottom: 2px solid #333;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.record-header h3 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 10px 0;
  letter-spacing: 4px;
}

.record-meta {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 13px;
  color: #666;
}

.record-section {
  margin-bottom: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.section-label {
  background: #409eff;
  color: #fff;
  padding: 8px 15px;
  font-size: 14px;
  font-weight: 500;
}

.section-label.highlight {
  background: #e6a23c;
}

.section-content {
  padding: 15px;
  font-size: 14px;
  line-height: 1.8;
  min-height: 50px;
  color: #333;
  background: #fff;
}


.source-tag {
  margin-top: 20px;
  text-align: center;
}

.record-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

/* 审核详情样式 */
.check-result-alert {
  margin-bottom: 16px;
}

.check-result-detail {
  padding: 0 8px;
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

/* AI审核详情样式 */
.ai-check-details {
  margin-top: 20px;
}

/* 病历搜索样式 */
.search-bar {
  margin-bottom: 8px;
}
</style>

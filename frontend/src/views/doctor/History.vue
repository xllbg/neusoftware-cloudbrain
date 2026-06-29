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
      <!-- 处方记录 -->
      <el-tab-pane label="处方记录" name="prescription">
        <app-loading :visible="presLoading" />
        <div v-if="!presLoading && prescriptions.length === 0">
          <app-empty description="暂无处方记录" />
        </div>
        <el-table v-else :data="prescriptions" stripe border>
          <el-table-column prop="id" label="处方号" width="100" />
          <el-table-column prop="patientName" label="患者姓名" width="100" />
          <el-table-column prop="doctorName" label="开方医生" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="开具时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewPrescription(row)">查看处方</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 病历记录 -->
      <el-tab-pane label="病历记录" name="record">
        <app-loading :visible="recLoading" />
        <div v-if="!recLoading && medicalRecords.length === 0">
          <app-empty description="暂无病历记录" />
        </div>
        <el-table v-else :data="medicalRecords" stripe border>
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
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewRecord(row)">查看病历</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 处方详情弹窗 - 处方单格式 -->
    <el-dialog v-model="prescriptionDialogVisible" title="处方笺" width="700px" :close-on-click-modal="false">
      <div class="prescription-form" v-if="currentPrescription">
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
          <div class="section-content highlight">
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
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { ArrowLeft } from "@element-plus/icons-vue"
import { usePrescriptionStore } from "@/stores/prescription"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"
import type { PrescriptionRecord, MedicalRecord } from "@/types"
import { ElMessage } from "element-plus"

const router = useRouter()
const presStore = usePrescriptionStore()
const recordStore = useMedicalRecordStore()
const userStore = useUserStore()

function goBack() {
  router.push("/doctor/patients")
}

const activeTab = ref("prescription")
const presLoading = ref(false)
const recLoading = ref(false)
const prescriptions = ref<PrescriptionRecord[]>([])
const medicalRecords = ref<MedicalRecord[]>([])

// 处方弹窗
const prescriptionDialogVisible = ref(false)
const currentPrescription = ref<PrescriptionRecord | null>(null)

// 病历弹窗
const recordDialogVisible = ref(false)
const currentRecord = ref<MedicalRecord | null>(null)

// 解析药品列表
const parsedMedicineList = ref<any[]>([])

onMounted(async () => {
  if (!userStore.userId) {
    ElMessage.warning("请先登录")
    return
  }
  presLoading.value = true
  try {
    const res = await presStore.fetchList({ doctorId: userStore.userId })
    prescriptions.value = res || []
  } finally { presLoading.value = false }

  recLoading.value = true
  try {
    const res = await recordStore.fetchList({ doctorId: userStore.userId })
    medicalRecords.value = res || []
  } finally { recLoading.value = false }
})

// 查看处方详情
function viewPrescription(prescription: PrescriptionRecord) {
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
  prescriptionDialogVisible.value = true
}

// 查看病历详情
function viewRecord(record: MedicalRecord) {
  currentRecord.value = {
    ...record,
    createTime: record.createTime || (record as any).createdAt,
  }
  recordDialogVisible.value = true
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

.section-content {
  padding: 15px;
  font-size: 14px;
  line-height: 1.8;
  min-height: 50px;
  color: #333;
  background: #fff;
}

.section-content.highlight {
  background: #fff9e6;
  font-weight: 500;
  color: #b8860b;
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
</style>

<template>
  <div class="patient-list page-container">
    <div class="page-header">
      <h2 class="page-title">挂号患者列表</h2>
      <div class="header-right">
        <el-button type="info" size="small" @click="goToHistory">
          <el-icon><Document /></el-icon>
          历史记录
        </el-button>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <el-card class="page-card filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="科室">
          <el-select v-model="filterForm.department" placeholder="全部科室" clearable style="width: 140px" @change="fetchList">
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="儿科" value="儿科" />
            <el-option label="骨科" value="骨科" />
            <el-option label="皮肤科" value="皮肤科" />
            <el-option label="心内科" value="心内科" />
            <el-option label="呼吸内科" value="呼吸内科" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px" @change="fetchList">
            <el-option label="待接诊" value="pending" />
            <el-option label="接诊中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="filterForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 160px"
            @change="fetchList"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="page-card">
      <app-loading :visible="loading" />
      <div v-if="!loading && records.length === 0">
        <app-empty description="暂无挂号患者" />
      </div>
      <el-table v-else :data="records" stripe border>
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="registrationDate" label="就诊日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="140">
          <template #default="{ row }">{{ formatTimeSlot(row.timeSlot) }}</template>
        </el-table-column>
        <el-table-column prop="symptom" label="症状" min-width="140" show-overflow-tooltip />
        <el-table-column label="AI分诊" width="120">
          <template #default="{ row }">
            <div v-if="row.triageResult">
              <el-tag type="success" size="small">已推荐</el-tag>
              <div class="triage-recommendation">{{ row.triageResult }}</div>
            </div>
            <div v-else class="no-triage">
              <el-tag type="info" size="small">无</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              size="small"
              @click="startConsultation(row)"
            >
              接诊
            </el-button>
            <template v-else-if="row.status === 'in_progress'">
              <el-button
                v-if="row.doctorId === userStore.userId"
                type="success"
                size="small"
                @click="goToConsultation(row)"
              >
                问诊
              </el-button>
              <el-tag v-else type="info" size="small">接诊中</el-tag>
            </template>
            <el-dropdown v-else style="margin-left: 8px">
              <el-button size="small">
                查看
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="viewConsultation(row)">问诊记录</el-dropdown-item>
                  <el-dropdown-item @click="viewMedicalRecord(row)">病历</el-dropdown-item>
                  <el-dropdown-item @click="viewPrescription(row)">处方</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              v-if="row.status === 'completed'"
              type="info"
              size="small"
              style="margin-left: 4px"
              @click="viewConsultation(row)"
            >
              回看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 处方详情弹窗 - 处方单格式 -->
    <el-dialog v-model="prescriptionDialogVisible" title="处方笺" width="700px" :close-on-click-modal="false">
      <app-loading :visible="prescriptionLoading" />
      <div class="prescription-form" v-if="!prescriptionLoading && currentPrescription">
        <div class="prescription-header">
          <h3>医 疗 处 方 笺</h3>
          <div class="prescription-meta">
            <span>处方编号：{{ currentPrescription.id }}</span>
            <span>开具时间：{{ formatDateTime(currentPrescription.createTime) }}</span>
          </div>
        </div>

        <table class="info-table">
          <tbody>
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
          </tbody>
        </table>

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

        <table class="info-table mt-20" v-if="currentPrescription.usage">
          <tbody>
            <tr>
              <td class="label" style="width: 100px;">用药说明</td>
              <td>{{ currentPrescription.usage }}</td>
            </tr>
          </tbody>
        </table>

        <div class="prescription-footer">
          <div class="signature-line">
            <span>医生签名：{{ currentPrescription.doctorName }}</span>
            <span>日期：{{ formatDateTime(currentPrescription.createTime) }}</span>
          </div>
          <div class="hospital-info">智慧云脑诊疗平台</div>
        </div>
      </div>
      <div v-if="!prescriptionLoading && !currentPrescription" class="empty-wrapper">
        <el-empty description="暂无处方记录" />
      </div>
    </el-dialog>

    <!-- 病历详情弹窗 - 门诊病历格式 -->
    <el-dialog v-model="recordDialogVisible" title="门诊病历" width="750px" :close-on-click-modal="false">
      <app-loading :visible="recordLoading" />
      <div class="medical-record-form" v-if="!recordLoading && currentMedicalRecord">
        <div class="record-header">
          <h3>门 诊 病 历</h3>
          <div class="record-meta">
            <span>病历编号：{{ currentMedicalRecord.id }}</span>
            <span>就诊时间：{{ formatDateTime(currentMedicalRecord.createTime) }}</span>
          </div>
        </div>

        <table class="info-table">
          <tbody>
            <tr>
              <td class="label">患者姓名</td>
              <td>{{ currentMedicalRecord.patientName }}</td>
              <td class="label">接诊医生</td>
              <td>{{ currentMedicalRecord.doctorName }}</td>
            </tr>
          </tbody>
        </table>

        <div class="record-section">
          <div class="section-label">主诉（Chief Complaint）</div>
          <div class="section-content">
            {{ currentMedicalRecord.chiefComplaint || '无' }}
          </div>
        </div>

        <div class="record-section">
          <div class="section-label">现病史（Present Illness）</div>
          <div class="section-content">
            {{ currentMedicalRecord.presentIllness || '无' }}
          </div>
        </div>

        <div class="record-section">
          <div class="section-label">既往史（Past History）</div>
          <div class="section-content">
            {{ currentMedicalRecord.pastHistory || '无' }}
          </div>
        </div>

        <div class="record-section">
          <div class="section-label">体格检查（Physical Examination）</div>
          <div class="section-content">
            {{ currentMedicalRecord.physicalExamination || '无' }}
          </div>
        </div>

        <div class="record-section">
          <div class="section-label">初步诊断（Preliminary Diagnosis）</div>
          <div class="section-content highlight">
            {{ currentMedicalRecord.diagnosis || '无' }}
          </div>
        </div>

        <div class="record-section">
          <div class="section-label">治疗意见（Treatment Plan）</div>
          <div class="section-content">
            {{ currentMedicalRecord.treatmentPlan || '无' }}
          </div>
        </div>

        <div class="source-tag" v-if="currentMedicalRecord.aiGenerated">
          <el-tag type="warning" size="large">* 此病历由AI辅助生成</el-tag>
        </div>

        <div class="record-footer">
          <div class="signature-line">
            <span>医生签名：{{ currentMedicalRecord.doctorName }}</span>
            <span>日期：{{ formatDateTime(currentMedicalRecord.createTime) }}</span>
          </div>
          <div class="hospital-info">智慧云脑诊疗平台</div>
        </div>
      </div>
      <div v-if="!recordLoading && !currentMedicalRecord" class="empty-wrapper">
        <el-empty description="暂无病历记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { useRouter } from "vue-router"
import { ArrowDown, Document } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import { useRegistrationStore } from "@/stores/registration"
import { usePrescriptionStore } from "@/stores/prescription"
import { useMedicalRecordStore } from "@/stores/medicalRecord"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel, formatDateTime } from "@/utils/format"
import type { RegistrationRecord, PrescriptionRecord, MedicalRecord } from "@/types"

const router = useRouter()
const regStore = useRegistrationStore()
const presStore = usePrescriptionStore()
const recordStore = useMedicalRecordStore()
const userStore = useUserStore()
const loading = ref(false)
const records = ref<RegistrationRecord[]>([])

const filterForm = reactive({
  department: "",
  status: "",
  date: "",
})

// 处方弹窗
const prescriptionDialogVisible = ref(false)
const prescriptionLoading = ref(false)
const currentPrescription = ref<PrescriptionRecord | null>(null)
const parsedMedicineList = ref<any[]>([])

// 病历弹窗
const recordDialogVisible = ref(false)
const recordLoading = ref(false)
const currentMedicalRecord = ref<MedicalRecord | null>(null)

onMounted(async () => {
  if (!userStore.userId) return
  await fetchList()
})

async function fetchList() {
  if (!userStore.userId) return
  loading.value = true
  try {
    const res = await regStore.fetchList({
      doctorId: userStore.userId,
      status: filterForm.status || undefined,
      department: filterForm.department || undefined,
      date: filterForm.date || undefined,
    })
    records.value = res || []
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filterForm.department = ""
  filterForm.status = ""
  filterForm.date = ""
  fetchList()
}

function formatTimeSlot(slot: string): string {
  if (!slot) return ""
  const map: Record<string, string> = {
    MORNING: "上午",
    AFTERNOON: "下午",
  }
  return map[slot] || slot
}

async function startConsultation(row: RegistrationRecord) {
  if (!userStore.userId) return
  try {
    await regStore.startConsultation(row.id, userStore.userId)
    ElMessage.success("接诊成功")
    router.push(`/doctor/consultation/${row.id}`)
  } catch (e: any) {
    ElMessage.error(e?.message || "接诊失败")
  }
}

function goToConsultation(row: RegistrationRecord) {
  router.push(`/doctor/consultation/${row.id}`)
}

function viewConsultation(row: RegistrationRecord) {
  router.push(`/doctor/consultation/${row.id}`)
}

// 查看处方（弹窗）
async function viewPrescription(row: RegistrationRecord) {
  prescriptionDialogVisible.value = true
  prescriptionLoading.value = true
  currentPrescription.value = null
  parsedMedicineList.value = []

  try {
    const list = await presStore.fetchList({ patientId: row.patientId, doctorId: userStore.userId })
    const found = (list || []).find((p: any) => p.registrationId === row.id)
    if (found) {
      currentPrescription.value = {
        ...found,
        createTime: found.createTime || found.createdAt,
      }
      try {
        if (found.medicineList) {
          parsedMedicineList.value = JSON.parse(found.medicineList)
        }
      } catch (e) {
        parsedMedicineList.value = []
      }
    }
  } catch (e) {
    console.error("加载处方失败", e)
  } finally {
    prescriptionLoading.value = false
  }
}

// 查看病历（弹窗）
async function viewMedicalRecord(row: RegistrationRecord) {
  recordDialogVisible.value = true
  recordLoading.value = true
  currentMedicalRecord.value = null

  try {
    const list = await recordStore.fetchList({ patientId: row.patientId, doctorId: userStore.userId })
    const found = (list || []).find((r: any) => r.registrationId === row.id)
    if (found) {
      currentMedicalRecord.value = {
        ...found,
        createTime: found.createTime || found.createdAt,
      }
    }
  } catch (e) {
    console.error("加载病历失败", e)
  } finally {
    recordLoading.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.push("/doctor/login")
}

function goToHistory() {
  router.push("/doctor/history")
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header-right {
  display: flex;
  gap: 8px;
}
.page-title {
  font-size: 22px;
  color: #303133;
  margin: 0;
}
.filter-card {
  margin-bottom: 16px;
}
.filter-form {
  margin: 0;
}

/* ===== 处方单样式 ===== */
.prescription-form {
  background: #fff;
  padding: 10px;
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
  padding: 10px;
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

.empty-wrapper {
  padding: 40px 0;
}

/* AI分诊样式 */
.triage-recommendation {
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-triage {
  color: #909399;
}
</style>

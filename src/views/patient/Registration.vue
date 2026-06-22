<template>
  <div class="registration-page page-container">
    <h2 class="page-title">在线挂号</h2>

    <el-card class="page-card">
      <el-steps :active="step" align-center finish-status="success" style="margin-bottom: 32px;">
        <el-step title="选择科室" />
        <el-step title="选择医生" />
        <el-step title="选择时间" />
        <el-step title="确认提交" />
      </el-steps>

      <!-- 步骤1：选择科室 -->
      <div v-if="step === 0">
        <h3>请选择科室</h3>
        <el-radio-group v-model="selectedDepartment" class="dept-group">
          <el-radio-button
            v-for="dept in departments"
            :key="dept"
            :value="dept"
          >
            {{ dept }}
          </el-radio-button>
        </el-radio-group>
        <div class="action-bar">
          <el-button type="primary" size="large" :disabled="!selectedDepartment" @click="step = 1">
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤2：选择医生 -->
      <div v-if="step === 1">
        <h3>请选择医生（{{ selectedDepartment }}）</h3>
        <app-loading :visible="loadingDoctors" />
        <div class="doctor-list" v-if="!loadingDoctors">
          <div
            v-for="doc in doctors"
            :key="doc.id"
            class="doctor-card"
            :class="{ selected: selectedDoctorId === doc.id }"
            @click="selectedDoctorId = doc.id"
          >
            <div class="doctor-name">{{ doc.name }}</div>
            <div class="doctor-info">
              <el-tag size="small">{{ doc.title }}</el-tag>
              <span v-if="!doc.available" class="unavailable">（暂停接诊）</span>
            </div>
          </div>
          <app-empty v-if="doctors.length === 0" description="暂无医生信息" />
        </div>
        <div class="action-bar">
          <el-button @click="step = 0">上一步</el-button>
          <el-button type="primary" size="large" :disabled="!selectedDoctorId" @click="step = 2">
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤3：选择时间 -->
      <div v-if="step === 2">
        <h3>选择就诊日期</h3>
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        <h3 style="margin-top: 16px;">选择时间段</h3>
        <el-radio-group v-model="selectedTimeSlot">
          <el-radio value="MORNING">上午（08:00 - 12:00）</el-radio>
          <el-radio value="AFTERNOON">下午（14:00 - 18:00）</el-radio>
        </el-radio-group>
        <div class="action-bar">
          <el-button @click="step = 1">上一步</el-button>
          <el-button type="primary" size="large" :disabled="!selectedDate || !selectedTimeSlot" @click="step = 3">
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤4：确认提交 -->
      <div v-if="step === 3">
        <h3>确认挂号信息</h3>
        <el-descriptions :column="1" border style="margin-top: 16px;">
          <el-descriptions-item label="科室">{{ selectedDepartment }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ selectedDoctorName }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ selectedDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">
            {{ selectedTimeSlot === "MORNING" ? "上午 08:00-12:00" : "下午 14:00-18:00" }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="action-bar">
          <el-button @click="step = 2">上一步</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            确认挂号
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { getDepartments, getDoctorsByDepartment } from "@/api/doctor"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import type { DoctorInfo } from "@/types"

const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const step = ref(0)
const departments = ref<string[]>([])
const doctors = ref<DoctorInfo[]>([])
const loadingDoctors = ref(false)
const selectedDepartment = ref("")
const selectedDoctorId = ref<number | null>(null)
const selectedDate = ref("")
const selectedTimeSlot = ref("MORNING")
const submitting = ref(false)

const selectedDoctorName = computed(() => {
  const doc = doctors.value.find((d) => d.id === selectedDoctorId.value)
  return doc ? `${doc.name}（${doc.title}）` : ""
})

// 加载科室列表
getDepartments().then((res) => {
  departments.value = res.data
})

// 科室变化时加载医生
watch(selectedDepartment, async (dept) => {
  if (!dept) return
  selectedDoctorId.value = null
  loadingDoctors.value = true
  try {
    const res = await getDoctorsByDepartment(dept)
    doctors.value = res.data
  } finally {
    loadingDoctors.value = false
  }
})

async function handleSubmit() {
  if (!userStore.userId) {
    ElMessage.warning("请先登录")
    router.push("/patient/login")
    return
  }
  submitting.value = true
  try {
    await regStore.create({
      patientId: userStore.userId,
      doctorId: selectedDoctorId.value!,
      registrationDate: selectedDate.value,
      timeSlot: selectedTimeSlot.value,
      symptom: "",
    })
    ElMessage.success("挂号成功！")
    router.push("/patient/my-registrations")
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.dept-group { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.action-bar { margin-top: 24px; display: flex; gap: 12px; }
.doctor-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; margin-top: 16px; }
.doctor-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.doctor-card:hover { border-color: #409eff; box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15); }
.doctor-card.selected { border-color: #409eff; background: #ecf5ff; }
.doctor-name { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.doctor-info { display: flex; align-items: center; gap: 8px; }
.unavailable { color: #c0c4cc; font-size: 12px; }
</style>

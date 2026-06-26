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

      <div v-if="step === 0">
        <h3>请选择科室</h3>
        <el-radio-group v-model="selectedDepartment" class="dept-group">
          <el-radio-button v-for="dept in departments" :key="dept" :value="dept">{{ dept }}</el-radio-button>
        </el-radio-group>
        <div class="action-bar">
          <el-button type="primary" size="large" :disabled="!selectedDepartment" @click="step = 1">下一步</el-button>
        </div>
      </div>

      <div v-if="step === 1">
        <h3>请选择医生（{{ selectedDepartment }}）</h3>
        <app-loading :visible="loadingDoctors" />
        <div class="doctor-list" v-if="!loadingDoctors">
          <div v-for="doc in doctors" :key="doc.id" class="doctor-card" :class="{ selected: selectedDoctorId === doc.id }" @click="selectedDoctorId = doc.id">
            <div class="doctor-name">{{ doc.name }}</div>
            <div class="doctor-info">
              <el-tag size="small">{{ doc.title }}</el-tag>
            </div>
          </div>
          <app-empty v-if="doctors.length === 0" description="暂无医生信息" />
        </div>
        <div class="action-bar">
          <el-button @click="step = 0">上一步</el-button>
          <el-button type="primary" size="large" :disabled="!selectedDoctorId" @click="step = 2">下一步</el-button>
        </div>
      </div>

      <div v-if="step === 2">
        <el-alert v-if="selectedDepartment === '急诊科'" type="warning" :closable="false" show-icon style="margin-bottom: 16px;">
          <template #title>
            <strong>急诊挂号说明：</strong>急诊患者需先经过预检分诊护士评估病情危重程度，再办理挂号。请如实描述症状。
          </template>
        </el-alert>
        <h3>选择就诊日期</h3>
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        <h3 style="margin-top: 16px;">选择时间段</h3>
        <el-radio-group v-model="selectedTimeSlot" class="time-slot-group">
          <el-radio label="MORNING">上午（08:00 - 12:00）</el-radio>
          <el-radio label="AFTERNOON">下午（14:00 - 18:00）</el-radio>
        </el-radio-group>
        <div class="action-bar">
          <el-button @click="selectedDepartment === '急诊科' ? step = 0 : step = 1">上一步</el-button>
          <el-button type="primary" size="large" :disabled="!selectedDate || !selectedTimeSlot" @click="step = 3">下一步</el-button>
        </div>
      </div>

      <div v-if="step === 3">
        <h3>确认挂号信息</h3>
        <el-descriptions :column="1" border style="margin-top: 16px;">
          <el-descriptions-item label="科室">{{ selectedDepartment }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ selectedDoctorName }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ selectedDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ selectedTimeSlot === "MORNING" ? "上午 08:00-12:00" : "下午 14:00-18:00" }}</el-descriptions-item>
        </el-descriptions>
        <h3 style="margin-top: 20px;">症状描述（选填）</h3>
        <el-input v-model="symptom" type="textarea" :rows="3" placeholder="请描述您的症状（选填）" maxlength="200" show-word-limit />
        <div class="action-bar">
          <el-button @click="step = 2">上一步</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">确认挂号</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from "element-plus"
import { getDoctorsByDepartment } from "@/api/doctor"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import type { DoctorInfo } from "@/types"

const router = useRouter()
const route = useRoute()
const regStore = useRegistrationStore()
const userStore = useUserStore()

const step = ref(0)
const departments = ref<string[]>([])
const allDoctors = ref<DoctorInfo[]>([])
const doctors = ref<DoctorInfo[]>([])
const loadingDoctors = ref(false)
const selectedDepartment = ref("")
const selectedDoctorId = ref<number | null>(null)
const selectedDate = ref("")
const selectedTimeSlot = ref("MORNING")
const symptom = ref("")
const submitting = ref(false)

const selectedDoctorName = computed(() => {
  if (selectedDepartment.value === "急诊科") {
    return "（急诊挂号，无需指定医生）"
  }
  const doc = doctors.value.find((d) => d.id === selectedDoctorId.value)
  return doc ? `${doc.name}（${doc.title}）` : ""
})

// 从医生列表中提取所有科室
onMounted(async () => {
  try {
    const res = await getDoctorsByDepartment("")
    allDoctors.value = res.data || []
    const deptSet = new Set(allDoctors.value.map((d) => d.department).filter(Boolean))
    departments.value = Array.from(deptSet) as string[]

    // 确保急诊科始终在列表中（即使没有急诊科医生也可以挂号）
    if (!departments.value.includes("急诊科")) {
      departments.value.push("急诊科")
    }

    // 处理从智能分诊页传来的推荐数据
    const query = route.query
    if (query.department) {
      selectedDepartment.value = query.department as string
      // 急诊科不需要选医生，直接跳到时间选择
      if (selectedDepartment.value === "急诊科") {
        step.value = 2
      } else {
        step.value = 1
      }
    }
    if (query.doctorId) {
      selectedDoctorId.value = parseInt(query.doctorId as string)
    }
  } catch {
    departments.value = ["心内科", "呼吸内科", "神经内科", "消化内科", "骨科", "儿科", "内科", "普外科", "急诊科"]
  }
})

// 按科室筛选医生
watch(selectedDepartment, (dept) => {
  if (!dept) return
  selectedDoctorId.value = null
  doctors.value = allDoctors.value.filter((d) => d.department === dept)
  // 急诊科不需要选医生，直接跳到时间选择
  if (dept === "急诊科") {
    step.value = 2
  }
})

async function handleSubmit() {
  if (!userStore.userId) { ElMessage.warning("请先登录"); return }
  submitting.value = true
  try {
    // 急诊科挂号不需要选医生，传0作为占位符
    const doctorId = selectedDepartment.value === "急诊科" ? 0 : selectedDoctorId.value!
    await regStore.create({
      patientId: userStore.userId,
      doctorId: doctorId,
      department: selectedDepartment.value,
      registrationDate: selectedDate.value,
      timeSlot: selectedTimeSlot.value,
      symptom: symptom.value,
    })
    ElMessage.success("挂号成功！")
    router.push("/patient/my-registrations")
  } catch { }
  finally { submitting.value = false }
}
</script>

<style scoped>
.page-title { font-size: 22px; margin-bottom: 24px; }
.dept-group { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.time-slot-group { display: flex; flex-direction: column; gap: 12px; margin-top: 12px; }
.action-bar { margin-top: 24px; display: flex; gap: 12px; }
.doctor-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; margin-top: 16px; }
.doctor-card { border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 16px; cursor: pointer; transition: all 0.2s; }
.doctor-card:hover { border-color: var(--primary); box-shadow: var(--shadow-sm); }
.doctor-card.selected { border-color: var(--primary); background: var(--primary-light); }
.doctor-name { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.doctor-info { display: flex; align-items: center; gap: 8px; }
</style>

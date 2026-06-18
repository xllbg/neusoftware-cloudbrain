<template>
  <div class="patient-list page-container">
    <div class="page-header">
      <h2 class="page-title">挂号患者列表</h2>
      <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
    </div>

    <el-card class="page-card">
      <app-loading :visible="loading" />
      <div v-if="!loading && records.length === 0">
        <app-empty description="暂无挂号患者" />
      </div>
      <el-table v-else :data="records" stripe border>
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="department" label="科室" width="120" />
        <el-table-column prop="registrationDate" label="就诊日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="80">
          <template #default="{ row }">{{ row.timeSlot === "MORNING" ? "上午" : "下午" }}</template>
        </el-table-column>
        <el-table-column prop="symptom" label="症状" min-width="160" show-overflow-tooltip />
        <el-table-column prop="triageResult" label="分诊结果" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="row.status !== 'PENDING'"
              @click="startConsultation(row)"
            >
              接诊
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel } from "@/utils/format"
import type { RegistrationRecord } from "@/types"

const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()
const loading = ref(false)
const records = ref<RegistrationRecord[]>([])

onMounted(async () => {
  if (!userStore.userId) return
  loading.value = true
  try {
    const res = await regStore.fetchList({ doctorId: userStore.userId })
    records.value = res || []
  } finally { loading.value = false }
})

function startConsultation(row: RegistrationRecord) {
  router.push(`/doctor/consultation/${row.id}`)
}

function handleLogout() {
  userStore.logout()
  router.push("/doctor/login")
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 22px; color: #303133; margin: 0; }
</style>

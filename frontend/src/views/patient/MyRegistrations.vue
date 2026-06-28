<template>
  <div class="my-registrations page-container">
    <h2 class="page-title">我的挂号记录</h2>

    <app-loading :visible="loading" />
    <div v-if="!loading && records.length === 0">
      <app-empty description="暂无挂号记录" :show-action="true" action-text="去挂号" @action="router.push('/patient/registration')" />
    </div>
    <div v-else class="record-list">
      <el-card v-for="record in records" :key="record.id" class="record-card">
        <div class="record-header">
          <span class="record-doctor">{{ record.doctorName }}</span>
          <el-tag :type="getStatusTag(record.status)">{{ getStatusLabel(record.status) }}</el-tag>
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="科室">{{ record.department }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ record.registrationDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ record.timeSlot === "MORNING" ? "上午" : "下午" }}</el-descriptions-item>
          <el-descriptions-item label="症状">{{ record.symptom || "无" }}</el-descriptions-item>
        </el-descriptions>
        <div class="record-actions" v-if="record.status === 'pending'">
          <el-button type="danger" size="small" @click="handleCancel(record.id)">取消挂号</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { useRegistrationStore } from "@/stores/registration"
import { useUserStore } from "@/stores/user"
import { getStatusTag, getStatusLabel } from "@/utils/format"
import { storeToRefs } from "pinia"

const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()
const { records, loading } = storeToRefs(regStore)

async function loadData() {
  if (!userStore.userId) return
  await regStore.fetchList({ patientId: userStore.userId })
}

async function handleCancel(id: number) {
  if (!userStore.userId) return
  try {
    await ElMessageBox.confirm("确定要取消该挂号吗？", "确认", { type: "warning" })
    await regStore.cancel(id, userStore.userId)
    ElMessage.success("已取消")
  } catch {
    // 用户取消对话框
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-title { font-size: 22px; color: #303133; margin-bottom: 24px; }
.record-list { display: flex; flex-direction: column; gap: 16px; }
.record-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.record-doctor { font-size: 16px; font-weight: 600; color: #303133; }
.record-actions { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

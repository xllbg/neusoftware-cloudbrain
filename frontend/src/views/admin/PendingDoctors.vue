<template>
  <div class="admin-page">
    <div class="admin-topbar">
      <div class="admin-back" @click="$router.push('/')">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </div>
      <div class="admin-title-area">
        <h1>医生注册审批</h1>
        <p>管理医生的注册申请</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="待审批" name="pending">
        <el-table :data="pendingDoctors" stripe style="width:100%" v-loading="loading">
          <el-table-column prop="username" label="用户名" width="100" />
          <el-table-column prop="name" label="姓名" width="80" />
          <el-table-column prop="gender" label="性别" width="55" />
          <el-table-column prop="age" label="年龄" width="55" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="hospital" label="医院" width="140" />
          <el-table-column prop="department" label="科室" width="80" />
          <el-table-column prop="title" label="职称" width="80" />
          <el-table-column prop="introduction" label="简介" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleApprove(row.id)">批准</el-button>
              <el-button type="danger" size="small" @click="showRejectDialog(row.id)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && pendingDoctors.length === 0" description="暂无待审批的申请" />
      </el-tab-pane>

      <el-tab-pane label="全部医生" name="all">
        <el-table :data="allDoctors" stripe style="width:100%" v-loading="loading">
          <el-table-column prop="username" label="用户名" width="100" />
          <el-table-column prop="name" label="姓名" width="80" />
          <el-table-column prop="department" label="科室" width="80" />
          <el-table-column prop="title" label="职称" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'APPROVED'" type="success" size="small">已批准</el-tag>
              <el-tag v-else-if="row.status === 'PENDING'" type="warning" size="small">待审批</el-tag>
              <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rejectReason" label="拒绝原因" min-width="150" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因（可选）" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { ArrowLeft } from "@element-plus/icons-vue"
import { getPendingDoctors, approveDoctor, rejectDoctor } from "@/api/user"

const activeTab = ref("pending")
const loading = ref(false)
const pendingDoctors = ref<any[]>([])
const allDoctors = ref<any[]>([])
const rejectDialogVisible = ref(false)
const rejectDoctorId = ref<number | null>(null)
const rejectReason = ref("")

async function loadData() {
  loading.value = true
  try {
    const res = await getPendingDoctors()
    pendingDoctors.value = res.data || []
  } catch { }
  finally { loading.value = false }
}

async function loadAll() {
  loading.value = true
  try {
    const { get } = await import("@/api/index")
    const res = await get("/doctor/all-with-status")
    allDoctors.value = res.data || []
  } catch { }
  finally { loading.value = false }
}

async function handleApprove(id: number) {
  try {
    await ElMessageBox.confirm("确认批准该医生注册申请？", "确认", { type: "info" })
    await approveDoctor(id)
    ElMessage.success("已批准")
    loadData()
    loadAll()
  } catch { }
}

function showRejectDialog(id: number) {
  rejectDoctorId.value = id
  rejectReason.value = ""
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (rejectDoctorId.value) {
    try {
      await rejectDoctor(rejectDoctorId.value, rejectReason.value)
      ElMessage.success("已拒绝")
      rejectDialogVisible.value = false
      loadData()
      loadAll()
    } catch { }
  }
}

onMounted(() => {
  loadData()
  loadAll()
})
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 48px;
}
.admin-topbar {
  margin-bottom: 24px;
}
.admin-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #5f6368;
  cursor: pointer;
  margin-bottom: 12px;
  transition: color 0.2s;
}
.admin-back:hover { color: #1a73e8; }
.admin-title-area h1 {
  font-size: 24px;
  font-weight: 600;
  color: #202124;
  margin-bottom: 4px;
}
.admin-title-area p {
  color: #5f6368;
  font-size: 14px;
}
</style>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>智慧云脑诊疗平台</h1>
        <p>医生登录</p>
      </div>

      <!-- 登录表单 -->
      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="name">
          <el-input v-model="loginForm.name" placeholder="姓名" :prefix-icon="User" @input="clearStatus" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="loginForm.phone" placeholder="手机号" :prefix-icon="Iphone" @input="clearStatus" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="submitting" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>

      <!-- 审核状态查询 -->
      <div class="status-section">
        <el-divider><span class="divider-text">审核状态查询</span></el-divider>
        <div class="status-query">
          <el-input v-model="statusName" placeholder="姓名" size="default" style="flex:1" />
          <el-input v-model="statusPhone" placeholder="手机号" size="default" style="flex:1" @keyup.enter="checkStatus" />
          <el-button :loading="checkingStatus" @click="checkStatus">查询</el-button>
        </div>
        <el-alert v-if="statusResult" :type="statusAlertType" show-icon :closable="false" class="status-alert">
          <template #title>
            <div v-if="statusResult.status === 'APPROVED'">✅ 您的账户已通过审核，请登录使用</div>
            <div v-else-if="statusResult.status === 'PENDING'">⏳ 您的账户正在等待管理员审核，请耐心等待</div>
            <div v-else-if="statusResult.status === 'REJECTED'">❌ 您的账户未通过审核{{ statusResult.rejectReason ? '，原因：' + statusResult.rejectReason : '' }}</div>
            <div v-else>未找到该医生信息</div>
          </template>
        </el-alert>
      </div>

      <div class="login-footer">
        还没有账号？<router-link to="/doctor/register">立即注册</router-link>
        <span class="separator">|</span>
        <router-link to="/">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { User, Lock, Iphone } from "@element-plus/icons-vue"
import { useUserStore } from "@/stores/user"
import { getDoctorStatus } from "@/api/user"

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)

const loginForm = reactive({ name: "", phone: "", password: "" })
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await userStore.doctorLoginByPhone(loginForm)
    ElMessage.success("登录成功")
    router.push("/doctor/patients")
  } catch (e: any) {
    // 登录失败 → 自动查审核状态
    const msg = e?.message || ""
    if (msg.includes("审核") || msg.includes("审批")) {
      statusName.value = loginForm.name
      statusPhone.value = loginForm.phone
      await checkStatus()
    }
  }
  finally { submitting.value = false }
}

// 审核状态查询
const statusName = ref("")
const statusPhone = ref("")
const checkingStatus = ref(false)
const statusResult = ref<any>(null)

const statusAlertType = computed(() => {
  if (!statusResult.value) return "info"
  if (statusResult.value.status === "APPROVED") return "success"
  if (statusResult.value.status === "PENDING") return "warning"
  if (statusResult.value.status === "REJECTED") return "error"
  return "info"
})

function clearStatus() {
  statusResult.value = null
}

async function checkStatus() {
  if (!statusName.value.trim() || !statusPhone.value.trim()) {
    ElMessage.warning("请输入姓名和手机号")
    return
  }
  checkingStatus.value = true
  statusResult.value = null
  try {
    const res = await getDoctorStatus(statusName.value.trim(), statusPhone.value.trim())
    statusResult.value = res.data
  } catch {
    statusResult.value = { status: "NOT_FOUND" }
  }
  finally { checkingStatus.value = false }
}
</script>

<style scoped>
.login-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
}
.login-card {
  width: 440px; padding: 40px; background: #fff;
  border-radius: 12px; box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}
.login-header { text-align: center; margin-bottom: 28px; }
.login-header h1 { font-size: 22px; color: #303133; margin-bottom: 8px; }
.login-header p { color: #909399; font-size: 14px; }
.login-btn { width: 100%; }
.status-section { margin-top: 8px; }
.divider-text { font-size: 12px; color: #b0b4b9; }
.status-query { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.status-query .el-input { min-width: 100px; }
.status-alert { margin-top: 8px; }
.login-footer { text-align: center; font-size: 13px; color: #909399; margin-top: 20px; }
.login-footer a { color: #409eff; text-decoration: none; font-weight: 500; }
.separator { margin: 0 8px; color: #dcdfe6; }
</style>

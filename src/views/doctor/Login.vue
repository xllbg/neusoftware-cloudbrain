<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>智慧云脑诊疗平台</h1>
        <p>医生登录</p>
      </div>
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入工号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="submitting" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { User, Lock } from "@element-plus/icons-vue"
import { useUserStore } from "@/stores/user"

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)

const loginForm = reactive({ username: "", password: "" })
const rules = {
  username: [{ required: true, message: "请输入工号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await userStore.doctorLogin(loginForm)
    ElMessage.success("登录成功")
    router.push("/doctor/patients")
  } catch { }
  finally { submitting.value = false }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); }
.login-card { width: 400px; padding: 40px; background: #fff; border-radius: 12px; box-shadow: 0 12px 40px rgba(0,0,0,0.15); }
.login-header { text-align: center; margin-bottom: 32px; }
.login-header h1 { font-size: 22px; color: #303133; margin-bottom: 8px; }
.login-header p { color: #909399; font-size: 14px; }
.login-btn { width: 100%; }
</style>

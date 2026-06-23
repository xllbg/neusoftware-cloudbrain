<template>
  <div class="patient-login">
    <div class="login-brand">
      <div class="brand-icon"><el-icon :size="36"><User /></el-icon></div>
      <h2>欢迎回来</h2>
      <p>登录智慧云脑诊疗平台</p>
    </div>
    <div class="login-form-wrap">
      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="name">
          <el-input v-model="loginForm.name" placeholder="姓名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="loginForm.phone" placeholder="手机号" :prefix-icon="Iphone" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        还没有账号？<router-link to="/patient/register">立即注册</router-link>
        <span class="sep">|</span>
        <router-link to="/">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { User, Lock, Iphone } from "@element-plus/icons-vue"
import { useUserStore } from "@/stores/user"

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const loginForm = reactive({ name: "", phone: "", password: "" })
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.patientLoginByPhone(loginForm)
    ElMessage.success("登录成功")
    router.push("/patient/triage")
  } catch { }
  finally { loading.value = false }
}
</script>

<style scoped>
.patient-login {
  min-height: 100vh; background: #fff; display: flex;
  flex-direction: column; justify-content: center; padding: 24px 28px;
}
.login-brand { text-align: center; margin-bottom: 40px; }
.brand-icon {
  width: 64px; height: 64px;
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  border-radius: 18px; display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px; color: #fff; box-shadow: 0 6px 16px rgba(26,115,232,0.25);
}
.login-brand h2 { font-size: 22px; font-weight: 700; color: #202124; margin-bottom: 6px; }
.login-brand p { font-size: 14px; color: #5f6368; }
.login-btn { width: 100%; height: 48px; font-size: 17px; border-radius: 10px; }
.login-footer { text-align: center; font-size: 14px; color: #5f6368; margin-top: 24px; }
.login-footer a { color: #1a73e8; text-decoration: none; font-weight: 500; }
.sep { margin: 0 8px; color: #ddd; }
</style>

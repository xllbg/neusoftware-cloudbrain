<template>
  <div class="patient-register">
    <div class="register-brand">
      <h2>创建账号</h2>
      <p>填写信息完成注册</p>
    </div>

    <div class="register-form-wrap">
      <el-form ref="formRef" :model="registerForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item prop="name">
          <el-input v-model="registerForm.name" placeholder="姓名" size="large" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="手机号" size="large" />
        </el-form-item>
        <el-form-item prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio-button label="男">男</el-radio-button>
            <el-radio-button label="女">女</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="age">
          <el-input-number v-model="registerForm.age" :min="1" :max="150" style="width:100%" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="register-btn" :loading="submitting" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/patient/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { useUserStore } from "@/stores/user"

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)

const registerForm = reactive({
  username: "", password: "", name: "", phone: "", gender: "男", age: 20,
})

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  age: [{ required: true, message: "请输入年龄", trigger: "blur" }],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await userStore.patientRegister(registerForm as any)
    ElMessage.success("注册成功")
    router.push("/patient/triage")
  } catch { }
  finally { submitting.value = false }
}
</script>

<style scoped>
.patient-register {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 28px;
}
.register-brand {
  text-align: center;
  margin-bottom: 32px;
}
.register-brand h2 { font-size: 22px; font-weight: 700; color: #202124; margin-bottom: 6px; }
.register-brand p { font-size: 14px; color: #5f6368; }
.register-btn { width: 100%; height: 48px; font-size: 17px; border-radius: 10px; margin-top: 4px; }
.register-footer { text-align: center; font-size: 14px; color: #5f6368; margin-top: 24px; }
.register-footer a { color: #1a73e8; text-decoration: none; font-weight: 500; }
.el-radio-group { display: flex; width: 100%; }
.el-radio-button { flex: 1; }
</style>

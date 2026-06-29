<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1>患者注册</h1>
        <p>填写信息完成注册</p>
      </div>
      <el-form ref="formRef" :model="registerForm" :rules="rules" label-width="90px" size="large">
        <el-divider content-position="left">账户信息</el-divider>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请设置密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" show-password />
        </el-form-item>

        <el-divider content-position="left">个人信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="registerForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="registerForm.age" :min="1" :max="150" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="registerForm.idCard" placeholder="请输入身份证号（可选）" maxlength="18" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="registerForm.address" placeholder="请输入地址（可选）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/patient/login">立即登录</router-link>
        <span class="split">|</span>
        <router-link to="/patient/login" class="back-link">返回登录</router-link>
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

const validatePass = (_rule: any, value: string, callback: any) => {
  if (value === "") {
    callback(new Error("请再次输入密码"))
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerForm = reactive({
  name: "", phone: "", password: "", confirmPassword: "", gender: "男", age: 20,
  idCard: "", address: "",
})

const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  password: [{ required: true, message: "请设置密码", trigger: "blur" }],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validatePass, trigger: "blur" },
  ],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  age: [{ required: true, message: "请输入年龄", trigger: "blur" }],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const { confirmPassword, ...submitData } = registerForm
    await userStore.patientRegister(submitData as any)
    ElMessage.success("注册成功")
    router.push("/patient/triage")
  } catch { }
  finally { submitting.value = false }
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #fff;
  padding: 40px 20px;
}
.register-card {
  width: 600px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.register-header {
  text-align: center;
  margin-bottom: 8px;
}
.register-header h1 { font-size: 24px; color: #303133; margin-bottom: 8px; }
.register-header p { color: #909399; font-size: 14px; }
.submit-btn { width: 100%; margin-top: 8px; height: 44px; font-size: 16px; }
.register-footer { text-align: center; font-size: 14px; color: #909399; margin-top: 16px; display: flex; justify-content: center; gap: 12px; }
.register-footer a { color: #409eff; text-decoration: none; }
.register-footer .back-link { color: #409eff; }
.el-divider { margin: 20px 0; }
</style>

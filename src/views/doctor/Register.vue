<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1>医生注册</h1>
        <p>填写完整信息，提交注册申请</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" size="large">
        <el-divider content-position="left">账户信息</el-divider>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用于登录的账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请设置密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password />
        </el-form-item>

        <el-divider content-position="left">个人信息</el-divider>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="20" :max="80" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-divider content-position="left">职业信息</el-divider>
        <el-form-item label="所属医院" prop="hospital">
          <el-input v-model="form.hospital" placeholder="请输入所在医院名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室" prop="department">
              <el-select v-model="form.department" placeholder="请选择科室" style="width:100%">
                <el-option label="心内科" value="心内科" />
                <el-option label="呼吸内科" value="呼吸内科" />
                <el-option label="消化内科" value="消化内科" />
                <el-option label="神经内科" value="神经内科" />
                <el-option label="骨科" value="骨科" />
                <el-option label="儿科" value="儿科" />
                <el-option label="妇产科" value="妇产科" />
                <el-option label="眼科" value="眼科" />
                <el-option label="耳鼻喉科" value="耳鼻喉科" />
                <el-option label="皮肤科" value="皮肤科" />
                <el-option label="中医科" value="中医科" />
                <el-option label="急诊科" value="急诊科" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="form.title" placeholder="请选择职称" style="width:100%">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="住院医师" value="住院医师" />
                <el-option label="医士" value="医士" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="个人简介" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="请简要介绍您的专业背景和擅长领域" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleRegister">
            提交注册申请
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/doctor/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { registerDoctor } from "@/api/user"

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  name: "",
  gender: "男",
  age: 35,
  phone: "",
  hospital: "",
  department: "",
  title: "",
  introduction: "",
})

const validatePass = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error("两次输入密码不一致"))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度3-20位", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请设置密码", trigger: "blur" },
    { min: 6, message: "密码至少6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validatePass, trigger: "blur" },
  ],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  age: [{ required: true, message: "请输入年龄", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  hospital: [{ required: true, message: "请输入医院名称", trigger: "blur" }],
  department: [{ required: true, message: "请选择科室", trigger: "change" }],
  title: [{ required: true, message: "请选择职称", trigger: "change" }],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const { confirmPassword, ...submitData } = form
    await registerDoctor(submitData)
    ElMessage.success("注册申请已提交，请等待管理员审核")
    router.push("/doctor/login")
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 40px 20px;
}
.register-card {
  width: 600px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}
.register-header {
  text-align: center;
  margin-bottom: 8px;
}
.register-header h1 { font-size: 24px; color: #303133; margin-bottom: 8px; }
.register-header p { color: #909399; font-size: 14px; }
.submit-btn { width: 100%; margin-top: 8px; height: 44px; font-size: 16px; }
.register-footer { text-align: center; font-size: 14px; color: #909399; margin-top: 16px; }
.register-footer a { color: #409eff; text-decoration: none; }
.el-divider { margin: 20px 0; }
</style>

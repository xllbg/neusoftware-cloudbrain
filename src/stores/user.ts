import { defineStore } from "pinia"
import { ref, computed } from "vue"
import { getToken, setToken, removeToken, setUser, getUser } from "@/utils/auth"
import type { UserInfo, LoginForm, RegisterForm } from "@/types"
import { loginPatient, loginDoctor, registerPatient, getCurrentUser } from "@/api/user"

export const useUserStore = defineStore("user", () => {
  // 状态
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getUser())

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isPatient = computed(() => userInfo.value?.role === "PATIENT")
  const isDoctor = computed(() => userInfo.value?.role === "DOCTOR")
  const userName = computed(() => userInfo.value?.name ?? "")
  const userId = computed(() => userInfo.value?.id)

  // 患者登录
  async function patientLogin(loginForm: { username: string; password: string }) {
    const res = await loginPatient(loginForm)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    setToken(res.data.token)
    setUser(res.data.userInfo)
    return res.data
  }

  // 医生登录
  async function doctorLogin(loginForm: { username: string; password: string }) {
    const res = await loginDoctor(loginForm)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    setToken(res.data.token)
    setUser(res.data.userInfo)
    return res.data
  }

  // 患者注册
  async function patientRegister(registerForm: RegisterForm) {
    const res = await registerPatient(registerForm)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    setToken(res.data.token)
    setUser(res.data.userInfo)
    return res.data
  }

  // 刷新用户信息
  async function refreshUserInfo() {
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data
      setUser(res.data)
    } catch {
      // 刷新失败不阻塞
    }
  }

  // 退出登录
  function logout() {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isPatient,
    isDoctor,
    userName,
    userId,
    patientLogin,
    doctorLogin,
    patientRegister,
    refreshUserInfo,
    logout,
  }
})

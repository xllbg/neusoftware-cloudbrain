import { defineStore } from "pinia"
import { ref, computed } from "vue"
import { getToken, setToken, removeToken, setUser, getUser } from "@/utils/auth"
import type { LoginForm, RegisterForm } from "@/types"
import { loginPatient, loginDoctor, registerPatient } from "@/api/user"

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<any | null>(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const isPatient = computed(() => userInfo.value?.role === "PATIENT")
  const isDoctor = computed(() => userInfo.value?.role === "DOCTOR")
  const userName = computed(() => userInfo.value?.name ?? "")
  const userId = computed(() => userInfo.value?.userId)

  // 患者登录（对齐后端 LoginResponse: { token, userId, username, name, role }）
  async function patientLogin(loginForm: { username: string; password: string }) {
    const res = await loginPatient(loginForm)
    token.value = res.data.token
    userInfo.value = res.data   // 直接存 LoginResponse
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  async function doctorLogin(loginForm: { username: string; password: string }) {
    const res = await loginDoctor(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  async function patientRegister(registerForm: RegisterForm) {
    const res = await registerPatient(registerForm)
    token.value = res.data.token
    userInfo.value = res.data
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  function logout() {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return { token, userInfo, isLoggedIn, isPatient, isDoctor, userName, userId, patientLogin, doctorLogin, patientRegister, logout }
})

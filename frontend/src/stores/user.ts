import { defineStore } from "pinia"
import { ref, computed } from "vue"
import { getToken, setToken, removeToken, setUser, getUser } from "@/utils/auth"
import type { LoginForm, RegisterForm } from "@/types"
import { loginPatient, loginDoctor, registerPatient, loginPatientByPhone, loginDoctorByPhone } from "@/api/user"

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<any | null>(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const isPatient = computed(() => userInfo.value?.role === "PATIENT")
  const isDoctor = computed(() => userInfo.value?.role === "DOCTOR")
  const userName = computed(() => userInfo.value?.name ?? "")
  const userId = computed(() => userInfo.value?.userId)

  // 患者登录 (username+password)
  async function patientLogin(loginForm: { username: string; password: string }) {
    const res = await loginPatient(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  // 患者登录 (name+phone+password)
  async function patientLoginByPhone(loginForm: { name: string; phone: string; password: string }) {
    const res = await loginPatientByPhone(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  // 医生登录 (username+password)
  async function doctorLogin(loginForm: { username: string; password: string }) {
    const res = await loginDoctor(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  // 医生登录 (name+phone+password)
  async function doctorLoginByPhone(loginForm: { name: string; phone: string; password: string }) {
    const res = await loginDoctorByPhone(loginForm)
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

  return { token, userInfo, isLoggedIn, isPatient, isDoctor, userName, userId, patientLogin, patientLoginByPhone, doctorLogin, doctorLoginByPhone, patientRegister, logout }
})

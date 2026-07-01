import { defineStore } from "pinia"
import { ref, computed } from "vue"
import {
  getDoctorToken, setDoctorToken, removeDoctorToken, setDoctorUser, getDoctorUser,
  getPatientToken, setPatientToken, removePatientToken, setPatientUser, getPatientUser,
} from "@/utils/auth"
import type { LoginForm, RegisterForm } from "@/types"
import { loginPatient, loginDoctor, registerPatient, loginPatientByPhone, loginDoctorByPhone } from "@/api/user"
import router from "@/router"

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(null)
  const userInfo = ref<any | null>(null)

  function refreshFromStorage() {
    const path = router.currentRoute.value.path
    if (path.startsWith("/doctor")) {
      token.value = getDoctorToken()
      userInfo.value = getDoctorUser()
    } else if (path.startsWith("/patient")) {
      token.value = getPatientToken()
      userInfo.value = getPatientUser()
    } else {
      token.value = getDoctorToken() || getPatientToken()
      userInfo.value = getDoctorUser() || getPatientUser()
    }
  }

  refreshFromStorage()

  const isLoggedIn = computed(() => !!token.value)
  const isPatient = computed(() => userInfo.value?.role === "PATIENT")
  const isDoctor = computed(() => userInfo.value?.role === "DOCTOR")
  const userName = computed(() => userInfo.value?.name ?? "")
  const userId = computed(() => userInfo.value?.userId)

  async function patientLogin(loginForm: { username: string; password: string }) {
    const res = await loginPatient(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setPatientToken(res.data.token)
    setPatientUser(res.data)
    return res.data
  }

  async function patientLoginByPhone(loginForm: { name: string; phone: string; password: string }) {
    const res = await loginPatientByPhone(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setPatientToken(res.data.token)
    setPatientUser(res.data)
    return res.data
  }

  async function doctorLogin(loginForm: { username: string; password: string }) {
    const res = await loginDoctor(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setDoctorToken(res.data.token)
    setDoctorUser(res.data)
    return res.data
  }

  async function doctorLoginByPhone(loginForm: { name: string; phone: string; password: string }) {
    const res = await loginDoctorByPhone(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    setDoctorToken(res.data.token)
    setDoctorUser(res.data)
    return res.data
  }

  async function patientRegister(registerForm: RegisterForm) {
    const res = await registerPatient(registerForm)
    token.value = res.data.token
    userInfo.value = res.data
    setPatientToken(res.data.token)
    setPatientUser(res.data)
    return res.data
  }

  function logout() {
    const role = userInfo.value?.role
    token.value = null
    userInfo.value = null
    if (role === "DOCTOR") {
      removeDoctorToken()
    } else if (role === "PATIENT") {
      removePatientToken()
    }
  }

  return { token, userInfo, isLoggedIn, isPatient, isDoctor, userName, userId, patientLogin, patientLoginByPhone, doctorLogin, doctorLoginByPhone, patientRegister, logout, refreshFromStorage }
})

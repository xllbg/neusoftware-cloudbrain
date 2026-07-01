// Token 存储键名（通用）
const TOKEN_KEY = "cloud-brain-token"
const USER_KEY = "cloud-brain-user"

// 医生专用
const DOCTOR_TOKEN_KEY = "cloud-brain-doctor-token"
const DOCTOR_USER_KEY = "cloud-brain-doctor-user"

// 患者专用
const PATIENT_TOKEN_KEY = "cloud-brain-patient-token"
const PATIENT_USER_KEY = "cloud-brain-patient-user"

// ============ 通用方法（保留兼容） ============
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser(): any | null {
  const userStr = localStorage.getItem(USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  }
  return null
}

export function setUser(user: any): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

// ============ 医生专用方法 ============
export function getDoctorToken(): string | null {
  return localStorage.getItem(DOCTOR_TOKEN_KEY)
}

export function setDoctorToken(token: string): void {
  localStorage.setItem(DOCTOR_TOKEN_KEY, token)
}

export function removeDoctorToken(): void {
  localStorage.removeItem(DOCTOR_TOKEN_KEY)
  localStorage.removeItem(DOCTOR_USER_KEY)
}

export function getDoctorUser(): any | null {
  const userStr = localStorage.getItem(DOCTOR_USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  }
  return null
}

export function setDoctorUser(user: any): void {
  localStorage.setItem(DOCTOR_USER_KEY, JSON.stringify(user))
}

// ============ 患者专用方法 ============
export function getPatientToken(): string | null {
  return localStorage.getItem(PATIENT_TOKEN_KEY)
}

export function setPatientToken(token: string): void {
  localStorage.setItem(PATIENT_TOKEN_KEY, token)
}

export function removePatientToken(): void {
  localStorage.removeItem(PATIENT_TOKEN_KEY)
  localStorage.removeItem(PATIENT_USER_KEY)
}

export function getPatientUser(): any | null {
  const userStr = localStorage.getItem(PATIENT_USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  }
  return null
}

export function setPatientUser(user: any): void {
  localStorage.setItem(PATIENT_USER_KEY, JSON.stringify(user))
}

// ============ 辅助判断函数 ============

export function isLoggedIn(): boolean {
  return !!getDoctorToken() || !!getPatientToken()
}

export function isPatient(): boolean {
  const user = getPatientUser()
  return user?.role === "PATIENT"
}

export function isDoctor(): boolean {
  const user = getDoctorUser()
  return user?.role === "DOCTOR"
}

// 用户登录/注册 API（对齐后端 PatientController）
import { post, get } from "./index"
import type { LoginForm, RegisterForm, LoginResult } from "@/types"

// 患者注册 → POST /api/patient/register { username, password, name, gender, age, phone }
export function registerPatient(data: RegisterForm) {
  return post<LoginResult>("/patient/register", data)
}

// 患者登录 → POST /api/patient/login { username, password }
export function loginPatient(data: { username: string; password: string }) {
  return post<LoginResult>("/patient/login", data)
}

// 医生登录（后端暂未实现，保留接口）
export function loginDoctor(data: { username: string; password: string }) {
  return post<LoginResult>("/doctor/login", data)
}

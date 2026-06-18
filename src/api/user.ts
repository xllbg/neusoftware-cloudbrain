import { post, get } from "./index"
import type { LoginForm, RegisterForm, LoginResult, UserInfo } from "@/types"

// 患者注册
export function registerPatient(data: RegisterForm) {
  return post<LoginResult>("/patient/register", data)
}

// 患者登录
export function loginPatient(data: { username: string; password: string }) {
  return post<LoginResult>("/patient/login", data)
}

// 医生登录
export function loginDoctor(data: { username: string; password: string }) {
  return post<LoginResult>("/doctor/login", data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return get<UserInfo>("/user/info")
}

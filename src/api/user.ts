import { post, get } from "./index"
import type { LoginForm, RegisterForm, LoginResult } from "@/types"

export function registerPatient(data: RegisterForm) {
  return post<LoginResult>("/patient/register", data)
}

export function loginPatient(data: { name: string; phone: string; password: string }) {
  return post<LoginResult>("/patient/login", data)
}

export function loginDoctor(data: { name: string; phone: string; password: string }) {
  return post<LoginResult>("/doctor/login", data)
}

// 医生注册
export function registerDoctor(data: any) {
  return post<string>("/doctor/register", data)
}

// 查询医生审核状态（姓名+手机号）
export function getDoctorStatus(name: string, phone: string) {
  return get<any>("/doctor/status", { name, phone })
}

// 获取待审批医生列表（管理员）
export function getPendingDoctors() {
  return get<any[]>("/doctor/pending")
}

// 批准医生
export function approveDoctor(id: number) {
  return post<string>("/doctor/approve", { id })
}

// 拒绝医生
export function rejectDoctor(id: number, reason: string) {
  return post<string>("/doctor/reject", { id, reason })
}
